(ns lobster-writer.coeffects
  (:require [re-frame.core :as rf]
            [lobster-writer.constants :as constants]))

(rf/reg-cofx
  ::id-generator
  (fn [coeffects _]
    (assoc coeffects ::id-generator (comp str random-uuid))))


(rf/reg-cofx
  ::persisted-app-db
  (fn [coeffects _]
    (assoc coeffects ::persisted-app-db (cljs.reader/read-string (js/localStorage.getItem constants/local-storage-app-db-key)))))
