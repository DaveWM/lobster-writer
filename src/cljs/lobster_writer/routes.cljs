(ns lobster-writer.routes
  (:require
    [bidi.bidi :as bidi]
    [pushy.core :as pushy]
    [goog.events :as gevents]
    [goog.history.EventType :as EventType]
    [re-frame.core :as re-frame]
    [lobster-writer.constants :refer [steps]]))

(def app-routes
  ["/" {"" :home
        "about" :about
        ["essays/" :essay-id] (->> steps
                                   (map (fn [s] [(str "/" (name s)) s]))
                                   (into {}))}])

(defn set-page! [match]
  (re-frame/dispatch [:lobster-writer.events/set-active-page (:handler match) (:route-params match)]))

(def history
  (pushy/pushy set-page! (partial bidi/match-route app-routes)))

(defn start-routing! []
  (pushy/start! history))

(defn navigate-to! [url]
  (pushy/set-token! history url))
