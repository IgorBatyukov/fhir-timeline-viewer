(ns fhir-timeline-viewer.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [fhir-timeline-viewer.events :as events]
            [fhir-timeline-viewer.views :as views]))

(rf/dispatch-sync [::events/initialize-db])


(defn ^:dev/after-load mount-root []
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))


(defn init []
  (mount-root))
