(ns lobster-writer.remote-storage)

(def remote-storage (js/LWRemoteStorage. (clj->js {:cache false})))
(def client (.scope remote-storage "/lobster-writer/"))

(defn save! [path data]
  (.storeFile client "application/edn" path (pr-str data)))

(defn get! [path]
  (.getFile client path false))

(defn get-all! [path]
  (-> (.getListing client path false)
      (.then (fn [listing]
               (->> (js->clj listing)
                    keys
                    (map get!)
                    (js/Promise.all))))
      (.then (fn [files]
               (remove nil? files)))))

(defn disconnect! []
  (.disconnect remote-storage))

(defn init-remote-storage! [on-connected on-disconnected]
  (-> remote-storage
      (.setApiKeys (clj->js {"dropbox" "m43cbo3wkoiuhl4"
                             "googledrive" "361048255604-033g8c6ves7onapah838qsfe2vdips13.apps.googleusercontent.com"})))
  (-> remote-storage
      (.-access)
      (.claim "lobster-writer" "rw"))

  (.on remote-storage "connected" (fn []
                                    (on-connected)))

  (.on remote-storage "disconnected" (fn []
                                       (on-disconnected))))
