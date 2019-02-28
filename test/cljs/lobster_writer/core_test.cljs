(ns lobster-writer.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [lobster-writer.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
