(ns lobster-writer.effects
  (:require [re-frame.core :as rf]
            [lobster-writer.routes :as routes]))

(rf/reg-fx
  ::navigate
  (fn [{:keys [url]}]
    (routes/navigate-to! url)))