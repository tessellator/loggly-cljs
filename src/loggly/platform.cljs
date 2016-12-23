(ns loggly.platform
  "Provides simple functions to interacting with the web page."
  (:require [goog.net.cookies :as cks]))

(defn protocol
  "Gets the current protocol."
  []
  js/window.location.protocol)

(defn host
  "Gets the current host."
  []
  js/window.location.host)

(defn write-session-cookie
  "Writes a session cookie with the specified name and value."
  [name value]
  (.set goog.net.cookies name value -1))

(defn read-cookie
  "Reads the cookie specified by name."
  [name]
  (.get goog.net.cookies name))
