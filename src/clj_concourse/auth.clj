(ns clj-concourse.auth
  (:require
    [clj-http.client :as http]))

(defn config->form-params
  [{:keys [username password]}]
  {:grant_type "password"
   :username   username
   :password   password
   :scope      "openid profile email federated:id groups"})

(defn create-access-token
  [{:keys [url] :as config}]
  (-> (http/post
        (str url "/sky/issuer/token")
        {:basic-auth  "fly:Zmx5"
         :form-params (config->form-params config)
         :as          :json-kebab-keys})
      (get-in [:body :access-token])))
