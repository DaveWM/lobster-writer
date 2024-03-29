(ns lobster-writer.views
  (:require
    [re-frame.core :as re-frame]
    [lobster-writer.subs :as subs]
    [lobster-writer.events :as events]
    [lobster-writer.components.editable-list :refer [editable-list]]
    [lobster-writer.utils :as utils]
    [lobster-writer.constants :as constants]
    [lobster-writer.components.helpers :refer [essay-display next-step]]
    [clojure.string :as s]
    [reagent.core :as r]
    [cljsjs.prop-types]
    [cljsjs.react-quill]
    [cljs-time.core :as t]
    [cljs-time.format :as tf]
    [cljs-time.coerce :as tc]
    [lobster-writer.components.file-chooser :refer [file-chooser]]
    [clojure.string :as str]
    [lobster-writer.remote-storage :as rs]))


(def react-quill (r/adapt-react-class js/ReactQuill))

(defn quill [props]
  [react-quill (assoc props
                 :modules {:toolbar [[{:header [1 2 3 false]}]
                                     ["bold" "italic" "underline" "strike" "blockquote"]
                                     [{:list "ordered"} {:list "bullet"} {:indent "-1"} {:indent "+1"}]
                                     ["link"]
                                     ["clean"]
                                     ["code-block"]]})])

(defn loading-spinner []
  [:div.flipped
   [:i.zmdi.zmdi-hc-spin-reverse.zmdi-replay]])


;; home

(defn home []
  (let [*all-essays (re-frame/subscribe [::subs/all-essays])
        *rs-info (re-frame/subscribe [::subs/remote-storage])]
    [:div
     (when (:available? @*rs-info)
       [:<>
        [:div.uk-margin
         [:h4 "Remote Storage"]
         [:div.uk-flex.uk-flex-start
          [:button.uk-button.uk-button-default.uk-margin-right
           {:on-click #(re-frame/dispatch [::events/remote-storage-save-all-requested])}
           (if (:uploading? @*rs-info)
             [loading-spinner]
             "Upload All")]
          [:button.uk-button.uk-button-default
           {:on-click #(re-frame/dispatch [::events/remote-storage-retrieve-all-requested])}
           (if (:downloading? @*rs-info)
             [loading-spinner]
             "Download All")]]]
        [:hr]])
     [:div
      [:h4 "Essays"]
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
                     :on-change #(re-frame/dispatch [::events/import-requested (utils/ev-val %)])}
       [:span "Import " [:i.zmdi.zmdi-hc-fw-rc.zmdi-upload]]]]]))


(defn about []
  [:div
   [:p "Lobster Writer is an application to help you write essays. It is based on the advice of Dr. Jordan Peterson in "
    [:a {:href "/Jordan-Peterson-Writing-Template.docx" :target "_blank"} "this essay writing guide"]
    ". According to Dr. Peterson, this method will help you \"to write an excellent essay from beginning to end\". "]
   [:p "Lobster Writer is also available as an Android app:"
    [:a.play-badge {:href "https://play.google.com/store/apps/details?id=uk.co.lobster_writer.twa&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1"}
     [:img {:alt "Get it on Google Play" :src "https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"}]]]
   [:p
    "Lobster Writer is free (both gratis and libre) software - you can find the souce code "
    [:a {:href "https://github.com/DaveWM/lobster-writer"} "here"]
    ". If you would like to support me in developing Lobster Writer, please follow the link below."]

   [:p.uk-text-bolder "Lobster Writer is not associated with Dr. Peterson in any way."]])


(defn candidate-topics [current-essay]
  [:div.step
   [:p "This is the first step in writing your essay. List around "
    [:b "10"]
    " topics that you would like to write about, or questions that you would like to answer."]
   [editable-list {:items (:candidate-topics current-essay)
                   :on-item-added #(re-frame/dispatch [::events/candidate-topic-added %])
                   :on-item-removed #(re-frame/dispatch [::events/candidate-topic-removed %])}]
   [:button.uk-button.uk-button-primary.next-step
    {:disabled (empty? (:candidate-topics current-essay))
     :on-click #(re-frame/dispatch [::events/next-step])}
    "Next Step"]])


(defn reading-list [current-essay]
  [:div.step
   [:p
    "Great, you've thought of some potential topics to write about. Now you need to find some books or articles to read. "
    "You should read around " [:b "5 to 10 books per 1000 words of essay."] " List them here."]
   [editable-list {:items (:reading-list current-essay)
                   :on-item-added #(re-frame/dispatch [::events/reading-list-item-added %])
                   :on-item-removed #(re-frame/dispatch [::events/reading-list-item-removed %])}]
   [:p
    "You can also make some notes if you want. "
    "One good way to make notes is to read a small section at a time, then write down what you have learned and any questions that you have. "]
   [:span
    "Where do you want to store the notes?"]
   [:form {:style {:display :flex
                   :flex-direction :column
                   :flex-wrap "nowrap"
                   :margin-top "10px"
                   :margin-bottom "25px"}}
    [:label {:for "in-app"}
     [:input.uk-radio {:id "in-app"
                       :type "radio"
                       :name "notes-type"
                       :value :in-app
                       :checked (= (:notes-type current-essay) :in-app)
                       :on-change (fn [v]
                                    (re-frame/dispatch [::events/notes-type-updated (-> v
                                                                                        .-target
                                                                                        .-value
                                                                                        keyword)]))}]
     " here"]
    [:label {:for "external"}
     [:input.uk-radio {:id "external"
                       :type "radio"
                       :name "notes-type"
                       :value :external
                       :checked (= (:notes-type current-essay) :external)
                       :on-change (fn [v]
                                    (re-frame/dispatch [::events/notes-type-updated (-> v
                                                                                        .-target
                                                                                        .-value
                                                                                        keyword)]))}]
     " in another app"]]
   (if (= (:notes-type current-essay) :in-app)
     [quill {:default-value (:notes current-essay)
             :on-change (fn [html _ _ editor]
                          (re-frame/dispatch [::events/in-app-notes-updated html (.call (aget editor "getText"))]))}]
     [:input.uk-input
      {:default-value (:external-notes-url current-essay)
       :on-blur #(re-frame/dispatch [::events/external-notes-url-updated (utils/ev-val %)])
       :placeholder "Enter the URL of your notes here"}])
   [:button.uk-button.uk-button-primary.next-step
    {:disabled (empty? (:reading-list current-essay))
     :on-click #(re-frame/dispatch [::events/next-step])}
    "Next Step"]])


(defn topic-choice [current-essay]
  [:div.step
   [:p "You now need to choose your topic, and the length your essay will be. "]
   [:b "Please pick a topic from the below choices"]
   [:ul.uk-card.uk-card-default.uk-list.uk-list-striped.topic-selection
    (->> (:candidate-topics current-essay)
         (map #(-> [:li.topic-selection__item {:class (when (= % (:title current-essay)) "topic-selection__item--selected")
                                               :on-click (partial re-frame/dispatch [::events/topic-selected %])}
                    %])))]
   [:label.uk-form-label {:for "target-essay-length"}
    "Target Essay Length"]
   [:input#target-essay-length.uk-input
    {:default-value (str (:target-length current-essay))
     :on-change #(re-frame/dispatch [::events/essay-target-length-changed (utils/parse-int (utils/ev-val %))])}]
   [:button.uk-button.uk-button-primary.next-step
    {:disabled (not (and (contains? (:candidate-topics current-essay) (:title current-essay))
                         (:target-length current-essay)))
     :on-click #(re-frame/dispatch [::events/next-step])}
    "Next Step"]])


(defn outline [current-essay]
  (let [min-sentences (min 15 (int (/ (:target-length current-essay) 100)))]
    [:div.step
     [:p "It's now time to write the outline of your essay. You need to write "
      [:b min-sentences] " headings, one per 100 words of your essay (up to 15 headings)."]
     [editable-list {:items (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                     :label-fn :heading
                     :on-item-added #(re-frame/dispatch [::events/outline-heading-added %])
                     :on-item-removed #(re-frame/dispatch [::events/outline-heading-removed (:heading %)])
                     :on-item-moved-up #(re-frame/dispatch [::events/paragraph-moved-up %])
                     :on-item-moved-down #(re-frame/dispatch [::events/paragraph-moved-down %])}]
     [:p "You have written " [:b (count (:outline current-essay))] " headings out of " [:b min-sentences] "."]
     [:button.uk-button.uk-button-primary.next-step
      {:disabled (zero? (count (:outline current-essay)))
       :on-click #(re-frame/dispatch [::events/next-step])}
      "Next Step"]]))


(defn outline-paragraphs [current-essay]
  [:div.step.outline-paragraphs
   (concat [[:p.outline-paragraphs__explanation
             "Aim to write about " [:b "10 to 15"] " sentences per outline heading."
             "You can write more or less if you want."]
            [:p.outline-paragraphs__explanation
             "You can use triple-backticks (```) to mark out code blocks, e.g. ```1 + 1```."]
            [:p.outline-paragraphs__explanation
             "To review your notes, "
             [:a {:on-click #(re-frame/dispatch [::events/view-notes-requested (:id current-essay)])}
              "click here."]]]
           (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                (mapcat (fn [section]
                          [[:h5.uk-margin-small-top (:heading section)]
                           [:textarea.uk-textarea
                            {:default-value (:v1 (:paragraph section))
                             :on-change #(re-frame/dispatch [::events/outline-paragraph-updated (:heading section) (utils/ev-val %)])
                             :rows 8}]
                           [:p "You have written "
                            [:b (count (get-in section [:sentences :v1]))]
                            " sentences, and "
                            [:b (-> (get-in section [:paragraph :v1]) (utils/words) count)]
                            " words."]])))
           [[:p
             "You have written "
             (->> (:outline current-essay)
                  vals
                  (map (comp :v1 :paragraph))
                  (mapcat utils/words)
                  count)
             " words, out of a target number of "
             (:target-length current-essay)
             " (aim for 25% above the target, " (int (* 1.25 (:target-length current-essay))) ")"]
            [:button.uk-button.uk-button-primary.next-step
             {:disabled (not-every? (comp #(not (s/blank? (:v1 (:paragraph %)))) val) (:outline current-essay))
              :on-click #(re-frame/dispatch [::events/next-step])}
             "Next Step"]])])


(defn rewrite-sentences [current-essay]
  [:div.step
   (concat [[:p
             "Re-write all the sentences you wrote in the previous step. "
             "If you can't improve on a sentence, just copy and paste it into the input box."
             "If you want to completely remove a sentence, leave the input box blank."]]
           (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                (map (fn [section]
                       [:div
                        [:h5.uk-margin-top (:heading section)]
                        (->> (get-in section [:sentences :v1])
                             (map-indexed (fn [idx v1]
                                            [idx v1 (get-in section [:sentences :v2 idx])]))
                             (mapcat (fn [[idx v1 v2]]
                                       (let [label (if (= (:type v1) :sentence)
                                                     (:value v1)
                                                     "Code Block")]
                                         [[:h6.uk-margin-small-top label]
                                          [:textarea.uk-textarea
                                           {:rows 3
                                            :default-value (:value v2)
                                            :on-change #(re-frame/dispatch [::events/sentence-rewritten (:heading section) idx (utils/ev-val %)])}]]))))])))
           [[:p
             "You have written "
             (->> (:outline current-essay)
                  vals
                  (map (comp utils/join-sentences :v2 :sentences))
                  (mapcat utils/words)
                  count)
             " words, out of a target number of "
             (:target-length current-essay)
             " (aim for 25% above the target, " (int (* 1.25 (:target-length current-essay))) ")"]
            [:button.uk-button.uk-button-primary.next-step
             {:disabled (->> (get-in current-essay [:outline])
                             (mapcat (comp :v2 :sentences val))
                             (every? (comp s/blank? :value)))
              :on-click #(re-frame/dispatch [::events/next-step])}
             "Next Step"]])])


(defn reorder-sentences [current-essay]
  [:div.step
   (concat [[:p "Try re-ordering the sentences within each paragraph."]]
           (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                (map (fn [section]
                       [:div
                        [:h5.uk-margin-top (:heading section)]
                        [:p (->> (get-in section [:sentences :v2])
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
                                                              (re-frame/dispatch [::events/sentence-moved-down (:heading section) i]))}]])))
           [[:button.uk-button.uk-button-primary.next-step
             {:on-click #(re-frame/dispatch [::events/next-step])}
             "Next Step"]])])


(defn reorder-paragraphs [current-essay]
  (let [ordered-sections (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))]
    [:div.step
     [:p "If you want to, re-order the paragraphs."]
     [editable-list {:items ordered-sections
                     :label-fn #(->> (get-in % [:sentences :v2])
                                     (map utils/mask-code)
                                     (utils/join-sentences))
                     :on-item-moved-up #(re-frame/dispatch [::events/paragraph-moved-up %])
                     :on-item-moved-down #(re-frame/dispatch [::events/paragraph-moved-down %])}]
     [essay-display
      (->> ordered-sections
           (map (comp :v2 :sentences)))
      (:title current-essay)]
     [:button.uk-button.uk-button-primary.next-step
      {:on-click #(re-frame/dispatch [::events/next-step])}
      "Next Step"]]))


(defn read-draft [current-essay]
  [:div.step
   [:p "Read your draft essay. You don't need to memorize it, just read it as you would someone else's essay."]
   [essay-display
    (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
         (map (comp :v2 :sentences)))
    (:title current-essay)]
   [:button.uk-button.uk-button-primary.next-step
    {:on-click #(re-frame/dispatch [::events/next-step])}
    "Next Step"]])


(defn second-outline [current-essay]
  [:div.step
   [:p "Now write a new outline. " [:b "Don’t look back at your essay while you are doing this."]]
   [editable-list {:items (utils/ordered-by (:second-outline current-essay) (:second-paragraph-order current-essay))
                   :label-fn :heading
                   :on-item-added #(re-frame/dispatch [::events/second-outline-heading-added %])
                   :on-item-removed #(re-frame/dispatch [::events/second-outline-heading-removed (:heading %)])
                   :on-item-moved-up #(re-frame/dispatch [::events/second-paragraph-moved-up %])
                   :on-item-moved-down #(re-frame/dispatch [::events/second-paragraph-moved-down %])}]
   [next-step
    {:disabled (zero? (count (:second-outline current-essay)))
     :on-click #(re-frame/dispatch [::events/next-step])}]])


(defn copy-from-draft [current-essay]
  [:div.step
   (concat [[:p
             "Copy from your draft essay into the new outline. "
             "You'll get a chance to edit your final essay in the next step, so don't worry about the formatting too much."]
            [essay-display
             (->> (utils/ordered-by (:outline current-essay) (:paragraph-order current-essay))
                  (map (comp :v2 :sentences)))
             (:title current-essay)]]
           (->> (utils/ordered-by (:second-outline current-essay) (:second-paragraph-order current-essay))
                (mapcat (fn [{:keys [heading paragraph]}]
                          [[:h5 heading]
                           [:textarea.uk-textarea
                            {:default-value paragraph
                             :on-change #(re-frame/dispatch [::events/second-outline-paragraph-updated heading (utils/ev-val %)])
                             :rows 8}]])))
           [[next-step
             {:disabled? (not-every? (comp #(not (s/blank? (:paragraph %))) val) (:second-outline current-essay))
              :on-click #(re-frame/dispatch [::events/next-step])}]
            [:button.uk-button.uk-button-default
             {:on-click #(re-frame/dispatch [::events/repeat-sentence-rewrite])}
             [:div.uk-flex.uk-flex-column {"uk-tooltip" "Repeat the process from the 'Outline' step, using the new outline"}
              "Repeat"]]])])


(defn final-essay [current-essay]
  [:div.step
   [:p
    "You can now format your final essay, and add citations if you wish. "
    "Your reading list is displayed below for you to copy citations from. "
    "When you've completed the essay, you can copy and paste it into a Word doc or Google doc."]
   [:p "To review your notes, " [:a {:on-click #(re-frame/dispatch [::events/view-notes-requested (:id current-essay)])} "click here."]]
   [editable-list {:items (:reading-list current-essay)}]
   [quill {:default-value (:final-essay current-essay)
           :on-change (fn [html _ _ editor]
                        (re-frame/dispatch [::events/final-essay-updated html (.call (aget editor "getText"))]))}]
   [:p
    "You have written "
    (:final-essay-word-count current-essay)
    " words, out of a target number of "
    (:target-length current-essay) "."]])

(defn sidebar [current-essay]
  [:div.sidebar.uk-card.uk-card-body
   [:h4.sidebar__header "Essay Steps"]
   [:div.sidebar__list
    (->> constants/steps
         (map #(let [enabled (utils/step-before-or-equal? % (:highest-step current-essay))]
                 ^{:key %}
                 [:a.sidebar__step
                  {:href (when enabled (utils/step-url (:id current-essay) %))
                   :class (str (when (= (:current-step current-essay) %)
                                 "sidebar__step--active ")
                               (when-not enabled
                                 "sidebar__step--disabled "))}
                  (utils/displayable-step-name %)])))]])

(defn essay-step []
  (let [share-dialog-open (r/atom false)
        encryption-key (r/atom nil)
        sidebar-open (re-frame/subscribe [::subs/sidebar-open])
        rs-info (re-frame/subscribe [::subs/remote-storage])]
    (fn [current-essay page page-component]
      [:div.uk-flex.uk-flex-column.essay
       [:div.uk-flex.uk-flex-row.uk-flex-between.uk-flex-middle.essay__header
        [:h3.uk-margin-remove
         (:title current-essay)]
        [:div.uk-flex
         (when (:available? @rs-info)
           [:<>
            [:button.uk-button.uk-button-default.uk-button-small.uk-border-rounded
             {"uk-tooltip" "title: Upload to Remote Storage; pos: bottom"
              :on-click #(re-frame/dispatch [::events/remote-storage-save-requested (:id current-essay)])}
             (if (:uploading? @rs-info)
               [loading-spinner]
               [:i.zmdi.zmdi-cloud-upload])]
            [:button.uk-button.uk-button-default.uk-button-small.uk-border-rounded
             {"uk-tooltip" "title: Download from Remote Storage; pos: bottom"
              :on-click #(re-frame/dispatch [::events/remote-storage-retrieve-requested (:id current-essay)])}
             (if (:downloading? @rs-info)
               [loading-spinner]
               [:i.zmdi.zmdi-cloud-download])]])
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
       [:div.uk-offcanvas {:class (when @sidebar-open "uk-offcanvas-overlay uk-open")
                           :style {:display :block}}
        [:div.uk-offcanvas-bar.uk-flex.uk-padding-remove.uk-offcanvas-bar-animation.uk-offcanvas-slide
         [:button.uk-offcanvas-close {"uk-close" ""
                                      :on-click #(re-frame/dispatch [::events/sidebar-closed])}]
         [sidebar current-essay]]]
       [:div {"uk-grid" "true"
              :class "uk-child-width-expand@s"}
        [:div.uk-width-auto.uk-flex {:class "uk-visible@m"}
         [sidebar current-essay]]
        [:div.uk-width-expand
         [:h3
          (utils/displayable-step-name page)
          [:button.uk-button.uk-button-default.uk-button-small.uk-border-rounded.uk-margin-left
           {:class "uk-hidden@m"
            :on-click #(re-frame/dispatch [::events/sidebar-opened])}
           [:i.zmdi.zmdi-menu]]]
         [page-component current-essay]]]
       [:div#share-modal {"uk-modal" "true"}
        [:div.uk-modal-dialog.uk-modal-body
         [:h3 "Share Essay"]
         [:p
          "This will share your essay by uploading it to " [:a {:href "https://pastebin.com" :target "_blank"} "PasteBin"] "."]
         [:p
          "If you enter an encryption key, nobody will be able to view your essay without the password.
           It is recommended to use 4 random words for this password, like \"correct-horse-battery-staple\"."]
         [:label {:for "password"}
          "Encryption Password"]
         [:input#password.uk-input
          {:value @encryption-key
           :on-change #(reset! encryption-key (.-value (.-target %)))}]
         [:small "(Leave blank to share essay unencrypted)"]
         [:div.uk-margin-top
          [:button.uk-button.uk-button-primary.uk-modal-close
           {:on-click #(re-frame/dispatch [::events/remote-save-requested (:id current-essay) @encryption-key])}
           "OK"]
          [:button.uk-button.uk-button-default.uk-modal-close
           {:on-click #(reset! share-dialog-open false)}
           "Cancel"]]]]])))

(defn not-found []
  [:p "Route not found!"])

(defn import-essay []
  [:p "Importing..."])

(defn pages [page]
  (let [page-component (case page
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

(defn remote-storage-widget []
  (reagent.core/create-class
    {:component-did-mount (fn [this]
                            (doto (js/Widget. rs/remote-storage (clj->js {:leaveOpen true
                                                                          :skipInitial true}))
                              (.attach "rs")))
     :reagent-render (fn [] [:div#rs])}))

(defn remote-storage-modal []
  [:div#remote-storage-modal {"uk-modal" "true"}
   [:div.uk-modal-dialog.uk-modal-body
    [:h3 "Connect to Remote Storage"]
    [:p
     "Connect to external storage (Google Drive, Dropbox, or remotestorage.io) to save your essays."]
    [:a.uk-navbar-item
     [remote-storage-widget]]]])

(defn header []
  [:div#app-bar.uk-navbar-container.uk-light {"uk-navbar" ""}
   [:div.uk-navbar-left
    [:div.uk-navbar-item
     [:a.uk-logo.app-bar__title {:href "/"} "Lobster Writer"]]]
   [:div.uk-navbar-right.app-bar__options
    [:a.uk-navbar-item {:href "/"} "Home"]
    [:a.uk-navbar-item {:href "/about"} "About"]
    [:a.uk-navbar-item.remote-storage-logo
     {"uk-toggle" "target: #remote-storage-modal"}
     [:img {:src "/images/icons/remote-storage.svg"}]]
    [:a.uk-navbar-item
     {:style {:text-decoration "none"}
      :href "https://github.com/DaveWM/lobster-writer"
      :target "_blank"}
     [:i.zmdi.zmdi-hc-2x.zmdi-github]]
    [:a.uk-navbar-item.dm-logo {:href "https://davemartin.me" :target "_blank"}
     [:img {:src "/images/dmp-logo.png"}]]]])


(defn main-panel []
  (let [*active-page (re-frame/subscribe [::subs/active-page])
        *alerts (re-frame/subscribe [::subs/alerts])
        *rs-info (re-frame/subscribe [::subs/remote-storage])]
    [:div
     [header @*rs-info]
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
      [remote-storage-modal]
      [:div#saving-indicator
       [:i.zmdi.zmdi-hc-2x.zmdi-floppy]]]]))
