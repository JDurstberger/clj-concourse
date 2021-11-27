(ns clj-concourse.client-test
  (:require
    [clj-concourse.core :as concourse]
    [clj-concourse.test-support.api-responses :as api-responses]
    [clj-concourse.test-support.data :as data]
    [clj-wiremock.core :as wmk]
    [clojure.test :refer :all]
    [freeport.core :refer [get-free-port!]]))

(def wiremock-port (get-free-port!))
(def server-url (str "http://localhost:" wiremock-port))

(use-fixtures :once
  (partial wmk/wiremock-fixture {:port wiremock-port}))

(deftest returns-server-info
  (let [api-response (:success api-responses/get-info)
        client (concourse/client {:url server-url})
        info (data/random-info)
        expected-info {:version (:version info)
                       :worker-version (:worker-version info)
                       :external-url (:external-url info)
                       :cluster-name (:cluster-name info)}]
    (wmk/with-stubs
      [((:->wmk-stub api-response) info)]
      (is (= expected-info (concourse/get-info client))))))
