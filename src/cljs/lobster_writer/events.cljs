(ns lobster-writer.events
  (:require
    [re-frame.core :as rf]
    [lobster-writer.db :as db]
    [lobster-writer.effects :as effects]
    [lobster-writer.coeffects :as coeffects]
    [lobster-writer.utils :as utils]
    [lobster-writer.interceptors :as interceptors]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [clojure.string :as s]))

(rf/reg-event-fx
  ::initialize-db
  [(rf/inject-cofx ::coeffects/persisted-app-db)]
  (fn-traced [coeffects _]
    {:db (or (::coeffects/persisted-app-db coeffects) db/default-db)}))

(rf/reg-event-db
  ::set-active-page
  [interceptors/persist-app-db]
  (fn-traced [db [_ active-page params]]
    (-> db
        (assoc :active-page active-page)
        (assoc :current-essay-id (:essay-id params)))))


(rf/reg-event-fx
  ::start-new-essay
  [(rf/inject-cofx ::coeffects/id-generator) interceptors/persist-app-db]
  (fn-traced [{:keys [db] :as cfx}]
    (let [essay-id ((::coeffects/id-generator cfx))]
      {:db (-> db
               (assoc-in [:essays essay-id] {:id essay-id
                                             :title (str "New Essay " (inc (count (:essays db))))
                                             :candidate-topics #{}
                                             :current-step :candidate-topics}))
       ::effects/navigate {:url (str "/essays/" essay-id "/candidate-topics")}})))


(rf/reg-event-fx
  ::essay-selected
  [interceptors/persist-app-db]
  (fn-traced [{:keys [db]} [_ essay-id]]
    (let [selected-essay (get-in db [:essays essay-id])]
      {::effects/navigate {:url (str "/essays/" essay-id "/" (name (:current-step selected-essay)))}})))


(rf/reg-event-db
  ::candidate-topic-added
  [interceptors/persist-app-db]
  (fn-traced [db [_ topic-name]]
    (if-not (s/blank? topic-name)
      (update-in db (conj (utils/current-essay-path db) :candidate-topics) conj topic-name)
      db)))


(rf/reg-event-db
  ::candidate-topic-removed
  [interceptors/persist-app-db]
  (fn-traced [db [_ topic-name]]
    (update-in db (conj (utils/current-essay-path db) :candidate-topics) disj topic-name)))
