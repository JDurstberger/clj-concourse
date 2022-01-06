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
      {:url          url
       :access-token access-token})
    (catch Exception e (exception->error e))))

(def operations
  {:list-teams      {:http-method http/get
                     :path        "/api/v1/teams"}
   :get-server-info {:http-method http/get
                     :path        "/api/v1/info"}})

(defn invoke
  [{:keys [url access-token]}
   {:keys [op]}]
  (let [{:keys [http-method path]} (op operations)]
    (-> (http-method (str url path)
                     {:oauth-token access-token
                      :as          :json-kebab-keys})
        (:body))))

(comment
  (require '[dev]
           '[clojure.pprint :refer [pprint]])

  (def c (client {:url      dev/server-url
                  :username dev/username
                  :password dev/password}))

  (println c)
  (pprint (get-info c))
  (pprint (get-teams c)))
