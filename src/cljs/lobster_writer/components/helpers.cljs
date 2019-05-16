(ns lobster-writer.components.helpers
  (:require [re-com.core :refer [p v-box]]
            [lobster-writer.styles :as styles]))


(defn essay-display [paragraphs]
  [:div {:style {:background-color styles/light-gray
                 :border-radius "5px"
                 :padding "10px"
                 :max-width "650px"}}
   (->> paragraphs
        (map #(-> ^{:key %} [p {:style {:text-indent "20px"}} %])))])
