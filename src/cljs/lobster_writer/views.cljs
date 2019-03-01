(ns lobster-writer.views
  (:require
    [re-frame.core :as re-frame]
    [lobster-writer.subs :as subs]
    [cljsjs.react-quill]
    [re-com.core :refer [button title p v-box h-box gap label line hyperlink-href]]))


;; home

(defn home-panel []
  [v-box
   :padding "25px"
   :children [[h-box
               :children [[title :level :level1 :margin-top "2px" :margin-bottom "2px" :label "Lobster Writer"]
                          [gap :size "55px"]
                          [hyperlink-href
                           :label "About"
                           :href "#/about"]]
               :align :center]
              [line]
              [gap :size "15px"]
              [p "Welcome to Lobster Writer, an application to help you write essays based on the advice of Jordan Peterson. "
               "To start, either start a new essay or select an existing one."]
              [gap :size "25px"]
              [:ul.list-group {:style {:max-width "500px"}}
               (->> ["Does evil exist?" "Are all cultures equally worthy of respect?" "What, if anything, makes a person good?"]
                    (map #(-> [:li.list-group-item.list-group-item-active %])))
               ]
              [gap :size "10px"]
              [button :class "btn-primary" :label "Start a new essay" :on-click #(re-frame/dispatch [:start-new-essay])]]
   ])


;; about

(defn about-panel []
  [v-box
   :padding "25px"
   :children [[title :level :level1 :margin-top "2px" :margin-bottom "2px" :label "About" :underline? true]
              [gap :size "15px"]
              [p "Lobster Writer is an application to help you write essays. It is based on the advice of Dr. Jordan Peterson in "
               [hyperlink-href :label "this essay writing guide" :href "https://jordanbpeterson.com/docs/430_docs/Template.docx"]
               ". According to Peterson, this method will help you \"to write an excellent essay from beginning to end\". "
               "Lobster Writer is free (gratis and libre) software - you can find the souce code "
               [hyperlink-href :label "here" :href "https://github.com/DaveWM/lobster-writer"]
               "."]
              [p {:style {:font-weight "bold"}} "Lobster Writer is not associated with Dr. Peterson in any way."]]])


;; main

(defn panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:p "Route not found!"]))


(defn main-panel []
  (let [active-page (re-frame/subscribe [::subs/active-page])]
    [panels @active-page]))
