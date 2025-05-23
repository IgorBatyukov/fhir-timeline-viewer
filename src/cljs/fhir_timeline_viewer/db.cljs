(ns fhir-timeline-viewer.db)

(def default-db
  {:timeline-items []
   :total-items 0
   :loading? false
   :error nil
   :selected-item-id nil
   :current-item-detail nil
   :loading-detail? false
   :item-detail-error nil
   :timeline-scroll-position nil})
