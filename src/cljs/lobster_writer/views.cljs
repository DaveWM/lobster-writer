(ns lobster-writer.views
  (:require
    [re-frame.core :as re-frame]
    [lobster-writer.subs :as subs]
    [lobster-writer.events :as events]
    [lobster-writer.components.editable-list :refer [editable-list]]
    [lobster-writer.utils :as utils]
    [lobster-writer.constants :as constants]
    [lobster-writer.components.helpers :refer [essay-display]]
    [re-com.core :refer [progress-bar button title p v-box h-box gap label line hyperlink-href hyperlink input-text h-split v-split input-textarea box scroller md-icon-button md-circle-icon-button radio-button modal-panel alert-box]]
    [clojure.string :as s]
    [reagent.core :as r]
    [cljsjs.prop-types]
    [cljsjs.react-quill]
    [cljs-time.core :as t]
    [cljs-time.format :as tf]
    [cljs-time.coerce :as tc]
    [lobster-writer.components.file-chooser :refer [file-chooser]]
    [clojure.string :as str]))


(def react-quill (r/adapt-react-class js/ReactQuill))

(defn quill [props]
  [react-quill (assoc props
                 :modules {:toolbar [[{:header [1 2 3 false]}]
                                     ["bold" "italic" "underline" "strike" "blockquote"]
                                     [{:list "ordered"} {:list "bullet"} {:indent "-1"} {:indent "+1"}]
                                     ["link"]
                                     ["clean"]
                                     ["code-block"]]})])


;; home

(defn home []
  (let [*all-essays (re-frame/subscribe [::subs/all-essays])]
    [:div
     [:div
      (->> @*all-essays
           (map val)
           (map (fn [essay]
                  ^{:key (:id essay)}
                  [:div.uk-card.uk-card-body.uk-card-default.uk-margin
                   [:a {:href "#"
                        :style {:display "flex"
                                :justify-content "space-between"
                                :align-items "center"}
                        :on-click (partial re-frame/dispatch [::events/essay-selected (:id essay)])}
                    [:div.uk-flex.uk-flex-column.uk-flex-1
                     [:h3 (:title essay)]
                     [:progress.uk-progress
                      {:value (utils/percentage-complete (:highest-step essay))
                       :max "100"}]]
                    [:div.uk-margin-left.uk-margin-right
                     [:button.uk-button.uk-button-default.uk-button-small
                      {:tooltip "Export"
                       :on-click (fn [evt]
                                   (re-frame/dispatch [::events/export-requested (:id essay)])
                                   (.stopPropagation evt))}
                      [:i {:class "zmdi zmdi-download"}]]]]])))]
     [:div.uk-flex
      [:button.uk-button.uk-button-primary {:on-click #(re-frame/dispatch [::events/start-new-essay])} "New Essay"]
      [file-chooser {:accept ".edn"
                     :on-change #(re-frame/dispatch [::events/import-requested %])}
       [:span "Import " [:i.zmdi.zmdi-hc-fw-rc.zmdi-upload]]]]]))


(defn about []
  [:div
   [:p "Lobster Writer is an application to help you write essays. It is based on the advice of Dr. Jordan Peterson in "
    [:a {:href "/Jordan-Peterson-Writing-Template.docx"} "this essay writing guide"]
    ". According to Dr. Peterson, this method will help you \"to write an excellent essay from beginning to end\". "]
   [:p
    "Lobster Writer is free (both gratis and libre) software - you can find the souce code "
    [:a {:href "https://github.com/DaveWM/lobster-writer"} "here"]
    ". If you would like to support me in developing Lobster Writer, please follow the link below."]

   [:p.uk-text-bolder "Lobster Writer is not associated with Dr. Peterson in any way."]])


(defn candidate-topics [current-essay]
  [:div
   [p "This is the first step in writing your essay. List around "
    [:b "10"]
    " topics that you would like to write about, or questions that you would like to answer."]
   [editable-list {:items (:candidate-topics current-essay)
                   :on-item-added #(re-frame/dispatch [::events/candidate-topic-added %])
                   :on-item-removed #(re-frame/dispatch [::events/candidate-topic-removed %])}]
   [:button.uk-button.uk-button-primary
    {:disabled (empty? (:candidate-topics current-essay))
     :on-click #(re-frame/dispatch [::events/next-step])}
    "Next Step"]])


(defn reading-list [current-essay]
  [v-box
   :children [[p
               "Great, you've thought of some potential topics to write about. Now you need to find some books or articles to read. "
               "You should read around " [:b "5 to 10 books per 1000 words of essay."] " List them here."]
              [editable-list {:items (:reading-list current-essay)
                              :on-item-added #(re-frame/dispatch [::events/reading-list-item-added %])
                              :on-item-removed #(re-frame/dispatch [::events/reading-list-item-removed %])}]
              [gap :size "20px"]
              [p
               "You can also make some notes if you want. "
               "One good way to make notes is to read a small section at a time, then write down what you have learned and any questions that you have. "]
              [p
               "Where do you want to store the notes?"]
              [h-box
               :children [[gap :size "25px"]
                          [radio-button
                           :model (:notes-type current-essay)
                           :value :in-app
                           :label "here"
                           :on-change (fn [v]
                                        (re-frame/dispatch [::events/notes-type-updated v]))]
                          [gap :size "15px"]
                          [radio-button
                           :model (:notes-type current-essay)
                           :value :external
                           :label "in another app"
                           :on-change (fn [v]
                                        (re-frame/dispatch [::events/notes-type-updated v]))]]]
              [gap :size "15px"]
              (if (= (:notes-type current-essay) :in-app)
                [quill {:default-value (:notes current-essay)
                        :on-change (fn [html _ _ editor]
                                     (re-frame/dispatch [::events/in-app-notes-updated html (.call (aget editor "getText"))]))}]
                [input-text
                 :model (:external-notes-url current-essay)
                 :on-change #(re-frame/dispatch [::events/external-notes-url-updated %])
                 :placeholder "Enter the URL of your notes here"])
              [gap :size "15px"]
              [button
               :disabled? (empty? (:reading-list current-essay)) :class "btn-primary" :label "Next Step"
               :on-click #(re-frame/dispatch [::events/next-step])]]])


(defn topic-choice [current-essay]
  [v-box
   :children [[p "You now need to choose your topic, and the length your essay will be. "]
              (when-not (contains? (:candidate-topics current-essay) (:title current-essay))
                [p {:style {:font-weight "bold"}} "Please pick a topic from the below choices"])
              [:ul.list-group {:style {:max-width "500px"}}
               (->> (:candidate-topics current-essay)
                    (map #(-> [:a.list-group-item.list-group-item-active
                               {:href "#"
                                :class (when (= % (:title current-essay)) "active")
                                :on-click (partial re-frame/dispatch [::events/topic-selected %])}
                               %])))]
              [gap :size "15px"]
              [label :label "Target Essay Length"]
              [input-text
               :model (str (:target-length current-essay))
               :on-change #(re-frame/dispatch [::events/essay-target-length-changed (utils/parse-int %)])
               :change-on-blur? false]
              [gap :size "15px"]
              [button
               :disabled? (not (and (contains? (:candidate-topics current-essay) (:title current-essay))
                                    (:target-length current-essay)))
               :class "btn-primary"
               :label "Next Step"
               :on-click #(re-frame/dispatch [::events/next-step])]]])


(defn outline [current-essay]
  (let [min-sentences (min 15 (int (/ (:target-length current-essay) 100)))]
    [v-box
     :children [[p "It's now time to write the outline of your essay. You need to write "
                 [:b min-sentences] " headings, one per 100 words of your essay (up to 15 headings)."]
                [gap :size "15px"]
                [editable-list {:items (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                                :label-fn :heading
                                :on-item-added #(re-frame/dispatch [::events/outline-heading-added %])
                                :on-item-removed #(re-frame/dispatch [::events/outline-heading-removed (:heading %)])
                                :on-item-moved-up #(re-frame/dispatch [::events/paragraph-moved-up %])
                                :on-item-moved-down #(re-frame/dispatch [::events/paragraph-moved-down %])}]
                [gap :size "5px"]
                [p "You have written " [:b (count (:outline current-essay))] " headings out of " [:b min-sentences] "."]
                [gap :size "15px"]
                [button
                 :disabled? (> 0 (count (:outline current-essay)))
                 :class "btn-primary"
                 :label "Next Step"
                 :on-click #(re-frame/dispatch [::events/next-step])]]]))


(defn outline-paragraphs [current-essay]
  [v-box
   :children (concat [[p "Aim to write about " [:b "10 to 15"] " sentences per outline heading."
                       "You can write more or less if you want."]
                      [p "You can use triple-backticks (```) to mark out code blocks, e.g. ```1 + 1```."]
                      [p "To review your notes, " [hyperlink :label "click here." :on-click #(re-frame/dispatch [::events/view-notes-requested (:id current-essay)])]]]
                     (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                          (mapcat (fn [section]
                                    [[title :level :level3 :label (:heading section)]
                                     [input-textarea
                                      :model (:v1 (:paragraph section))
                                      :change-on-blur? false
                                      :on-change #(re-frame/dispatch [::events/outline-paragraph-updated (:heading section) %])
                                      :rows 8
                                      :width "650px"]
                                     [p "You have written "
                                      [:b (count (get-in section [:sentences :v1]))]
                                      " sentences, and "
                                      [:b (-> (get-in section [:paragraph :v1]) (utils/words) count)]
                                      " words."]])))
                     [[p
                       "You have written "
                       (->> (:outline current-essay)
                            vals
                            (map (comp :v1 :paragraph))
                            (mapcat utils/words)
                            count)
                       " words, out of a target number of "
                       (:target-length current-essay)
                       " (aim for 25% above the target, " (int (* 1.25 (:target-length current-essay))) ")"]
                      [button
                       :disabled? (not-every? (comp #(not (s/blank? (:v1 (:paragraph %)))) val) (:outline current-essay))
                       :class "btn-primary"
                       :label "Next Step"
                       :on-click #(re-frame/dispatch [::events/next-step])]])])


(defn rewrite-sentences [current-essay]
  [v-box
   :children (concat [[p
                       "Re-write all the sentences you wrote in the previous step. "
                       "If you can't improve on a sentence, just copy and paste it into the input box."
                       "If you want to completely remove a sentence, leave the input box blank."]]
                     (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                          (map (fn [section]
                                 [v-box
                                  :children [[title :level :level3 :label (:heading section)]
                                             (->> (get-in section [:sentences :v1])
                                                  (map-indexed (fn [idx v1]
                                                                 [idx v1 (get-in section [:sentences :v2 idx])]))
                                                  (mapcat (fn [[idx v1 v2]]
                                                            (let [label (if (= (:type v1) :sentence)
                                                                          (:value v1)
                                                                          "Code Block")]
                                                              [[title :level :level4 :label label]
                                                               [input-textarea
                                                                :rows 3
                                                                :width "700px"
                                                                :model (:value v2)
                                                                :on-change #(re-frame/dispatch [::events/sentence-rewritten (:heading section) idx %])]
                                                               [gap :size "5px"]]))))]])))
                     [[p
                       "You have written "
                       (->> (:outline current-essay)
                            vals
                            (map (comp utils/join-sentences :v2 :sentences))
                            (mapcat utils/words)
                            count)
                       " words, out of a target number of "
                       (:target-length current-essay)
                       " (aim for 25% above the target, " (int (* 1.25 (:target-length current-essay))) ")"]
                      [gap :size "15px"]
                      [button
                       :disabled? (->> (get-in current-essay [:outline])
                                       (mapcat (comp :v2 :sentences val))
                                       (every? (comp s/blank? :value)))
                       :class "btn-primary"
                       :label "Next Step"
                       :on-click #(re-frame/dispatch [::events/next-step])]])])


(defn reorder-sentences [current-essay]
  [v-box
   :children (concat [[p "Try re-ordering the sentences within each paragraph."]]
                     (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                          (map (fn [section]
                                 [v-box
                                  :children [[title :level :level3 :label (:heading section)]
                                             [p (->> (get-in section [:sentences :v2])
                                                     (map utils/mask-code)
                                                     (utils/join-sentences))]
                                             [editable-list {:items (->> (get-in section [:sentences :v2])
                                                                         (map utils/mask-code)
                                                                         (map :value)
                                                                         (map-indexed vector))
                                                             :label-fn second
                                                             :on-item-moved-up (fn [[i _]]
                                                                                 (re-frame/dispatch [::events/sentence-moved-up (:heading section) i]))
                                                             :on-item-moved-down (fn [[i _]]
                                                                                   (re-frame/dispatch [::events/sentence-moved-down (:heading section) i]))}]]])))
                     [[gap :size "15px"]
                      [button
                       :class "btn-primary"
                       :label "Next Step"
                       :on-click #(re-frame/dispatch [::events/next-step])]])])


(defn reorder-paragraphs [current-essay]
  (let [ordered-sections (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))]
    [v-box
     :children [[p "If you want to, re-order the paragraphs."]
                [gap :size "10px"]
                [editable-list {:items ordered-sections
                                :label-fn #(->> (get-in % [:sentences :v2])
                                                (map utils/mask-code)
                                                (utils/join-sentences))
                                :on-item-moved-up #(re-frame/dispatch [::events/paragraph-moved-up %])
                                :on-item-moved-down #(re-frame/dispatch [::events/paragraph-moved-down %])}]
                [title :level :level3 :label "Essay"]
                [essay-display (->> ordered-sections
                                    (map (comp :v2 :sentences)))]
                [gap :size "15px"]
                [button
                 :class "btn-primary"
                 :label "Next Step"
                 :on-click #(re-frame/dispatch [::events/next-step])]]]))


(defn read-draft [current-essay]
  [v-box
   :children [[p "Read your draft essay. You don't need to memorize it, just read it as you would someone else's essay."]
              [essay-display (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                                  (map (comp :v2 :sentences)))]
              [gap :size "15px"]
              [button
               :class "btn-primary"
               :label "Next Step"
               :on-click #(re-frame/dispatch [::events/next-step])]]])


(defn second-outline [current-essay]
  (let [min-sentences (min 15 (int (/ (:target-length current-essay) 100)))]
    [v-box
     :children [[p "Now write a new outline. " [:b "Don’t look back at your essay while you are doing this."]]
                [gap :size "15px"]
                [editable-list {:items (map key (:second-outline current-essay))
                                :on-item-added #(re-frame/dispatch [::events/second-outline-heading-added %])
                                :on-item-removed #(re-frame/dispatch [::events/second-outline-heading-removed %])}]
                [gap :size "5px"]
                [p "You have written " [:b (count (:second-outline current-essay))] " headings out of " [:b min-sentences] "."]
                [gap :size "15px"]
                [button
                 :disabled? (zero? (count (:second-outline current-essay)))
                 :class "btn-primary"
                 :label "Next Step"
                 :on-click #(re-frame/dispatch [::events/next-step])]]]))


(defn copy-from-draft [current-essay]
  [v-box
   :children (concat [[p
                       "Copy from your draft essay into the new outline. "
                       "You'll get a chance to edit your final essay in the next step, so don't worry about the formatting too much."]
                      [essay-display (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                                          (map (comp :v2 :sentences)))]
                      [gap :size "15px"]]
                     (->> (:second-outline current-essay)
                          (mapcat (fn [[heading section]]
                                    [[title :level :level3 :label heading]
                                     [gap :size "5px"]
                                     [quill {:default-value (:paragraph section)
                                             :on-change (fn [html _ _ _]
                                                          (re-frame/dispatch [::events/second-outline-paragraph-updated heading html]))}]
                                     [gap :size "10px"]])))
                     [[gap :size "15px"]
                      [button
                       :disabled? (not-every? (comp #(not (s/blank? (:paragraph %))) val) (:second-outline current-essay))
                       :class "btn-primary"
                       :label "Next Step"
                       :on-click #(re-frame/dispatch [::events/next-step])]])])


(defn final-essay [current-essay]
  [scroller
   :child [v-box
           :children [[p
                       "You can now format your final essay, and add citations if you wish. "
                       "Your reading list is displayed below for you to copy citations from. "
                       "When you've completed the essay, you can copy and paste it into a Word doc or Google doc."]
                      [p "To review your notes, " [hyperlink :label "click here." :on-click #(re-frame/dispatch [::events/view-notes-requested (:id current-essay)])]]
                      [quill {:default-value (:final-essay current-essay)
                              :on-change (fn [html _ _ editor]
                                           (re-frame/dispatch [::events/final-essay-updated html (.call (aget editor "getText"))]))}]
                      [p
                       "You have written "
                       (:final-essay-word-count current-essay)
                       " words, out of a target number of "
                       (:target-length current-essay) "."]]]])


(defn essay-step []
  (let [share-dialog-open (r/atom false)
        encryption-key    (r/atom nil)]
    (fn [current-essay page page-component]
      [:div.uk-flex.uk-flex-column
       [:div.uk-flex.uk-flex-row.uk-flex-between.uk-flex-middle
        [:h3 {:style {:margin "0.3em 0"}}
         (:title current-essay)]
        [:div.uk-flex
         [:button.uk-button.uk-button-default.uk-button-small.uk-border-rounded
          {"uk-tooltip" "title: Download Essay; pos: bottom"
           :on-click #(re-frame/dispatch [::events/export-requested (:id current-essay)])}
          [:i.zmdi.zmdi-download]]
         (when-not (or (and (= (:notes-type current-essay) :in-app) (str/blank? (:notes current-essay)))
                       (and (= (:notes-type current-essay) :external) (str/blank? (:external-notes-url current-essay))))
           [:button.uk-button.uk-button-default.uk-button-small.uk-border-rounded
            {"uk-tooltip" "title: View Notes; pos: bottom"
             :on-click #(re-frame/dispatch [::events/view-notes-requested (:id current-essay)])}
            [:i.zmdi.zmdi-file-text]])
         [:button.uk-button.uk-button-default.uk-button-small.uk-border-rounded
          {"uk-tooltip" "title: Share Essay; pos: bottom"
           "uk-toggle" "target: #share-modal"}
          [:i.zmdi.zmdi-share]]]]
       [:div {"uk-grid" "true"
              :class "uk-child-width-expand@s"}
        [:div.uk-width-auto
         [:ul.uk-nav.uk-nav-default.sidebar
          [:li.uk-nav-header "Essay Steps"]
          (->> constants/steps
               (map #(let [enabled (utils/step-before-or-equal? % (:highest-step current-essay))]
                       ^{:key %}
                       [:li {:class (str (when (= (:current-step current-essay) %)
                                           "uk-active ")
                                         (when-not enabled
                                           "uk-disabled "))}
                        [:a {:href (when enabled (utils/step-url (:id current-essay) %))}
                         (utils/displayable-step-name %)]])))]]
        [:div.uk-width-expand
         [:h3 (utils/displayable-step-name page)]
         [page-component current-essay]]]
       [:div#share-modal {"uk-modal" "true"}
        [:div.uk-modal-dialog.uk-modal-body
         [:h3 "Share Essay"]
         [p
          "This will share your essay by uploading it to " [:a {:href "https://pastebin.com" :target "_blank"} "PasteBin"] "."]
         [p
          "If you enter an encryption key, nobody will be able to view your essay without the password.
           It is recommended to use 4 random words for this password, like \"correct-horse-battery-staple\"."]
         [:label "Encryption Password"]
         [:input.uk-input
          {:value @encryption-key
           :on-change #(reset! encryption-key (.-value (.-target %)))}]
         [:label "(Leave blank to share essay unencrypted)"]
         [:div
          [:button.uk-button.uk-button-default.uk-modal-close
           {:on-click #(reset! share-dialog-open false)}
           "Cancel"]
          [:button.uk-button.uk-button-primary.uk-modal-close
           {:on-click #(re-frame/dispatch [::events/remote-save-requested (:id current-essay) @encryption-key])}
           "OK"]]]]])))

(defn not-found []
  [p "Route not found!"])

(defn import-essay []
  [p "Importing..."])

(defn pages [page]
  (let [page-component   (case page
                           :home home
                           :about about
                           :import-essay import-essay
                           :candidate-topics candidate-topics
                           :reading-list reading-list
                           :topic-choice topic-choice
                           :outline outline
                           :outline-paragraphs outline-paragraphs
                           :rewrite-sentences rewrite-sentences
                           :reorder-sentences reorder-sentences
                           :reorder-paragraphs reorder-paragraphs
                           :read-draft read-draft
                           :second-outline second-outline
                           :copy-from-draft copy-from-draft
                           :final-essay final-essay
                           not-found)
        for-single-essay (not (contains? #{home about import-essay not-found} page-component))]
    (if for-single-essay
      (let [*current-essay (re-frame/subscribe [::subs/current-essay])]
        (if @*current-essay
          [essay-step @*current-essay page page-component]
          [:p "Essay not found!"]))
      [page-component])))


(defn main-panel []
  (let [*active-page (re-frame/subscribe [::subs/active-page])
        *alerts      (re-frame/subscribe [::subs/alerts])]
    [:div
     [:div#app-bar.uk-navbar-container.uk-light {"uk-navbar" ""}
      [:div.uk-navbar-left
       [:a.uk-navbar-item.uk-logo {:href "/"} "Lobster Writer"]]
      [:div.uk-navbar-right
       [:a.uk-navbar-item {:href "/"} "Home"]
       [:a.uk-navbar-item {:href "/about"} "About"]
       [:a.uk-navbar-item
        {:style {:text-decoration "none"}
         :href "https://github.com/DaveWM/lobster-writer"
         :target "_blank"}
        [:i.zmdi.zmdi-hc-2x.zmdi-github {"uk-tooltip" "title: GitHub Repo; pos: bottom"}]]
       [:a.uk-navbar-item {:class "dm-logo" :href "https://davemartin.me" :target "_blank"}
        [:img {:src "/images/dm-logo.png"}]]]]
     [:div.uk-section
      [:div.uk-container
       [:div
        (for [alert @*alerts]
          ^{:key (:id alert)}
          [:div.uk-alert.uk-alert-danger
           [:a.uk-alert-close {"uk-close" "true"
                               :on-click #(re-frame/dispatch [::events/close-alert %])}]
           (:body alert)])]
       [pages @*active-page]]
      [:div#saving-indicator
       [md-icon-button :md-icon-name "zmdi-floppy" :size :larger]]]]))
