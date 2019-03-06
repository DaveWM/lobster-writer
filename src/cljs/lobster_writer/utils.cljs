(ns lobster-writer.utils
  (:require [lobster-writer.constants :as constants]
            [clojure.string :as s]))

(defn current-essay-path [db]
  [:essays (:current-essay-id db)])

(defn next-step [step]
  (let [step-idx (.indexOf constants/steps step)]
    (get constants/steps (inc step-idx))))

(defn step-before-or-equal? [step1 step2]
  (<= (.indexOf constants/steps step1)
      (.indexOf constants/steps step2)))

(defn step-after? [step1 step2]
  (> (.indexOf constants/steps step1)
     (.indexOf constants/steps step2)))

(defn parse-int [s]
  (let [i (js/parseInt s)]
    (when (int? i)
      i)))

(defn displayable-step-name [step-name]
  (->> (name step-name)
       (re-seq #"\w+")
       (map s/capitalize)
       (s/join " ")))

(defn step-url [essay-id step]
  (str "/essays/" essay-id "/" (name step)))

(defn sentences [text]
  (when text
    (re-seq #"(?:\s|^)+[^.!?]+(?:[.!?]|$)" (s/trim text))))
