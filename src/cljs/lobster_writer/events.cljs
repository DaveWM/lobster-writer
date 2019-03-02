(ns lobster-writer.events
  (:require
    [re-frame.core :as rf]
    [lobster-writer.db :as db]
    [lobster-writer.effects :as effects]
    [lobster-writer.coeffects :as coeffects]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(rf/reg-event-db
  ::initialize-db
  (fn-traced [_ _]
    db/default-db))

(rf/reg-event-db
  ::set-active-page
  (fn-traced [db [_ active-page params]]
    (-> db
        (assoc :active-page active-page)
        (assoc :current-essay-id (:essay-id params)))))


(rf/reg-event-fx
  ::start-new-essay
  [(rf/inject-cofx ::coeffects/id-generator)]
  (fn-traced [{:keys [db] :as cfx}]
    (let [essay-id ((::coeffects/id-generator cfx))]
      {:db (-> db
               (assoc :current-essay-id essay-id)
               (assoc-in [:essays essay-id] {:id essay-id}))
       ::effects/navigate {:url (str "/essays/" essay-id "/candidate-topics")}})))
