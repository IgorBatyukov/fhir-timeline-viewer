(ns fhir-timeline-viewer.core
  (:require [fhir-timeline-viewer.events :as events]
            [fhir-timeline-viewer.views :as views]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]))

(rf/dispatch-sync [::events/initialize-db])
(rf/dispatch [::events/fetch-timeline])


(defn ^:dev/after-load mount-root
  "Renders the main application component to the DOM.
 Used for both initial mounting and hot-reload during development."
  []
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))


(defn init
  "Application initialization function that mounts the UI."
  []
  (mount-root))
