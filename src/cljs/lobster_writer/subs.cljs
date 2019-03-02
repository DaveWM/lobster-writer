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
