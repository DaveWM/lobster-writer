(ns lobster-writer.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:.quill {:display "contents"}]
  [:.ql-toolbar {:min-height "40px"}]
  [:.ql-editor {:min-height "180px"
                :width      "550px"
                :overflow-y "scroll"
                :overflow-x "hidden"}]
  [:.fix-size {:flex "1 !important"}])
