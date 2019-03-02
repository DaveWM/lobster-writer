(ns lobster-writer.coeffects
  (:require [re-frame.core :as rf]))

(rf/reg-cofx
  ::id-generator
  (fn [coeffects _]
    (assoc coeffects ::id-generator random-uuid)))
