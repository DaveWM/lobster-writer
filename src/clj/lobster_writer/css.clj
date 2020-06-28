(ns lobster-writer.css
  (:require [garden.def :refer [defstyles]]
            [garden.stylesheet :refer [at-keyframes]]))

(def light-gray "#e1e8f0")

(defstyles screen
  [:.quill {:width "600px"
            :min-height "250px"
            :display "flex"
            :flex-direction "column"}]
  [:.ql-toolbar {:min-height "40px"}]
  [:.ql-editor {:flex 1
                :overflow-y "scroll"
                :overflow-x "hidden"}]
  [:.ql-container {:flex 1
                   :display "flex"
                   :flex-direction "column"}]
  [:.fix-size {:flex "1 !important"}]
  (apply at-keyframes "saving-animation"
                (->> (map vector (range 0 (inc 100) 25) (cycle [0 1]))
                     (map (fn [[percentage-done opacity]]
                            [(str percentage-done "%") {:opacity opacity}]))))
  [:.run-saving-animation {:animation "saving-animation 2s linear"}]
  [:#saving-indicator {:position "fixed"
                       :right "60px"
                       :bottom "20px"
                       :opacity "0"}]
  [:.progress {:margin-bottom "0 !important"}]
  [:.rc-progress-bar-wrapper {:width "100%"}]
  [:.lw-container {:max-width "1000px"
                   :width "100%"
                   :align-self "center"}]
  [:.essay-display {:border "1px solid black"
                    :border-radius "8px"
                    :padding "15px"
                    :max-width "650px"}
   [:&__section {:margin-bottom "25px"}
    [:&:last-child {:margin-bottom "0"}]]
   [:&__sentence {:text-indent "20px"}]
   [:&__code {:margin-left "20px"}]]
  [:.sidebar {:background-color light-gray
              :padding "20px"
              :border-radius "8px"}]
  [:.dm-logo
   [:img {:max-height "30px"}]])
