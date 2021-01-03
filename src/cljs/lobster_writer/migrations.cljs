(ns lobster-writer.migrations
  (:require [lobster-writer.utils :as u]))

(defmulti update-db (fn [db]
                      (:lobster-writer.db/version db)))

(defmethod update-db 1 [db]
  (update db
          :essays
          (fn [essays]
            (->> essays
                 (u/map-vals (fn [essay]
                               (-> essay
                                   (update
                                     :outline
                                     (fn [outline]
                                       (->> outline
                                            (u/map-vals (fn [v]
                                                          (let [update-sentence #(-> {:type :sentence
                                                                                      :value %})]
                                                            (-> v
                                                                (update-in [:sentences :v1] (partial mapv update-sentence))
                                                                (update-in [:sentences :v2] (partial mapv update-sentence)))))))))
                                   (assoc :notes-type (or (:notes-type essay) :in-app)))))))))

(defmethod update-db 2 [db]
  (update db
          :essays
          (partial u/map-vals (fn [e]
                                (assoc e :second-paragraph-order (vec (keys (:second-outline e))))))))

(defmethod update-db :default [db]
  nil)

(defn migrate [db]
  (if-let [updated-db (-> db
                          (assoc :lobster-writer.db/version (or (:lobster-writer.db/version db) 1))
                          (update-db))]
    (-> updated-db
        (update :lobster-writer.db/version inc)
        recur)
    db))