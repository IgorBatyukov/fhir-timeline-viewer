(ns fhir-timeline-viewer.core
  (:require
    [com.stuartsierra.component :as component]
    [fhir-timeline-viewer.components.pedestal :as pedestal-component]
    [fhir-timeline-viewer.config :as config]
    [next.jdbc.connection :as connection])
  (:import com.zaxxer.hikari.HikariDataSource
           (org.flywaydb.core Flyway)))

(defn api-system
  "Creates a component system for the application.
   Sets up the database connection pool and web server components.

   Parameters:
   - config: A map containing application configuration

   Returns:
   - A component system map containing all application components"
  [config]
  (component/system-map
    :datasource (connection/component
                  HikariDataSource
                  (assoc (:db-spec config)
                    :init-fn (fn [datasource]
                               (-> (Flyway/configure)
                                   (.dataSource datasource)
                                   (.locations (into-array String ["classpath:migrations"]))
                                   (.baselineOnMigrate true)
                                   (.load)
                                   (.migrate)))))
    :pedestal-component (component/using
                          (pedestal-component/new-pedestal-component config)
                          [:datasource])))


(defn -main []
  (let [system (-> (config/read-config)
                   (api-system)
                   (component/start-system))]
    (.addShutdownHook
      (Runtime/getRuntime)
      (Thread. #(component/stop-system system)))))
