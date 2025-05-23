(ns fhir-timeline-viewer.components.pedestal
  (:require
    [com.stuartsierra.component :as component]
    [fhir-timeline-viewer.routes :as routes]
    [io.pedestal.http :as http]
    [io.pedestal.http.content-negotiation :as content-negotiation]
    [io.pedestal.interceptor :as interceptor]))

(def cors-headers
  {"Access-Control-Allow-Origin" "*"
   "Access-Control-Allow-Methods" "GET, POST, OPTIONS"
   "Access-Control-Allow-Headers" "Content-Type, Authorization"
   "Access-Control-Max-Age" "86400"})

(def cors
  (interceptor/interceptor
    {:name ::cors
     :leave (fn [context]
              (let [response (:response context)]
                (assoc context :response
                               (update response :headers
                                       #(merge % cors-headers)))))}))

(def cors-preflight
  (interceptor/interceptor
    {:name ::cors-preflight
     :enter (fn [context]
              (let [request  (:request context)
                    response {:status 200
                              :headers cors-headers
                              :body ""}]
                (if (= :options (:request-method request))
                  (assoc context :response response)
                  context)))}))

(def content-negotiation-interceptor
  (content-negotiation/negotiate-content ["application/json"
                                          "application/javascript"
                                          "text/html"]))

(defn inject-dependencies
  [dependencies]
  (interceptor/interceptor
    {:name ::inject-dependencies
     :enter (fn [context]
              (assoc context :dependencies dependencies))}))

(defrecord PedestalComponent [config datasource]
  component/Lifecycle

  (start [component]
    (let [server (-> {::http/host "0.0.0.0"
                      ::http/routes routes/routes
                      ::http/port 8080
                      ::http/type :jetty
                      ::http/join? false
                      ::http/resource-path "/public"}
                     (http/default-interceptors)
                     (update ::http/interceptors concat
                             [cors
                              cors-preflight
                              (inject-dependencies component)
                              content-negotiation-interceptor
                              http/json-body
                              http/html-body])
                     (http/create-server)
                     (http/start))]
      (assoc component :server server)))

  (stop [component]
    (when-let [server (:server component)]
      (http/stop server))
    (assoc component :server nil)))

(defn new-pedestal-component [config]
  (map->PedestalComponent {:config config}))