(ns lobster-writer.css
  (:require [garden.def :refer [defstyles]]
            [garden.stylesheet :refer [at-keyframes]]
            [garden.color :as gc]))

(def light-gray "#e1e8f0")

(def palette-1 "#0072BB")
(def palette-2 "#1B3B6F")
(def palette-3 "0B132B")
(def palette-4 "#B9D6F2")
(def palette-5 "#00ABE7")

(defstyles screen
  [:.quill {:min-height "250px"
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
                       :right "20px"
                       :bottom "20px"
                       :opacity "0"}]
  [:.progress {:margin-bottom "0 !important"}]
  [:.rc-progress-bar-wrapper {:width "100%"}]
  [:.essay-display
   [:&__section {:margin-bottom "25px"}
    [:&:last-child {:margin-bottom "0"}]]
   [:&__sentence {:text-indent "20px"}]
   [:&__code {:margin-left "20px"}]]
  [:.sidebar {:background-color palette-2
              :padding "5px"
              :margin "0"
              :border-radius "8px"
              :flex 1}
   [:&__header {:color "white"}]
   [:&__list {:display :flex
              :flex-direction :column
              :padding-left "10px"
              :border-left [["3px" "solid" palette-1]]}]
   [:&__step {:color "white"
              :margin-bottom "15px"
              :border-radius "8px"}
    [:&:hover {:color "white"}]
    [:&--active {:color [[palette-1 "!important"]]}
     [:&:hover {:color [[palette-1 "!important"]]}]]
    [:&--disabled {:opacity 0.5}
     [:&:hover {:text-decoration "none"
                :color "white"
                :cursor "default"}]]]]
  [:.step {:display :flex
           :flex-direction :column
           :align-items :stretch}]
  [:.dm-logo
   [:img {:max-height "20px"}]]
  [:#app-bar {:background-color palette-2
              :color "white"}
   [:.app-bar__title {:font-size "40px"
                      :font-weight 200
                      :color "white"
                      :padding-right "12px"
                      :border-bottom "1px solid rgb(255,255,255,0.6)"}]]
  [:.next-step {:margin-top "15px !important"}]
  [:.essay__header {:margin-bottom "15px"}]
  [:.topic-selection
   [:&__item {:padding "15px"
              :cursor "pointer"
              :border-left [["2px" "solid" "transparent"]]}
    [:&--selected {:border-left [["2px" "solid" palette-1]]}]
    [:&:hover {:border-left [["2px" "solid" palette-5]]}]]])
