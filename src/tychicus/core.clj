(ns tychicus.core
  (:gen-class)
  (:require
   [clojure-mail.core :as mail]
   [taoensso.timbre :as timbre]
   [clojure-mail.gmail :as gmail]
   [clojure-mail.events :as events]
   [clojure-mail.message :as message :refer [read-message]]
   [clojure.pprint :refer [pprint]]
   [environ.core :refer [env]]
   [clojure.spec.alpha :as s]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(def number-regex #"[0-9]+")

(s/def ::email-string (s/and string? #(re-matches email-regex %)))
(s/def ::number-string (s/and string? #(re-matches number-regex %)))
(s/def ::boolean-string (s/and string? #(contains? #{"true" "false"} %)))

(s/valid? ::boolean-string "true")

(s/def ::tychicus-username ::email-string)
(s/def ::tychicus-password string?)
(s/def ::tychicus-forwarding-address ::email-string)

(s/def ::tychicus-smtp-port ::number-string)
(s/def ::tychicus-smtp-host string?)

(s/def ::tychicus-debug ::boolean-string)

(s/def ::config (s/keys :req-un [::tychicus-username ::tychicus-password
                              ::tychicus-forwarding-address
                              ::tychicus-smtp-port ::tychicus-smtp-host]
                        :opt-un [::tychicus-debug]))

(when (not (s/valid? ::config env)
           (timbre/error (s/explain ::config env))
           (System/exit 1)))

(def USERNAME (env :tychicus-username))
(def PASSWORD (env :tychicus-password))

(def FORWARDING_ADDRESS (env :tychicus-forwarding-address))

(def SMTP_HOST (env :tychicus-smtp-host))
(def SMTP_PORT (env :tychicus-smtp-port))

(def DEBUG (-> (env :tychicus-debug) true? str))

(defn send-email
  "Forwards a received IMAP message to the configured address."
  [imap-message]
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
      (javax.mail.Transport/send msg))))

(defn noop [& args])

(defn handle-message 
  "Handles each incoming message, logging it and attempting to forward it."
  [message]
  (let [message-map (read-message message :fields [:subject :from :content-type])]
    (timbre/info "Received message: " message-map)
    (try (send-email message)
         (catch Exception e
           (timbre/error e)))))

(defn -main
  "Asks the IMAP folder for push notifications whenver a new message comes, and handles it accordingly."
  [& args]
  (let [s (mail/get-session "imaps")
        gstore (mail/store "imaps" s "imap.gmail.com" USERNAME PASSWORD)
        folder (mail/open-folder gstore "inbox" :readonly)
        im (events/new-idle-manager s)]
    (events/add-message-count-listener (fn [event]
                                         (let [{:keys [messages]} event]
                                           (doseq [m messages]
                                             (handle-message m))))
                                       noop
                                       folder
                                       im)))

