(ns user
  (:require
    [cheshire.core :as cc]
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [clojure.tools.namespace.repl :refer [refresh]]
    [com.stuartsierra.component :as component]
    [fhir-timeline-viewer.config :as config]
    [fhir-timeline-viewer.core :as app]
    [hugsql.core :as hugsql]))

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (app/api-system (config/read-config)))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(def config
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname "//localhost:5432/postgres"
   :user "postgres"
   :password "postgres"})

(hugsql/def-db-fns "hugsql/dev/fhir.sql")
(hugsql/def-db-fns "hugsql/queries.sql")

(defn read-json [json-resource]
  (with-open [rdr (io/reader json-resource)]
    (json/read-str (slurp rdr))))

(defn entry->payload [entry]
  (let [resource      (get entry "resource")
        resource-type (str/lower-case (get resource "resourceType"))]
    (when (contains? #{"observation" "condition"} resource-type)
      {:resource-type resource-type
       :resource-id (get resource "id")
       :patient-id (-> resource
                       (get "subject")
                       (get "reference")
                       (str/split #"/")
                       (last))
       :content (json/write-str resource)})))

(defn get-jsons []
  (->> (io/resource "fhir_examples")
       (io/file)
       (.listFiles)))

(defn seed-db [config]
  (let [payloads (->> (get-jsons)
                      (map read-json)
                      (mapcat #(get % "entry"))
                      (keep entry->payload))]
    (loop [p payloads]
      (when (seq p)
        (do
          (seed-fhir-resources-table config (first p))
          (log/info "uploaded"))

        (recur (rest p))))))

(defn read-jsonb-object [obj]
  (when obj
    (some-> obj
            (.getValue)
            (cc/parse-string true))))

(defn reset-db []
  (drop-table! config {:table-name "fhir_resources"})
  (drop-table! config {:table-name "flyway_schema_history"}))

(comment
  (seed-fhir-resources-table config)
  (reset-db)
  (seed-db config)

  (get-fhir-timeline config {:patient-id "example-2"})

  (->> (get-fhir-timeline config)
       (map (fn [resource]
              (assoc resource
                :categories (read-jsonb-object (:categories resource))
                :value_quantity (read-jsonb-object (:value_quantity resource)))))))
