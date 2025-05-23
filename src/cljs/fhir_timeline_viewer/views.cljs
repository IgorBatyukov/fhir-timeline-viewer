(ns fhir-timeline-viewer.views
  (:require [clojure.string :as str]
            [fhir-timeline-viewer.events :as events]
            [fhir-timeline-viewer.subs :as subs]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(defn detail-view-display [item]
  [:div.detail-panel
   [:div.detail-header
    [:h2 (:resource_type item)]
    [:button.close-btn
     {:on-click #(rf/dispatch [::events/close-detail])}
     "Back to timeline"]]
   [:div.detail-content
    [:div.detail-row
     [:strong "ID: "] [:span (:id item)]]
    [:div.detail-row
     [:strong "Resource ID: "] [:span (:resource_id item)]]
    [:div.detail-row
     [:strong "Patient ID: "] [:span (:patient_id item)]]
    [:div.detail-row
     [:strong "Date: "] [:span (:effective_dt item)]]
    (when-let [status (:resource_status item)]
      [:div.detail-row
       [:strong "Status: "] [:span status]])
    (when-let [verification (:verification_status item)]
      [:div.detail-row
       [:strong "Verification: "] [:span verification]])
    (when-let [code (:code_text item)]
      [:div.detail-row
       [:strong "Code: "] [:span code]])
    (when-let [categories (:categories item)]
      [:div.detail-section
       [:h3 "Categories"]
       [:ul.detail-list
        (for [category categories]
          ^{:key category} [:li category])]])
    (when-let [quantity (:value_quantity item)]
      [:div.detail-section
       [:h3 "Measurements"]
       [:div.detail-row
        [:strong "Value: "]
        [:span (:value quantity) " " (:unit quantity)]]])]])


(defn detail-view-container []
  (let [item            @(rf/subscribe [::subs/selected-item])
        loading-detail? @(rf/subscribe [::subs/loading-detail?])
        detail-error    @(rf/subscribe [::subs/item-detail-error])]
    [:div.detail-container
     (cond
       loading-detail? [:div.loading "Loading item details..."]

       detail-error [:div.error
                     [:p "Error loading item details: " (str detail-error)]
                     [:button {:on-click #(rf/dispatch [::events/close-detail])} "Back to list"]]

       item [detail-view-display item]

       :else [:div.empty "No item selected or details not found."])]))


(defn timeline-item [item]
  [:div.timeline-item.clickable
   {:on-click #(rf/dispatch [::events/select-item (:id item)])}
   [:h3 "Resource type: " (:resource_type item)]
   [:p "Date: " (or (:effective_dt item) "Not available")]
   (when-let [code (:code_text item)]
     [:p.code-text code])
   (when-let [categories (:categories item)]
     [:div.categories
      [:h4 (str "Categories: " (str/join " " categories))]])
   (when-let [quantity (:value_quantity item)]
     [:div.quantity
      [:p "Value: " (:value quantity) " " (:unit quantity)]])])


(defn maybe-scroll-and-clear [scroll-pos-sub]
  (let [scroll-pos @scroll-pos-sub]
    (when (some? scroll-pos)
      (js/setTimeout
        #(do
           (js/console.log (str "Attempting to scroll to: " scroll-pos))
           (js/window.scrollTo 0 scroll-pos)
           (rf/dispatch [::events/clear-timeline-scroll-position]))
        0))))


(defn render-timeline-list-contents []
  (let [items    @(rf/subscribe [::subs/timeline-items])
        loading? @(rf/subscribe [::subs/loading?])
        error    @(rf/subscribe [::subs/error])]
    [:div.timeline-container
     [:h2 "FHIR Timeline"]
     [:button.fetch-btn
      {:on-click #(rf/dispatch [::events/fetch-timeline])}
      "Fetch Timeline Data"]
     (cond
       loading? [:div.loading "Loading..."]
       error [:div.error "Error loading timeline: " (str error)]
       (some? items) [:div.timeline-list
                      (for [item items]
                        ^{:key (:id item)}
                        [timeline-item item])]
       :else [:div.empty "No timeline items found."])]))


(defn timeline-list []
  (let [scroll-pos-sub (rf/subscribe [::subs/timeline-scroll-position])]
    (r/create-class
      {:component-did-mount (fn [] (maybe-scroll-and-clear scroll-pos-sub))
       :component-did-update (fn [_ _] (maybe-scroll-and-clear scroll-pos-sub))
       :display-name "timeline-list"
       :reagent-render (fn [] [render-timeline-list-contents])})))


(defn main-panel []
  (let [selected-item-id @(rf/subscribe [::subs/selected-item-id])]
    [:div.main
     [:h1 "FHIR Timeline Viewer"]
     (if selected-item-id
       [detail-view-container]
       [timeline-list])]))
