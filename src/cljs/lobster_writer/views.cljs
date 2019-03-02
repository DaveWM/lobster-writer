(ns lobster-writer.views
  (:require
    [re-frame.core :as re-frame]
    [lobster-writer.subs :as subs]
    [lobster-writer.events :as events]
    [lobster-writer.components.editable-list :refer [editable-list]]
    [re-com.core :refer [button title p v-box h-box gap label line hyperlink-href]]))


;; home

(defn home []
  (let [*all-essays (re-frame/subscribe [::subs/all-essays])]
    [v-box

     :children [[p "Welcome to Lobster Writer, an application to help you write essays based on the advice of Jordan Peterson. "
                 "To start, either start a new essay or select an existing one."]
                [gap :size "25px"]
                [:ul.list-group {:style {:max-width "500px"}}
                 (->> @*all-essays
                      (map val)
                      (map #(-> [:a.list-group-item.list-group-item-active
                                 {:href "#"
                                  :on-click (partial re-frame/dispatch [::events/essay-selected (:id %)])}
                                 (:title %)])))]
                [gap :size "10px"]
                [button :class "btn-primary" :label "Start a new essay" :on-click #(re-frame/dispatch [::events/start-new-essay])]]]))


(defn about []
  [v-box
   :children [[p "Lobster Writer is an application to help you write essays. It is based on the advice of Dr. Jordan Peterson in "
               [hyperlink-href :label "this essay writing guide" :href "https://jordanbpeterson.com/docs/430_docs/Template.docx"]
               ". According to Peterson, this method will help you \"to write an excellent essay from beginning to end\". "
               "Lobster Writer is free (gratis and libre) software - you can find the souce code "
               [hyperlink-href :label "here" :href "https://github.com/DaveWM/lobster-writer"]
               "."]
              [p {:style {:font-weight "bold"}} "Lobster Writer is not associated with Dr. Peterson in any way."]]])


(defn candidate-topics []
  (let [*current-essay (re-frame/subscribe [::subs/current-essay])]
    (if @*current-essay
      [v-box
       :children [[p "This is the first step in writing your essay. List around "
                   [:b "10"]
                   " topics that you would like to write about, or questions that you would like to answer."]
                  [editable-list {:items (:candidate-topics @*current-essay)
                                  :on-item-added #(re-frame/dispatch [::events/candidate-topic-added %])
                                  :on-item-removed #(re-frame/dispatch [::events/candidate-topic-removed %])}]]]
      [p "Essay not found!"])))


;; main

(defn panels [panel-name]
  (case panel-name
    :home [home]
    :about [about]
    :candidate-topics [candidate-topics]
    [:p "Route not found!"]))


(defn main-panel []
  (let [*active-page (re-frame/subscribe [::subs/active-page])]
    [v-box
     :padding "25px"
     :children [[h-box
                 :children [[title :level :level1 :margin-top "2px" :margin-bottom "2px" :label "Lobster Writer"]
                            [gap :size "55px"]
                            [hyperlink-href
                             :label "Home"
                             :href "/"]
                            [gap :size "20px"]
                            [hyperlink-href
                             :label "About"
                             :href "/about"]]
                 :align :center]
                [line]
                [gap :size "15px"]
                [panels @*active-page]]]))
