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
  [:.essay-display {:border "1px solid black"
                    :border-radius "8px"
                    :padding "15px"
                    :max-width "650px"}
   [:&__section {:margin-bottom "25px"}
    [:&:last-child {:margin-bottom "0"}]]
   [:&__sentence {:text-indent "20px"}]
   [:&__code {:margin-left "20px"}]]
  [:ul.sidebar {:background-color (gc/lighten palette-1 60)
              :padding "15px"
              :margin "0 20px"
              :border-radius "8px"}]
  [:.dm-logo
   [:img {:max-height "26px"}]]
  [:#app-bar {:background-color palette-2
              :color "white"}])
