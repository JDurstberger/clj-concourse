(ns clj-concourse.test-support.api-responses
  (:require
    [clj-concourse.test-support.json :as json]))

(def get-info
  {:success
   {:->wmk-stub
    (fn [{:keys [version worker-version external-url cluster-name]}]
      {:req [:GET "/api/v1/info"]
       :res [200 {:body (json/->default-json {:version        version
                                              :worker_version worker-version
                                              :external_url   external-url
                                              :cluster_name   cluster-name})}]})}})
