(ns fhir-timeline-viewer.config
  (:require
    [aero.core :as aero]
    [clojure.java.io :as io]))

(defn read-config
  "Reads and parses the application configuration from config.edn.
   Returns a map containing the application configuration settings."
  []
  (->
    "config.edn"
    (io/resource)
    (aero/read-config)))
