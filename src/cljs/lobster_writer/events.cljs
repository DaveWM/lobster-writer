(ns lobster-writer.events
  (:require
    [re-frame.core :as rf]
    [lobster-writer.db :as db]
    [lobster-writer.effects :as effects]
    [lobster-writer.coeffects :as coeffects]
    [lobster-writer.utils :as utils]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [clojure.string :as s]))

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
               (assoc-in [:essays essay-id] {:id essay-id
                                             :title (str "New Essay " (inc (count (:essays db))))
                                             :candidate-topics #{}
                                             :current-step :candidate-topics}))
       ::effects/navigate {:url (str "/essays/" essay-id "/candidate-topics")}})))


(rf/reg-event-fx
  ::essay-selected
  (fn-traced [{:keys [db]} [_ essay-id]]
    (let [selected-essay (get-in db [:essays essay-id])]
      {::effects/navigate {:url (str "/essays/" essay-id "/" (name (:current-step selected-essay)))}})))


(rf/reg-event-db
  ::candidate-topic-added
  (fn-traced [db [_ topic-name]]
    (if-not (s/blank? topic-name)
      (update-in db (conj (utils/current-essay-path db) :candidate-topics) conj topic-name)
      db)))


(rf/reg-event-db
  ::candidate-topic-removed
  (fn-traced [db [_ topic-name]]
    (update-in db (conj (utils/current-essay-path db) :candidate-topics) disj topic-name)))
