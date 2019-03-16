(ns lobster-writer.components.helpers
  (:require [re-com.core :refer [p v-box]]
            [lobster-writer.styles :as styles]))


(defn essay-display [paragraphs]
  [v-box
   :style {:background-color styles/light-gray
           :border-radius "5px"
           :padding "10px"}
   :children (->> paragraphs
                  (map #(-> ^{:key %} [p {:style {:text-indent "20px"}} %])))])
