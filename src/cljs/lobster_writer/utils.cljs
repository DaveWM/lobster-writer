(ns lobster-writer.utils)

(defn current-essay-path [db]
  [:essays (:current-essay-id db)])