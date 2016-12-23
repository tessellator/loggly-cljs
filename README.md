# loggly-cljs

Client-side (browser) logger to use with Loggly gen2. It is meant to provide capabilities similar to and compatible with Loggly's [loggly-jslogger](https://github.com/loggly/loggly-jslogger) library.


## Installation
Add the following to your `project.clj` file:

[![Clojars Project](https://clojars.org/loggly-cljs/latest-version.svg)](https://clojars.org/loggly-cljs)


## Quick Start

```clojure
(ns my-project.core
  (:require [loggly.core :as loggly]))

(loggly/set-config! {:key "my-customer-token"})

(loggly/log "some log message")
(loggly/log {:key :value})
```


## Configuration Options
There are several configuration options available to control the behavior of the tracker. They are as follows:

Key | Description
--- | -----------
`:key` | Required. The customer token provided by Loggly.
`:protocol` | The protocol used to send messages. May be `:http` or `:https`. Defaults to current window.location.protocol.
`:tag` | The tag applied to messages when sending them to Loggly. A comma-separated string containing tag names (e.g. \"tag1,tag2\"). The tag may be overriden on a per-message level; see the document for `log`. Defaults to \"jslogger\" to be compatible with the loggly-jslogger library.
`:use-domain-proxy?` | A boolean that indicates whether the tracker should use the host domain as a proxy. Defaults to false.
`:proxy-route` | The route on the host domain to which messages should be posted. Defaults to `/loggly`.
`:collector-domain` |  The domain used to collect messages. Defaults to `logs-01.loggly.com`.
`:session-key` | The name of the cookie used to store the session ID. Defaults to `logglytrackingsession` to be compatible with the loggly-jslogger library.
`:session-id` | A UUID string that identifies the current session. If provided, the ID is stored in the cookie specified by session-key. If not provided, an attempt is made to recover the session ID from the cookie. If not found, a new UUID string is used and stored in the cookie.
`:handler` | The function that should be run upon a message being sent successfully. Defaults to a no-op.
`:error-hander` | The function that should be run upon failure to send a message. The function must accept the message and the HTTP error as parameters. Defaults to `println`.


## Per-Message Tagging
When configuring the tracker, you may set a tag (or list of tags) to be applied to all messages by providing a comma-delimited string (e.g., `tag1,tag2`).

You may wish to override this behavior and specify tags to be applied to an individual message. Overriding tags may be applied to map messages by using the `:loggly/tag` keyword. A message of `{:text "my message"}` will use the tag specified on the tracker (default: 'jslogger'), but a message of `{:text "my message" :loggly/tag "tag1,tag2"}` will have both `tag1` and `tag2` applied to it in Loggly.


## Multiple Trackers
In some cases, you may wish to log to many trackers. In this case, you only need to create a tracker and log to it directly. An example is provided below. The configuration and logging options match those in the global case.

```clojure
(ns my-project.core
  (:require [loggly.tracker :as t]))

(def t1 (t/tracker {:key "my-first-key"}))
(def t2 (t/tracker {:key "my-other-key"}))

(t/log t1 "some message")
(t/log t2 "other message")
```

## License

Copyright © 2016 Thomas C. Taylor

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
