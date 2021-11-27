(ns clj-concourse.body-coercion
  (:require
    [camel-snake-kebab.core :refer [->kebab-case-keyword]]
    [clj-http.client :as http]))

(defmethod http/coerce-response-body :json-kebab-keys [req resp]
  (http/coerce-json-body req resp (memoize ->kebab-case-keyword) false))
