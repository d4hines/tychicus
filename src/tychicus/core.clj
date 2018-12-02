(ns tychicus.core
  (:gen-class)
  (:require
   [clojure-mail.core :as mail]
   [taoensso.timbre :as timbre]
   [clojure-mail.gmail :as gmail]
   [clojure-mail.events :as events]
   [clojure-mail.message :as message :refer [read-message]]
   [postal.core :as postal]
   [environ.core :refer [env]])
  (:import (javax.mail Message)))

(def USERNAME (env :tychicus-username))
(def PASSWORD (env :tychicus-password))
(def FORWARDING_ADDRESS (env :tychicus-forwarding-address))
(def SMTP_HOST (env :tychicus-smtp-host))
(def SMTP_PORT (env :tychicus-smtp-port))
(def DEBUG (-> (env :tychicus-debug) true? str))

(def gstore (gmail/store USERNAME PASSWORD))

(def inbox-messages (mail/inbox gstore))

(def email (first inbox-messages))

(defn send-email [imap-message]
  (let [props (java.util.Properties.)]

    (doto props
      (.put "mail.debug" DEBUG)
      (.put "mail.smtp.host" SMTP_HOST)
      (.put "mail.smtp.port" SMTP_PORT)
      (.put "mail.smtp.user" USERNAME)
      (.put "mail.smtp.socketFactory.port" SMTP_PORT)
      (.put "mail.smtp.auth" "true"))

    (doto props
      (.put "mail.smtp.starttls.enable" "true")
      (.put "mail.smtp.socketFactory.class"
            "javax.net.ssl.SSLSocketFactory")
      (.put "mail.smtp.socketFactory.fallback" "false"))

    (let [authenticator (proxy [javax.mail.Authenticator] []
                          (getPasswordAuthentication
                            []
                            (javax.mail.PasswordAuthentication.
                             USERNAME PASSWORD)))
          session (javax.mail.Session/getInstance props authenticator)
          msg     (javax.mail.internet.MimeMessage. session)]

      (.setRecipients msg
                      (javax.mail.Message$RecipientType/TO)
                      (javax.mail.internet.InternetAddress/parse FORWARDING_ADDRESS))

      (.setSubject msg (.getSubject imap-message))
      (.setContent msg (.getContent imap-message))
      (.saveChanges msg)
      msg
      (javax.mail.Transport/send msg))))

(def manager
  (do
    (try (.stop manager) (catch Exception e))
    (let [s (mail/get-session "imaps")
          gstore (mail/store "imaps" s "imap.gmail.com" USERNAME PASSWORD)
          folder (mail/open-folder gstore "inbox" :readonly)
          im (events/new-idle-manager s)]
      (events/add-message-count-listener (fn [e]
                                           (let [mes (->> e
                                                          :messages
                                                          (map read-message))
                                                 subjects (map :subject mes)]
                                             (println "test" subjects)))
                                         #(prn "removed" %)
                                         folder
                                         im))))

(.stop manager)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

