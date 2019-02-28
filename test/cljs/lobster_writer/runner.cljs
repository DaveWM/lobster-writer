(ns lobster-writer.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [lobster-writer.core-test]))

(doo-tests 'lobster-writer.core-test)
