(ns clj-concourse.core
  (:require
    [clj-concourse.auth :as auth]
    [clj-concourse.body-coercion]
    [clj-concourse.json :as json]
    [clj-concourse.operations :refer [operations]]
    [clj-concourse.path :as path]
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

(defn parse-response-body
  [response]
  (-> response
      :body
      (json/<-concourse-json)))

(defn invoke
  [{:keys [url access-token]}
   {:keys [op] :as command}]
  (let [{:keys [http-method path attach-context ops param-key]} (op operations)
        context (meta (get command param-key))
        url (str url (path/build-path context path))
        response (http-method url
                              {:oauth-token      access-token
                               :throw-exceptions false})]
    (if (http/success? response)
      {:data (attach-context context (parse-response-body response))
       :ops  ops}
      {:error {:status      (:status response)
               :description (:body response)}})))

(comment
  (require '[dev]
           '[clojure.pprint :refer [pprint]])

  (def config {:url      dev/server-url
               :username dev/username
               :password dev/password})
  (def c (client config))

  (as->
    (invoke c {:op :list-all-teams})
    $
    (-> $ :data first)
    (invoke c {:op   :list-pipelines
               :team $})
    (-> $ :data first)
    (invoke c {:op       :list-jobs
               :pipeline $})
    (-> $ :data first)))

