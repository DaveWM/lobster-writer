(ns lobster-writer.effects
  (:require [re-frame.core :as rf]
            [lobster-writer.routes :as routes]))

(rf/reg-fx
  ::navigate
  (fn [{:keys [url]}]
    (routes/navigate-to! url)))


(rf/reg-fx
  ::show-saving-indicator
  (fn [_]
    (let [elem (.getElementById js/document "saving-indicator")]
      (-> elem .-classList (.remove "run-saving-animation"))
      (js/setTimeout #(-> elem .-classList (.add "run-saving-animation")) 0))))