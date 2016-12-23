(ns loggly.tracker-test
  (:require [ajax.core :as ajax]
            [loggly.platform :as p]
            [loggly.tracker :as t]
            [clojure.test :refer-macros [deftest testing is use-fixtures]]))

(def ^:dynamic *cookie-store* nil)
(def ^:dynamic *post-value* nil)

(defn each-fixture [f]
  (binding [*cookie-store* (atom {})
            *post-value* (atom nil)]
    (with-redefs [p/write-session-cookie (fn [n v] (swap! *cookie-store* assoc n v))
                  p/read-cookie (fn [n] (get @*cookie-store* n))
                  p/protocol (constantly "https:")
                  p/host (constantly "example.com")
                  ajax/POST (fn [url opts] (reset! *post-value* {:url url :opts opts}))]
      (f))))

(use-fixtures :each each-fixture)

(deftest test-tracker-with-no-key
  (is (nil? (t/tracker {})))
  (is (nil? (t/tracker nil))))

(deftest test-tracker-with-provided-session-id
  (let [tracker (t/tracker {:key "my-key" :session-id "qwerty"})]
    (is (= "qwerty" (:session-id tracker)))
    (is (= "qwerty" (p/read-cookie "logglytrackingsession")))))

(deftest test-tracker-with-stored-session-id
  (p/write-session-cookie "logglytrackingsession" "1234")
  (is (= "1234" (:session-id (t/tracker {:key "my-key"})))))

(deftest test-tracker-with-no-session-id
  (let [{:keys [session-id session-key]} (t/tracker {:key "my-key"})]
    (is (= session-id (p/read-cookie session-key)))))

(deftest test-log-url
  (testing "with proxy domain"
    (let [tracker (t/tracker {:key "my-key" :use-domain-proxy? true :proxy-route "/my-custom-route"})]
      (t/log tracker "my message")
      (is (= "https://example.com/my-custom-route/inputs/my-key/tag/jslogger" (:url @*post-value*)))

      (t/log tracker {:text "my message" :loggly/tag "tag1,tag2"})
      (is (= "https://example.com/my-custom-route/inputs/my-key/tag/tag1,tag2" (:url @*post-value*)))))

  (testing "with collector domain"
    (let [tracker (t/tracker {:key "my-key" :collector-domain "collector.com"})]
      (t/log tracker "my message")
      (is (= "https://collector.com/inputs/my-key/tag/jslogger" (:url @*post-value*)))

      (t/log tracker {:text "my message" :loggly/tag "tag1,tag2"})
      (is (= "https://collector.com/inputs/my-key/tag/tag1,tag2" (:url @*post-value*)))))

  (testing "with specified protocol"
    (t/log (t/tracker {:key "my-key" :use-domain-proxy? true :proxy-route "/l" :protocol :http}) "msg")
    (is (= "http://example.com/l/inputs/my-key/tag/jslogger" (:url @*post-value*)))

    (t/log (t/tracker {:key "my-key" :protocol :http}) "msg")
    (is (= "http://logs-01.loggly.com/inputs/my-key/tag/jslogger" (:url @*post-value*)))))

(deftest test-log-formats-message
  (let [tracker (t/tracker {:key "my-key"})]
    (t/log tracker "my message")
    (is (= {"text" "my message" "sessionId" (:session-id tracker)}
           (js->clj (.parse js/JSON (get-in @*post-value* [:opts :params])))))

    (t/log tracker {:text "my message" :loggly/tag "tag1,tag2"})
    (is (= {"text" "my message" "sessionId" (:session-id tracker)}
           (js->clj (.parse js/JSON (get-in @*post-value* [:opts :params])))))))
