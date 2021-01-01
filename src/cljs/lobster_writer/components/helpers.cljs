(ns lobster-writer.components.helpers
  (:require [re-com.core :refer [p v-box]]
            [clojure.string :as s]))


(defn essay-display [section-sentences essay-title]
  [:div.uk-card.uk-card-body.uk-card-default.uk-card-large.essay-display
   (when essay-title [:h4.uk-card-title essay-title])
   (->> section-sentences
        (map (fn [sentences]
               ^{:key (hash sentences)}
               [:div.essay-display__section
                (->> sentences
                     (partition-by :type)
                     (mapcat (fn [xs]
                               (if (= (:type (first xs)) :sentence)
                                 (let [s (->> xs (map :value) (map s/trim) (s/join " "))]
                                   [[p {:class "essay-display__sentence"} s]])
                                 (->> xs (map #(-> [:pre {:class "essay-display__code"} (:value %)])))))))])))])

(defn next-step [props]
  [:button.uk-button.uk-button-primary.next-step
   props
   "Next Step"])
