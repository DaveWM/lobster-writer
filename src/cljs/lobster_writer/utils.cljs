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

(defn words [text]
  (when text
    (vec (re-seq #"[^\s]+" (s/trim text)))))

(defn join-sentences [sentences]
  (s/join " " sentences))


(defn ordered-by [m ordering]
  (->> ordering
       (map #(get m %))
       vec))


(defn swap-elements [xs i1 i2]
  (let [e1 (get xs i1)
        e2 (get xs i2)]
    (-> xs
        (assoc i2 e1)
        (assoc i1 e2))))


(defn move-element [xs x places]
  "Moves the given element x the given number of places in the xs vector.
   If places is positive, moves the element forwards, else backwards."
  (let [idx (.indexOf xs x)]
    (swap-elements xs idx (+ idx places))))


(defn highlight-links [text highlight-fn]
  "Detects and highlights the links in the given text. Returns a vector, where the urls have been run through highlight-fn."
  (let [url-regex #"(https?://[^\s]+)"]
    (->> (s/split text url-regex)
         (map (fn [x]
                (if (re-matches url-regex x)
                  (highlight-fn x)
                  x))))))
