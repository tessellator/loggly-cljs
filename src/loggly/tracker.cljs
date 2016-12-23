(ns loggly.tracker
  "Functions for configuring trackers and sending messages to Loggly."
  (:require [ajax.core :refer [POST]]
            [clojure.string :as str]
            [loggly.platform :as p]))

(defn default-config
  "Returns a default configuration without the required `:key` entry."
  []
  {:protocol (str/replace (p/protocol) ":" "")
   :session-id (str (random-uuid))
   :tag "jslogger"
   :use-domain-proxy? false
   :proxy-route "/loggly"
   :collector-domain "logs-01.loggly.com"
   :session-key "logglytrackingsession"
   :handler (fn [_])
   :error-handler println})

(defn- set-cookie [{:keys [session-key session-id] :as t}]
  (p/write-session-cookie session-key session-id)
  t)

(defn tracker
  "Creates a tracker that can send messages to Loggly using config.

  Config entries are as follows:

  `:key`               Required. The customer token provided by Loggly. If no
                       key is provided, this function returns `nil`.

  `:protocol`          The protocol used to send messages. May be :http or
                       :https. Defaults to current window.location.protocol.

  `:tag`               The tag applied to messages when sending them to
                       Loggly. A comma-separated string containing tag names
                       (e.g. \"tag1,tag2\"). The tag may be overriden on a per-
                       message level; see the document for `log`. Defaults to
                       \"jslogger\" to be compatible with the loggly-jslogger
                       library.

  `:use-domain-proxy?` A boolean that indicates whether the tracker should use
                       the host domain as a proxy. Defaults to false.

  `:proxy-route`       The route on the host domain to which messages should be
                       posted. Defaults to \"/loggly\".

  `:collector-domain`  The domain used to collect messages. Defaults to
                       \"logs-01.loggly.com\".

  `:session-key`       The name of the cookie used to store the session ID.
                       Defaults to \"logglytrackingsession\" to be compatible
                       with the loggly-jslogger library.

  `:session-id`        A UUID string that identifies the current session. If
                       provided, the ID is stored in the cookie specified by
                       session-key. If not provided, this function attempts to
                       recover it from the cookie. If not found, a new UUID
                       string is used and stored in the cookie.

  `:handler`           The function that should be run upon a message being sent
                       successfully. Defaults to a no-op.

  `:error-hander`      The function that should be run upon failure to send a
                       message. The function must accept the message and the
                       HTTP error as parameters. Defaults to `println`."
  ([config]
   (when (:key config)
    (let [t (merge (default-config) config)]
      (if (:session-id config)
        (set-cookie t)
        (if-let [sid (p/read-cookie (:session-key t))]
          (assoc t :session-id sid)
          (set-cookie t)))))))

(defn- url
  [{:keys [use-domain-proxy? proxy-route protocol collector-domain key] :as t} tag]
  (if use-domain-proxy?
    (str (name protocol) "://" (p/host) proxy-route "/inputs/" key "/tag/" tag)
    (str (name protocol) "://" collector-domain "/inputs/" key "/tag/" tag)))

(defn log
  "Sends message to Loggly using tracker.

  message may be a string or a map.

  If message is a map, it may optionally override the tracker's tag by including
  a :loggly/tag key with a string (comma-separated) value containing the names
  of the tags.

  Each message will be appended with a :sessionId value based on the session-id
  of the tracker. This provides compatibility with the output of the
  loggly-jslogger library."
  [tracker message]
  (let [msg (if (string? message) {:text message} message)
        tag (or (:loggly/tag msg) (:tag tracker))
        url (url tracker tag)
        msg (-> (dissoc msg :loggly/tag)
                (assoc :sessionId (:session-id tracker)))]
    (POST url {:params (.stringify js/JSON (clj->js msg))
               :format :text
               :handler (:handler tracker)
               :error-handler (partial (:error-handler tracker) msg)})
    nil))
