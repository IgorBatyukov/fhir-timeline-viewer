(ns fhir-timeline-viewer.db)

(def default-db
  "The default/initial application state.
   Contains empty timeline data, loading states, and error handling fields."
  {:timeline-items []                                       ;; List of timeline items from the server
   :total-items 0                                           ;; Total count of timeline items
   :loading? false                                          ;; Flag indicating if timeline is being loaded
   :error nil                                               ;; Error message for timeline loading failures
   :selected-item-id nil                                    ;; ID of the currently selected timeline item
   :current-item-detail nil                                 ;; Detailed data for the selected item
   :loading-detail? false                                   ;; Flag indicating if item detail is being loaded
   :item-detail-error nil                                   ;; Error message for item detail loading failures
   :timeline-scroll-position nil})                          ;; Saved scroll position in timeline view

