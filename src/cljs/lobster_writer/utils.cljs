(ns lobster-writer.utils
  (:require [lobster-writer.constants :as constants]
            [clojure.string :as s]))

(defn current-essay-path [db]
  [:essays (:current-essay-id db)])

(defn next-step [step]
  (let [step-idx (.indexOf constants/steps step)]
    (get constants/steps (inc step-idx))))

(defn parse-int [s]
  (let [i (js/parseInt s)]
    (when (int? i)
      i)))

(defn displayable-step-name [step-name]
  (->> (name step-name)
       (re-seq #"\w+")
       (map s/capitalize)
       (s/join " ")))