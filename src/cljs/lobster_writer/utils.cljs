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

(defn percentage-complete [current-step]
  (let [step-idx (.indexOf constants/steps current-step)]
    (-> (* 100 (/ (inc step-idx) (count constants/steps)))
        (js/Math.floor))))

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
    (let [acronym-regex #"(?:[a-zA-Z]\.)+[a-zA-Z]\.?"
          acronym-replacement (str (gensym))
          chunks (->> (s/split (s/trim text) #"```")
                      (map (fn [type v] {:type type
                                         :value (s/trim v)})
                           (cycle [:text :code]))
                      (remove #(s/blank? (:value %))))]
      (->> chunks
           (mapcat (fn [chunk]
                     (if (= (:type chunk) :text)
                       (as-> (:value chunk) $
                             (s/replace $ acronym-regex #(s/replace % #"\." acronym-replacement))
                             (re-seq #"(?:\s|^)+[^.!?]+(?:[.!?]|$)" $)
                             (map #(s/replace % (re-pattern acronym-replacement) ".") $)
                             (map #(-> {:type :sentence :value (s/trim %)}) $))
                       [chunk])))
           vec))))

(defn words [text]
  (when text
    (vec (re-seq #"[^\s]+" (s/trim text)))))

(defn mask-code [{:keys [type] :as chunk}]
  (if (= type :code)
    (assoc chunk :value " <<code block>> ")
    chunk))

(defn join-sentences
  [sentences]
  (->> sentences
       (map (fn [{:keys [type value]}]
              (if (= type :code)
                (str "\n\n" value "\n\n")
                value)))
       (s/join " ")))


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


(defn map-vals [f m]
  (->> m
       (map (fn [[k v]]
              [k (f v)]))
       (into {})))

(defn ->form-data [m]
  (let [form-data (js/FormData.)]
    (doseq [[k v] m]
      (.append form-data (name k) (str v)))
    form-data))