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
    (vec (re-seq #"(?:\s|^)+[^.!?]+(?:[.!?]|$)" (s/trim text)))))

(defn join-sentences [sentences]
  (s/join sentences))


(defn ordered-by [m ordering]
  (->> ordering
       (map #(get m %))
       vec))


(defn move-element-forwards [xs x places]
  (let [curr-idx (.indexOf xs x)
        new-idx  (+ curr-idx places)]
    (->> (concat (subvec xs 0 curr-idx)
                 (subvec xs (inc curr-idx) (inc new-idx))
                 [x]
                 (subvec xs (inc new-idx)))
         vec)))


(defn move-element-backwards [xs x places]
  (->> (move-element-forwards (vec (reverse xs)) x places)
       reverse
       vec))


(defn move-element [xs x places]
  "Moves the given element x the given number of places in the xs vector.
   If places is positive, moves the element forwards, else backwards."
  (if (neg? places)
    (move-element-backwards xs x (- places))
    (move-element-forwards xs x places)))
