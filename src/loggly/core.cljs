(ns loggly.core
  "Functions for sending data to Loggly."
  (:require [loggly.tracker :as lt]))

(def ^:private tracker (atom nil))

(defn set-config!
  "Sets the configuration for the global tracker.

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
  [config]
  (reset! tracker (lt/tracker config)))

(defn log
  "Sends a message to the global tracker. Throws an exception if the tracker has
  not been configured.

  msg  may be a string or a map.

  If msg is a map, it may optionally override the tracker's tag by including
  a :loggly/tag key with a string (comma-separated) value containing the names
  of the tags.

  Each message will be appended with a :sessionId value based on the session-id
  of the global tracker. This provides compatibility with the output of the
  loggly-jslogger library."
  [msg]
  (if-let [t @tracker]
    (lt/log t msg)
    (throw (js/Error. "Loggly tracker not initialized. Please configure it with 'loggly.core/set-config!'"))))
