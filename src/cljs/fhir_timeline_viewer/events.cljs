(ns fhir-timeline-viewer.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [fhir-timeline-viewer.db :as db]
            [re-frame.core :as rf]))

(rf/reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))


(rf/reg-event-fx
  ::fetch-timeline
  (fn [{:keys [db]} _]
    {:db (assoc db :loading? true :error nil)
     :http-xhrio {:method :get
                  :uri "/timeline"
                  :timeout 5000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [::fetch-timeline-success]
                  :on-failure [::fetch-timeline-failure]}}))


(rf/reg-event-db
  ::fetch-timeline-success
  (fn [db [_ response]]
    (js/console.log "Timeline response received:", response)
    (-> db
        (assoc :loading? false)
        (assoc :timeline-items (:items response))
        (assoc :total-items (:total response)))))


(rf/reg-event-db
  ::fetch-timeline-failure
  (fn [db [_ response]]
    (js/console.error "Error fetching timeline:", response)
    (-> db
        (assoc :loading? false)
        (assoc :error (str "Timeline fetch failed: " response)))))


(rf/reg-event-fx
  ::select-item
  (fn [{:keys [db]} [_ item-id]]
    (let [current-scroll js/window.pageYOffset]
      (js/console.log "Storing scroll position:" current-scroll "for item ID:" item-id)
      {:db (-> db
               (assoc :selected-item-id item-id)
               (assoc :loading-detail? true)
               (assoc :current-item-detail nil)
               (assoc :item-detail-error nil)
               (assoc :timeline-scroll-position current-scroll))
       :http-xhrio {:method :get
                    :uri (str "/timeline/" item-id)
                    :timeout 5000
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success [::fetch-item-detail-success]
                    :on-failure [::fetch-item-detail-failure]}})))


(rf/reg-event-db
  ::fetch-item-detail-success
  (fn [db [_ response]]
    (js/console.log "Item detail response received:", response)
    (-> db
        (assoc :loading-detail? false)
        (assoc :current-item-detail response))))


(rf/reg-event-db
  ::fetch-item-detail-failure
  (fn [db [_ response]]
    (js/console.error "Error fetching item detail:", response)
    (-> db
        (assoc :loading-detail? false)
        (assoc :item-detail-error (str "Failed to fetch item details: " response)))))


(rf/reg-event-db
  ::close-detail
  (fn [db _]
    (-> db
        (assoc :selected-item-id nil)
        (assoc :current-item-detail nil)
        (assoc :loading-detail? false)
        (assoc :item-detail-error nil))))


(rf/reg-event-db
  ::clear-timeline-scroll-position
  (fn [db _]
    (js/console.log "Clearing timeline scroll position.")
    (assoc db :timeline-scroll-position nil)))
