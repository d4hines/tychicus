(ns tychicus.core
  (:gen-class)
  (:require
   [clojure.set :refer [rename-keys]]
   [clojure-mail.core :as mail]
   [taoensso.timbre :as timbre]
   [clojure-mail.gmail :as gmail]
   [clojure-mail.events :as events]
   [clojure-mail.message :as message :refer [read-message]]
   [postal.core :as postal]
   [environ.core :refer [env]]
   [clojure.string :as str]

   [clojure.pprint :refer [pprint]])
  (:import (javax.mail Message)))

(def USERNAME (env :tychicus-username))
(def PASSWORD (env :tychicus-password))
(def FORWARDING_ADDRESS (env :tychicus-forwarding-address))
(def SMTP_HOST (env :tychicus-smtp-host))
(def SMTP_PORT (env :tychicus-port))

(def gstore (gmail/store USERNAME PASSWORD))

(def inbox-messages (mail/inbox gstore))

(def email (first infox-messages))
(def latest (read-message email))
(def messages (atom '()))

(defn send-email  [imap-message]
  (let [{:keys [cc bcc]} (read-message imap-message)
        props (java.util.Properties.)]

    (doto props
      (.put "mail.debug")
      (.put "mail.smtp.host" SMTP_HOST)
      (.put "mail.smtp.port" SMTP_PORT)
      (.put "mail.smtp.user" USERNAME)
      (.put "mail.smtp.socketFactory.port" SMTP_PORT)
      (.put "mail.smtp.auth" "true")
      ;; Enable SSL
      (.put "mail.smtp.starttls.enable" "true")
      (.put "mail.smtp.socketFactory.class"
            "javax.net.ssl.SSLSocketFacmail (apply hash-map m)
        tory")
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
      (.seContent msg (.getContent imap-message))
      (.saveChanges msg)
      (javax.mail.Transport/send msg)
      )))

(def msg (send-email email))

(defn send-email [imap-message]
  (let [props (java.util.Properties.)]

    (doto props
      (.put "mail.debug" "true")
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
          recipients (reduce #(str % "," %2) (:to mail))
          session (javax.mail.Session/getInstance props authenticator)
          msg     (javax.mail.internet.MimeMessage. session)]

      (.setRecipients msg
                      (javax.mail.Message$RecipientType/TO)
                      (javax.mail.internet.InternetAddress/parse FORWARDING_ADDRESS))

      (.setSubject msg (.getSubject imap-message))
      (.seContent msg (.getContent imap-message))
      (.saveChanges msg)

      (javax.mail.Transport/send msg))))

(send-email email)

(mail :user USER
      :password PASSWORD
      :host SMTP_HOST
      :port SMTP_PORT
      :ssl true
      :to [FORWARDING_ADDRESS]
      :subject "I Have Rebooted."
      :text "I Have Rebooted.")

(defn forward-message [message]
  (let [{:keys [subject from body]} message
        postal-body]
    (pprint postal-body)
    (println (str "Forwarding message: " subject))
    (postal/send-message {:host SMTP_HOST
                          :user USERNAME
                          :pass PASSWORD
                          :ssl true}
                         {:from from
                          :to FORWARDING_ADDRESS
                          :subject subject
                          :body [:alternative postal-body]}))) 

(doseq [m @messages] (forward-message m))

(.getRecipients email)

(postal/send-message {:host SMTP_HOST
                      :user USERNAME
                      :pass PASSWORD
                      :ssl true}
                     {:from "d4hines@gmail.com"
                      :to "d4hines@gmail.com"
                      :subject "test message"
                      :body "hellooooo world"})

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
                                                 subjects (map :subject mes)
                                                 _2 (reset! messages mes)]
                                             (doseq [m mes] (forward-message m))
                                             (println "test" subjects)))
                                         #(prn "removed" %)
                                         folder
                                         im))))

(.stop manager)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

