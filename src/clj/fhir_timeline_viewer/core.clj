(ns fhir-timeline-viewer.core
  (:require
    [com.stuartsierra.component :as component]
    [fhir-timeline-viewer.components.pedestal :as pedestal-component]
    [fhir-timeline-viewer.config :as config]
    [next.jdbc.connection :as connection])
  (:import com.zaxxer.hikari.HikariDataSource
           (org.flywaydb.core Flyway)))

(defn api-system [config]
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
