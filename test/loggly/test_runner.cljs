(ns loggly.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [loggly.tracker-test]))

(enable-console-print!)

(doo-tests 'loggly.tracker-test)
