(ns lobster-writer.views
  (:require
    [re-frame.core :as re-frame]
    [lobster-writer.subs :as subs]
    [react-quill :refer [ReactQuill]]))


;; home

(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "Lobster Writer"]
     [:p "Welcome to Lobster Writer, an application to help you write essays."]
     [:div
      [:a {:href "#/about"}
       "go to About Page"]]
     ]))


;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]
   [ReactQuill]

   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])


;; main

(defn panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:p "Route not found!"]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-page (re-frame/subscribe [::subs/active-page])]
    [panels @active-page]))
