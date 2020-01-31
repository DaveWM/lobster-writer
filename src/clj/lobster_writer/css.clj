(ns lobster-writer.css
  (:require [garden.def :refer [defstyles]]
            [garden.stylesheet :refer [at-keyframes]]))

(defstyles screen
  [:.quill {:width "600px"}]
  [:.ql-toolbar {:min-height "40px"}]
  [:.ql-editor {:min-height "180px"
                :overflow-y "scroll"
                :overflow-x "hidden"}]
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
                   :align-self "center"}])
