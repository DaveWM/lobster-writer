(ns lobster-writer.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [lobster-writer.events :as events]
   [lobster-writer.routes :as routes]
   [lobster-writer.views :as views]
   [lobster-writer.config :as config]
   [lobster-writer.remote-storage :as rs]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/start-routing!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root)
  (rs/init-remote-storage!
    #(re-frame/dispatch [:lobster-writer.events/remote-storage-available])
    #(re-frame/dispatch [::events/remote-storage-log-out])))
