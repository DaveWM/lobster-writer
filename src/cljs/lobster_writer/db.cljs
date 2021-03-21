(ns lobster-writer.db)

(def default-db
  {:active-page nil
   :current-essay-id nil
   :essays {}
   :last-saved nil
   ::version 2
   :alerts {}
   :remote-storage-available false
   :remote-storage-uploading false
   :remote-storage-downloading false})
