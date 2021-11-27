(ns clj-concourse.core
  (:require
    [clj-concourse.body-coercion]
    [clj-concourse.specs]
    [clj-http.client :as http]
    [clojure.spec.alpha :as s]))

(defn client
  [{:keys [url] :as config}]
  {:pre [(s/valid? :clj-concourse/client-config config)]}
  {:api-url (str url "/api/v1")})

(defn get-info
  [client]
  (-> (http/get (str (:api-url client) "/info")
                {:as :json-kebab-keys})
      (:body)))

(comment
  (require '[dev]
           '[clojure.pprint :refer [pprint]])

  (-> (client {:url dev/server-url})
      (get-info)
      (pprint)))
