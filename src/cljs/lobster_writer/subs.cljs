(ns lobster-writer.subs
  (:require
   [re-frame.core :as re-frame]
   [lobster-writer.utils :as utils]))

(re-frame/reg-sub
  ::active-page
  (fn [db _]
   (:active-page db)))


(re-frame/reg-sub
  ::current-essay
  (fn [db _]
    (get-in db (utils/current-essay-path db))))


(re-frame/reg-sub
  ::all-essays
  (fn [db _]
    (:essays db)))


(re-frame/reg-sub
  ::last-saved
  (fn [db _]
    (:last-saved db)))


(re-frame/reg-sub
 ::alerts
 (fn [db _]
   (vals (:alerts db))))


(re-frame/reg-sub
  ::sidebar-open
  (fn [db _]
    (:sidebar-open db)))

(re-frame/reg-sub
  ::remote-storage
  (fn [db _]
    {:available? (:remote-storage-available db)
     :uploading? (:remote-storage-uploading db)
     :downloading? (:remote-storage-downloading db)}))
