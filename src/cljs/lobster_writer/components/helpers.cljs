(ns lobster-writer.components.helpers
  (:require [re-com.core :refer [p v-box]]
            [clojure.string :as s]))


(defn essay-display [section-sentences]
  [:div {:class "essay-display"}
   (->> section-sentences
        (map (fn [sentences]
               ^{:key (hash sentences)}
               [:div {:class "essay-display__section"}
                (->> sentences
                     (partition-by :type)
                     (mapcat (fn [xs]
                               (if (= (:type (first xs)) :sentence)
                                 (let [s (->> xs (map :value) (map s/trim) (s/join " "))]
                                   [[p {:class "essay-display__sentence"} s]])
                                 (->> xs (map #(-> [:pre {:class "essay-display__code"} (:value %)])))))))])))])
