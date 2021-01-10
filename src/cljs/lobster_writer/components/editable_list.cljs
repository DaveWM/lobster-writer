(ns lobster-writer.components.editable-list
  (:require [reagent.core :as reagent]
            [lobster-writer.utils :as utils]))


(defn editable-list []
  (let [*input-text (reagent/atom "")]
    (fn [{:keys [on-item-added on-item-removed on-item-moved-up on-item-moved-down label-fn items]
          :or {label-fn identity}}]
      [:div.editable-list
       (when (seq items)
         [:ul.uk-list.uk-list-striped
          (->> items
               (map-indexed (fn [idx item]
                              ^{:key item}
                              [:li.uk-flex.uk-flex-row.uk-flex-between.uk-flex-middle
                               [:span.editable-list__label
                                (-> (label-fn item)
                                    (utils/highlight-links (fn [url]
                                                             [:a
                                                              {:href url
                                                               :target "_blank"}
                                                              url])))]
                               [:span.editable-list__actions
                                (when on-item-removed
                                  [:button.uk-button.uk-button-default.uk-button-rounded.uk-button-small
                                   {:on-click (partial on-item-removed item)}
                                   [:i.zmdi.zmdi-delete]])
                                (when on-item-moved-up
                                  [:button.uk-button.uk-button-default.uk-button-rounded.uk-button-small
                                   {:on-click (partial on-item-moved-up item)
                                    :disabled (zero? idx)}
                                   [:i.zmdi.zmdi-chevron-up]])
                                (when on-item-moved-down
                                  [:button.uk-button.uk-button-default.uk-button-rounded.uk-button-small
                                   {:on-click (partial on-item-moved-down item)
                                    :disabled (= idx (dec (count items)))}
                                   [:i.zmdi.zmdi-chevron-down]])]])))])
       (when on-item-added
         ^{:key :input}
         [:div.uk-flex.uk-flex-row
          [:input.uk-input
           {:value @*input-text
            :on-change #(reset! *input-text (-> % .-target .-value))
            :on-key-press #(when (= 13 (.-which %))
                             (do (on-item-added @*input-text)
                                 (reset! *input-text "")))}]
          [:button.uk-button.uk-button-default
           {:on-click #(do (on-item-added @*input-text)
                           (reset! *input-text ""))}
           "Add"]])])))
