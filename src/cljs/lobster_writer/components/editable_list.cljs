(ns lobster-writer.components.editable-list
  (:require [re-com.core :as rc]
            [reagent.core :as reagent]))


(defn editable-list [{:keys [on-item-added on-item-removed on-item-moved-up on-item-moved-down label-fn items]
                      :or {label-fn identity}}]
  (let [*input-text (reagent/atom "")]
    [rc/v-box
     :children [[:ul.list-group {:style {:max-width "500px"}}
                 (->> items
                      (map-indexed (fn [idx item]
                                     ^{:key item}
                                     [:li.list-group-item.list-group-item-active
                                      [rc/h-box
                                       :justify :between
                                       :children [[:span {:style {:overflow-x "auto"}}
                                                   (label-fn item)]
                                                  [:span
                                                   (when on-item-removed
                                                     [rc/md-icon-button
                                                      :md-icon-name "zmdi-delete" :size :smaller
                                                      :on-click (partial on-item-removed item)])
                                                   (when on-item-moved-up
                                                     [rc/md-icon-button
                                                      :md-icon-name "zmdi-chevron-up" :size :smaller
                                                      :on-click (partial on-item-moved-up item)
                                                      :disabled? (zero? idx)])
                                                   (when on-item-moved-down
                                                     [rc/md-icon-button
                                                      :md-icon-name "zmdi-chevron-down" :size :smaller
                                                      :on-click (partial on-item-moved-down item)
                                                      :disabled? (= idx (dec (count items)))])]]]])))]
                [rc/gap :size "10px"]
                (when on-item-added
                  [rc/h-box
                   :children [[rc/input-text
                               :model *input-text
                               :change-on-blur? false
                               :on-change #(reset! *input-text %)
                               :attr {:on-key-press #(when (= 13 (.-which %))
                                                       (on-item-added @*input-text))}]
                              [rc/button :label "Add Item" :on-click #(on-item-added @*input-text)]]])]]))
