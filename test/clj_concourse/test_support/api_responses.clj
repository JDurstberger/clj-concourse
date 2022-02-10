(ns clj-concourse.test-support.api-responses
  (:require
    [clj-concourse.test-support.json :as json]))

(def get-token
  {:success
   {:->wmk-stub (fn [access-token]
                  {:req [:POST "/sky/issuer/token"]
                   :res [200 {:body (json/->default-json
                                      {:access_token access-token})}]})}

   :invalid-credentials
   (let [error "access_denied"
         description "Invalid username or password"]
     {:error       error
      :description description
      :->wmk-stub  (fn []
                     {:req [:POST "/sky/issuer/token"]
                      :res [401 {:body
                                 (json/->default-json
                                   {:error             error
                                    :error_description description})}]})})})

(def get-info
  {:success
   {:->wmk-stub
    (fn [{:keys [version worker-version external-url cluster-name]}]
      {:req [:GET "/api/v1/info"]
       :res [200 {:body (json/->default-json
                          {:version        version
                           :worker_version worker-version
                           :external_url   external-url
                           :cluster_name   cluster-name})}]})}})

(def get-teams
  {:success
   {:->wmk-stub
    (fn [teams]
      {:req [:GET "/api/v1/teams"]
       :res [200 {:body (json/->default-json
                          (map #(hash-map :id (:id %)
                                          :name (:name %))
                               teams))}]})}})

(defn pipelines->body
  [pipelines]
  (json/->default-json
    (map #(hash-map :id (:id %)
                    :name (:name %))
         pipelines)))

(defn get-team-pipelines
  [team-name]
  {:success
   {:->wmk-stub
    (fn [pipelines]
      {:req [:GET (str "/api/v1/teams/" team-name "/pipelines")]
       :res [200 {:body (pipelines->body pipelines)}]})}})

(def get-jobs
  {:success
   {:->wmk-stub
    (fn [jobs]
      {:req [:GET "/api/v1/jobs"]
       :res [200 {:body (json/->default-json
                          (map #(hash-map :id (:id %)
                                          :name (:name %))
                               jobs))}]})}})

(def get-pipelines
  {:success
   {:->wmk-stub
    (fn [pipelines]
      {:req [:GET "/api/v1/pipelines"]
       :res [200 {:body (pipelines->body pipelines)}]})}})

(def generic-error
  (let [body "generic server error body"]
    {:body body
     :->wmk-stub
     (fn []
       {:req [:GET #"/.*"]
        :res [500 {:body body}]})}))
