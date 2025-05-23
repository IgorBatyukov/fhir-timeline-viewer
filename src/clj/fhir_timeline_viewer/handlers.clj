(ns fhir-timeline-viewer.handlers
  (:require [cheshire.core :as cc]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hugsql.adapter.next-jdbc :as next-jdbc-adapter]
            [hugsql.core :as hugsql]
            [io.pedestal.log :as log]))

(hugsql/def-db-fns "hugsql/queries.sql"
                   {:adapter (next-jdbc-adapter/hugsql-adapter-next-jdbc)})


(defn assoc-response [ctx status body]
  (assoc ctx :response {:status status
                        :body body}))


(defn with-error-handling
  "Wraps a handler function with standard error handling.
   handler-fn should accept ctx and return an updated ctx with a response."
  [handler-fn]
  (fn [ctx]
    (try
      (handler-fn ctx)
      (catch Exception e
        (log/error :error (.getMessage e))
        (assoc-response ctx 500 {:error "Internal server error"})))))


(defn ctx->deps-params [ctx]
  {:params (-> ctx :request :path-params)
   :datasource (-> ctx :dependencies :datasource)})


(defn read-jsonb-object [obj]
  (when obj
    (some-> obj
            (.getValue)
            (cc/parse-string true))))


(defn index [_]
  (if-let [resource (io/resource "public/index.html")]
    {:status 200
     :headers {"Content-Security-Policy" "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'"
               "X-Content-Type-Options" "nosniff"}
     :body (slurp resource)}
    {:status 404
     :body "Index.html not found"}))


(def health-check
  {:name :health-check
   :enter (fn [ctx]
            (assoc-response ctx 200 {:status "OK"}))})


(defn fhir-resource->response [resource]
  (when resource
    (assoc resource
      :resource_type (str/capitalize (:resource_type resource))
      :categories (read-jsonb-object (:categories resource))
      :value_quantity (read-jsonb-object (:value_quantity resource)))))


(defn get-timeline [ctx]
  (let [{:keys [datasource params]} (ctx->deps-params ctx)
        db-response (->> (get-fhir-timeline (datasource) params)
                         (keep fhir-resource->response))]
    (assoc-response ctx 200 {:items db-response
                             :total (count db-response)})))


(defn get-resource [ctx]
  (let [{:keys [datasource params]} (ctx->deps-params ctx)
        resource (get-resource-by-id (datasource) params)
        response (fhir-resource->response resource)]
    (if response
      (assoc-response ctx 200 response)
      (assoc-response ctx 404 {:error (str "Can't find resource by ID - " (:id params))}))))


(def timeline
  {:name :timeline
   :enter (with-error-handling get-timeline)})


(def resource
  {:name :resource
   :enter (with-error-handling get-resource)})
