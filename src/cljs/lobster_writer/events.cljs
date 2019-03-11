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
  (fn [db [_ active-page params]]
    (let [selected-essay (get-in db [:essays (:essay-id params)])]
      (cond-> db
              true (assoc :active-page active-page)
              true (assoc :current-essay-id (:essay-id params))
              selected-essay (assoc-in [:essays (:essay-id params) :current-step] active-page)))))


(rf/reg-event-fx
  ::start-new-essay
  [(rf/inject-cofx ::coeffects/id-generator) interceptors/persist-app-db]
  (fn-traced [{:keys [db] :as cfx}]
    (let [essay-id ((::coeffects/id-generator cfx))]
      {:db (-> db
               (assoc-in [:essays essay-id] {:id essay-id
                                             :title (str "New Essay " (inc (count (:essays db))))
                                             :candidate-topics #{}
                                             :reading-list []
                                             :outline {}
                                             :paragraph-order []
                                             :final-essay ""
                                             :current-step :candidate-topics
                                             :highest-step :candidate-topics}))
       ::effects/navigate {:url (utils/step-url essay-id :candidate-topics)}})))


(rf/reg-event-fx
  ::essay-selected
  [interceptors/persist-app-db]
  (fn-traced [{:keys [db]} [_ essay-id]]
    (let [selected-essay (get-in db [:essays essay-id])]
      {::effects/navigate {:url (utils/step-url essay-id (:current-step selected-essay))}})))


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


(rf/reg-event-db
  ::reading-list-item-added
  [interceptors/persist-app-db]
  (fn-traced [db [_ reading-list-item]]
    (if-not (s/blank? reading-list-item)
      (update-in db (conj (utils/current-essay-path db) :reading-list) concat [reading-list-item])
      db)))


(rf/reg-event-db
  ::reading-list-item-removed
  [interceptors/persist-app-db]
  (fn-traced [db [_ reading-list-item]]
    (update-in db (conj (utils/current-essay-path db) :reading-list) #(remove (partial = reading-list-item) %))))


(rf/reg-event-fx
  ::next-step
  [interceptors/persist-app-db]
  (fn-traced [{:keys [db]} _]
    (let [current-essay (get-in db (utils/current-essay-path db))
          next-step     (utils/next-step (:current-step current-essay))]
      {::effects/navigate {:url (utils/step-url (:id current-essay) next-step)}
       :db (if (utils/step-after? next-step (:highest-step current-essay))
             (assoc-in db (conj (utils/current-essay-path db) :highest-step) next-step)
             db)})))


(rf/reg-event-db
  ::topic-selected
  [interceptors/persist-app-db]
  (fn-traced [db [_ topic]]
    (assoc-in db (conj (utils/current-essay-path db) :title) topic)))


(rf/reg-event-db
  ::essay-target-length-changed
  [interceptors/persist-app-db]
  (fn-traced [db [_ essay-length]]
    (if (< 0 essay-length)
      (assoc-in db (conj (utils/current-essay-path db) :target-length) essay-length)
      (assoc-in db (conj (utils/current-essay-path db) :target-length) nil))))


(rf/reg-event-db
  ::outline-heading-added
  [interceptors/persist-app-db]
  (fn-traced [db [_ outline-heading]]
    (if-not (s/blank? outline-heading)
      (-> db
          (assoc-in (conj (utils/current-essay-path db) :outline outline-heading) {:heading outline-heading
                                                                                   :paragraph {}
                                                                                   :sentences {:v1 []
                                                                                               :v2 []}})
          (update-in (conj (utils/current-essay-path db) :paragraph-order) conj outline-heading))
      db)))


(rf/reg-event-db
  ::outline-heading-removed
  [interceptors/persist-app-db]
  (fn-traced [db [_ outline-heading]]
    (-> db
        (update-in (conj (utils/current-essay-path db) :outline) dissoc outline-heading)
        (update-in (conj (utils/current-essay-path db) :paragraph-order) #(remove (partial = outline-heading) %)))))


(rf/reg-event-db
  ::outline-paragraph-updated
  [interceptors/persist-app-db]
  (fn-traced [db [_ heading updated-paragraph]]
    (-> db
        (assoc-in (conj (utils/current-essay-path db) :outline heading :paragraph :v1) updated-paragraph)
        (assoc-in (conj (utils/current-essay-path db) :outline heading :sentences :v1) (->> (utils/sentences updated-paragraph)
                                                                                            vec)))))
(defn update-paragraph-from-sentences [version section]
  (let [paragraph (->> (get-in section [:sentences version])
                       utils/join-sentences)]
    (-> section
        (assoc-in [:paragraph version] paragraph))))

(rf/reg-event-db
  ::sentence-rewritten
  [interceptors/persist-app-db]
  (fn-traced [db [_ heading idx updated-sentence]]
    (-> db
        (assoc-in (conj (utils/current-essay-path db) :outline heading :sentences :v2 idx) updated-sentence)
        (update-in (conj (utils/current-essay-path db) :outline heading) #(update-paragraph-from-sentences :v2 %)))))


(rf/reg-event-db
  ::sentence-moved-up
  [interceptors/persist-app-db]
  (fn-traced [db [_ heading moved-sentence]]
    (-> db
        (update-in (conj (utils/current-essay-path db) :outline heading :sentences :v2)
                   (fn [sentences]
                     (utils/move-element sentences moved-sentence -1)))
        (update-in (conj (utils/current-essay-path db) :outline heading) #(update-paragraph-from-sentences :v2 %)))))


(rf/reg-event-db
  ::sentence-moved-down
  [interceptors/persist-app-db]
  (fn-traced [db [_ heading moved-sentence]]
    (-> db
        (update-in (conj (utils/current-essay-path db) :outline heading :sentences :v2)
                   (fn [sentences]
                     (utils/move-element sentences moved-sentence 1)))
        (update-in (conj (utils/current-essay-path db) :outline heading) #(update-paragraph-from-sentences :v2 %)))))


(rf/reg-event-db
  ::paragraph-moved-up
  [interceptors/persist-app-db]
  (fn-traced [db [_ moved-section]]
    (update-in db (conj (utils/current-essay-path db) :paragraph-order)
               (fn [ordering]
                 (utils/move-element ordering (:heading moved-section) -1)))))


(rf/reg-event-db
  ::paragraph-moved-down
  [interceptors/persist-app-db]
  (fn-traced [db [_ moved-section]]
    (update-in db (conj (utils/current-essay-path db) :paragraph-order)
               (fn [ordering]
                 (utils/move-element ordering (:heading moved-section) 1)))))


(rf/reg-event-db
  ::second-outline-heading-added
  [interceptors/persist-app-db]
  (fn-traced [db [_ outline-heading]]
    (if-not (s/blank? outline-heading)
      (-> db
          (assoc-in (conj (utils/current-essay-path db) :second-outline outline-heading) {:heading outline-heading
                                                                                          :paragraph nil}))
      db)))


(rf/reg-event-db
  ::second-outline-heading-removed
  [interceptors/persist-app-db]
  (fn-traced [db [_ outline-heading]]
    (-> db
        (update-in (conj (utils/current-essay-path db) :second-outline) dissoc outline-heading))))


(rf/reg-event-db
  ::second-outline-paragraph-updated
  [interceptors/persist-app-db]
  (fn-traced [db [_ heading updated-paragraph]]
    (-> db
        (assoc-in (conj (utils/current-essay-path db) :second-outline heading :paragraph) updated-paragraph)
        (update-in (utils/current-essay-path db) (fn [current-essay]
                                                   (let [final-essay (->> (:second-outline current-essay)
                                                                          vals
                                                                          (map :paragraph)
                                                                          (map (partial str "     "))
                                                                          (s/join "\n\n"))]
                                                     (assoc current-essay :final-essay final-essay)))))))


(rf/reg-event-db
  ::final-essay-updated
  [interceptors/persist-app-db]
  (fn-traced [db [_ updated-final-essay]]
    (-> db
        (assoc-in (conj (utils/current-essay-path db) :final-essay) updated-final-essay))))