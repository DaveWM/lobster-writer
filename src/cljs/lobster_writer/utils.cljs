(ns lobster-writer.utils
  (:require [lobster-writer.constants :as constants]))

(defn current-essay-path [db]
  [:essays (:current-essay-id db)])

(defn next-step [step]
  (let [step-idx (.indexOf constants/steps step)]
    (get constants/steps (inc step-idx))))