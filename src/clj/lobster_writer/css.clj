(ns lobster-writer.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:.ql-editor {:min-height "180px"
                :width "550px"
                :overflow-y "scroll"
                :overflow-x "hidden"}])