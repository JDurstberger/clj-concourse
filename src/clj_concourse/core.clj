(ns clj-concourse.core
  (:require
    [clj-concourse.auth :as auth]
    [clj-concourse.body-coercion]
    [clj-concourse.json :as json]
    [clj-concourse.specs]
    [clj-http.client :as http]
    [clojure.spec.alpha :as s]))

(defn exception->error
  [e]
  (let [{:keys [error error-description]}
        (-> (ex-data e)
            :body
            (json/<-concourse-json))]
    (if error
      {:error       error
       :description error-description}
      {:error       (ex-cause e)
       :description (ex-message e)})))

(defn client
  [{:keys [url] :as config}]
  {:pre [(s/valid? :clj-concourse/client-config config)]}
  (try
    (let [access-token (auth/create-access-token config)]
      {:url          url
       :access-token access-token})
    (catch Exception e (exception->error e))))

(def operations
  {:list-all-teams     {:http-method http/get
                        :path        "/api/v1/teams"}
   :list-all-pipelines {:http-method http/get
                        :path        "/api/v1/pipelines"}
   :list-all-jobs      {:http-method http/get
                        :path        "/api/v1/jobs"}
   :get-server-info    {:http-method http/get
                        :path        "/api/v1/info"}})

(defn invoke
  [{:keys [url access-token]}
   {:keys [op]}]
  (let [{:keys [http-method path]} (op operations)
        response (http-method (str url path)
                              {:oauth-token      access-token
                               :throw-exceptions false})]
    (if (http/success? response)
      (-> response
          :body
          (json/<-concourse-json))
      {:error {:status      (:status response)
               :description (:body response)}})))

(comment
  (require '[dev]
           '[clojure.pprint :refer [pprint]])

  (def config {:url      dev/server-url
               :username dev/username
               :password dev/password})
  (def c (client config))

  (pprint (invoke c {:op :get-server-info}))
  (pprint (invoke c {:op :list-teams}))


  (->> (invoke c {:op :list-all-pipelines})
       (first)))
