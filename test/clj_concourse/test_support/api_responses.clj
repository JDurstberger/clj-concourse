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
       :res [200 {:body (json/->default-json {:version        version
                                              :worker_version worker-version
                                              :external_url   external-url
                                              :cluster_name   cluster-name})}]})}})
