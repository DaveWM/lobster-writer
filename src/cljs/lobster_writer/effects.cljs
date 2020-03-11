(ns lobster-writer.effects
  (:require [re-frame.core :as rf]
            [lobster-writer.routes :as routes]
            [cljsjs.filesaverjs]
            [day8.re-frame.http-fx]))

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


(rf/reg-fx
  ::save-file
  (fn [{:keys [content type name]}]
    (-> (js/Blob. #js [content] #js {:type (str type ";charset=utf-8")})
        (js/saveAs name))))


(rf/reg-fx
  ::read-file
  (fn [{:keys [on-success file]}]
    (let [fr (js/FileReader.)]
      (set! (.-onload fr) #(on-success (.-result fr)))
      (.readAsText fr file))))


(rf/reg-fx
  ::open-dialog
  (fn [{:keys [title body]}]
    (let [dialog (js/window.open "" title "height=600,width=800,toolbar=no,personalbar=no")]
      (do (-> dialog
              (.-document)
              (.-body)
              (.-innerHTML)
              (set! body))
          (-> dialog
              (.-document)
              (.-title)
              (set! title))))))

(rf/reg-fx
 ::open-external-url
 (fn [{:keys [url new-tab]}]
   (if new-tab
     (js/window.open url)
     (set! js/window.location.href url))))

(rf/reg-fx
 ::show-alert
 (fn [{:keys [text]}]
   (js/alert text)))