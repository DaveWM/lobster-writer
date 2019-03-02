(ns lobster-writer.components.editable-list
  (:require [re-com.core :as rc]
            [reagent.core :as reagent]))


(defn editable-list [{:keys [on-item-added on-item-removed items]}]
  (let [*input-text (reagent/atom "")]
    [rc/v-box
     :children [[:ul.list-group {:style {:max-width "500px"}}
                 (->> items
                      (map #(-> [:li.list-group-item.list-group-item-active
                                 [rc/h-box
                                  :justify :between
                                  :children [[:span %]
                                             [rc/md-icon-button
                                              :md-icon-name "zmdi-delete" :size :smaller
                                              :on-click (partial on-item-removed %)]]]])))]
                [rc/gap :size "10px"]
                [rc/h-box
                 :children [[rc/input-text
                             :model *input-text
                             :change-on-blur? false
                             :on-change #(reset! *input-text %)
                             :attr {:on-key-press #(when (= 13 (.-which %))
                                                     (on-item-added @*input-text))}]
                            [rc/button :label "Add Item" :on-click #(on-item-added @*input-text)]]]]]))