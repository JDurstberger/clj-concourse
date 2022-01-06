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

(def client-config
  {:url      server-url
   :username "username"
   :password "password"})

(deftest client-creation
  (testing "throws error when url not provided"
    (is (thrown? AssertionError (concourse/client (dissoc client-config :url)))))

  (testing "throws error when url not a url"
    (is (thrown? AssertionError (concourse/client (assoc client-config :url "not-a-url")))))

  (testing "throws error when username not provided"
    (is (thrown? AssertionError (concourse/client (dissoc client-config :username)))))

  (testing "throws error when password not provided"
    (is (thrown? AssertionError (concourse/client (dissoc client-config :password)))))

  (testing "returns error when invalid credentials provided"
    (let [api-response (:invalid-credentials api-responses/get-token)
          expected-error {:error       (:error api-response)
                          :description (:description api-response)}]
      (wmk/with-stubs
        [((:->wmk-stub api-response))]
        (is (= expected-error (concourse/client client-config))))))

  (testing "returns client when creation succeeds"
    (let [api-response (:success api-responses/get-token)
          token (data/random-token)
          expected-client {:url          server-url
                           :access-token token}]
      (wmk/with-stubs
        [((:->wmk-stub api-response) token)]
        (is (= expected-client (concourse/client client-config)))))))

(deftest returns-server-info
  (let [api-response (:success api-responses/get-info)
        client (data/random-client server-url)
        info (data/random-info)
        expected-info {:version        (:version info)
                       :worker-version (:worker-version info)
                       :external-url   (:external-url info)
                       :cluster-name   (:cluster-name info)}]
    (wmk/with-stubs
      [((:->wmk-stub api-response) info)]
      (is (= expected-info (concourse/invoke client {:op :get-server-info}))))))

(deftest returns-teams
  (let [api-response (:success api-responses/get-teams)
        client (data/random-client server-url)
        team (data/random-team)
        expected-team {:id   (:id team)
                       :name (:name team)}]

    (wmk/with-stubs
      [((:->wmk-stub api-response) [team])]
      (is (= [expected-team] (concourse/invoke client {:op :list-teams}))))))
