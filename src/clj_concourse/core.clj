(ns clj-concourse.core
  (:require
    [clj-concourse.body-coercion]
    [clj-concourse.json :as json]
    [clj-concourse.specs]
    [clj-http.client :as http]
    [clojure.spec.alpha :as s]))

(defn- get-access-token
  [{:keys [url username password]}]
  (-> (http/post (str url "/sky/issuer/token")
                 {:basic-auth  "fly:Zmx5"
                  :form-params {:grant_type "password"
                                :username   username
                                :password   password
                                :scope      "openid profile email federated:id groups"}
                  :as          :json-kebab-keys})
      (get-in [:body :access-token])))

(defn exception->error
  [e]
  (let [{:keys [error error-description]}
        (-> (ex-data e)
            :body
            (json/<-concourse-json))]
    {:error       error
     :description error-description}))

(defn client
  [{:keys [url] :as config}]
  {:pre [(s/valid? :clj-concourse/client-config config)]}
  (try
    (let [access-token (get-access-token config)]
      {:api-url (str url "/api/v1")
       :access-token access-token})
    (catch Exception e (exception->error e))))

(defn get-info
  [client]
  (-> (http/get (str (:api-url client) "/api/v1/info")
                {:as :json-kebab-keys})
      (:body)))

(comment
  (require '[dev]
           '[clojure.pprint :refer [pprint]])

  (-> (client {:url dev/server-url})
      (get-info)
      (pprint)))
