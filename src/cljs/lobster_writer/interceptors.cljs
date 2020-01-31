(ns lobster-writer.interceptors
  (:require [lobster-writer.constants :as constants]))

(def persist-app-db
  (re-frame.core/->interceptor
    :id :persist-app-db
    :after (fn [context]
               (when-let [db (get-in context [:effects :db])]
                 (js/localStorage.setItem constants/local-storage-app-db-key (prn-str db))
                 (-> context
                     (assoc-in [:effects :lobster-writer.effects/show-saving-indicator] true)
                     (assoc-in [:effects :db :last-saved] (js/Date.)))))))