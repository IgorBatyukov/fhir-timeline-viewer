(ns fhir-timeline-viewer.routes
  (:require [io.pedestal.http.route :as route]
            [fhir-timeline-viewer.handlers :as handlers]))

(def routes
  (route/expand-routes
    #{["/" :get handlers/index :route-name :index]
      ["/health" :get handlers/health-check :route-name :health-check]
      ["/timeline" :get handlers/timeline :route-name :resources/timeline]
      ["/timeline/:id" :get handlers/resource :route-name :resources/resource]}))
