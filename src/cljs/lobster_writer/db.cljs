(ns lobster-writer.db)

(def default-db
  {:active-page nil
   :current-essay-id nil
   :essays {}
   :last-saved nil
   ::version 2})
