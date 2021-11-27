(ns clj-concourse.core
  (:require
    [clj-concourse.body-coercion]
    [clj-http.client :as http]))

(defn client
  [{:keys [url]}]
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
