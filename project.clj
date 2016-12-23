(defproject loggly-cljs "0.1.0"
  :description "A Loggly library for ClojureScript"
  :url "https://github.com/tessellator/loggly-cljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.5.2"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.227"]
                 [cljs-ajax "0.5.8"]]

  :global-vars { *warn-on-reflection* true}

  :clean-targets ["target"]

  :profiles {:dev {:plugins [[lein-cljsbuild "1.1.5"]
                             [lein-doo "0.1.7"]]}}

  :cljsbuild
  {:builds [{:id "test"
             :source-paths ["src" "test"]
             :incremental? true
             :compiler {:output-to "target/unit-test.js"
                        :output-dir "target"
                        :main loggly.test-runner
                        :optimizations :none
                        :pretty-print true}}]}

  :aliases {"test-once" ["doo" "phantom" "test" "once"]
            "test-auto" ["doo" "phantom" "test"]
            "test" ["test-once"]})
