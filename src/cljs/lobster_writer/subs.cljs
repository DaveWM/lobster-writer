(ns lobster-writer.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
  ::active-page
  (fn [db _]
   (:active-page db)))
