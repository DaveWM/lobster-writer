(ns lobster-writer.components.file-chooser)

(defn file-chooser [props button-child]
  (let [id (str (gensym))]
    [:div
     [:input {:id id
              :accept (:accept props)
              :on-change #((:on-change props) (-> % .-target .-files (.item 0)))
              :type "file"
              :style {:display "none"}}]
     [:button.uk-button.uk-button-default
      {:style {:height "100%"}
       :on-click #(let [file-input (.getElementById js/document id)]
                    (.click file-input))}
      button-child]]))
