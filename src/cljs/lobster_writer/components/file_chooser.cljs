(ns lobster-writer.components.file-chooser
  (:require [re-com.core :refer [button]]))

(defn file-chooser [props button-child]
  (let [id (str (gensym))]
    [:div
     [:input {:id id
              :accept (:accept props)
              :on-change #((:on-change props) (-> % .-target .-files (.item 0)))
              :type "file"
              :style {:display "none"}}]
     [button
      :on-click #(let [file-input (.getElementById js/document id)]
                   (.click file-input))
      :label button-child]]))
