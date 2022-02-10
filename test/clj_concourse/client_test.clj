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
    (is (thrown? AssertionError
                 (concourse/client (dissoc client-config :url)))))

  (testing "throws error when url not a url"
    (is (thrown? AssertionError
                 (concourse/client (assoc client-config :url "not-a-url")))))

  (testing "throws error when username not provided"
    (is (thrown? AssertionError
                 (concourse/client (dissoc client-config :username)))))

  (testing "throws error when password not provided"
    (is (thrown? AssertionError
                 (concourse/client (dissoc client-config :password)))))

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

      (let [result (concourse/invoke client {:op :get-server-info})
            server-info (:data result)]

        (testing "no errors"
          (is (nil? (:error result))))

        (testing "server info is returned"
          (is (= expected-info server-info)))))))

(deftest returns-all-jobs
  (let [api-response (:success api-responses/get-jobs)
        client (data/random-client server-url)
        job (data/random-job)
        expected-job {:id   (:id job)
                      :name (:name job)}]

    (wmk/with-stubs
      [((:->wmk-stub api-response) [job])]
      (let [result (concourse/invoke client {:op :list-all-jobs})
            jobs (:data result)]

        (testing "no errors"
          (is (nil? (:error result))))

        (testing "jobs are returned"
          (is (= [expected-job] jobs)))))))

(deftest returns-all-pipelines
  (let [api-response (:success api-responses/get-pipelines)
        client (data/random-client server-url)
        pipeline (data/random-pipeline)
        expected-pipeline {:id   (:id pipeline)
                           :name (:name pipeline)}]

    (wmk/with-stubs
      [((:->wmk-stub api-response) [pipeline])]
      (let [result (concourse/invoke client {:op :list-all-pipelines})
            pipelines (:data result)]

        (testing "no errors"
          (is (nil? (:error result))))

        (testing "pipelines are returned"
          (is (= [expected-pipeline] pipelines)))))))

(deftest returns-all-teams
  (let [api-response (:success api-responses/get-teams)
        client (data/random-client server-url)
        team (data/random-team)
        expected-team {:id   (:id team)
                       :name (:name team)}]

    (wmk/with-stubs
      [((:->wmk-stub api-response) [team])]
      (let [result (concourse/invoke client {:op :list-all-teams})
            teams (:data result)
            team (first teams)]

        (testing "no errors"
          (is (nil? (:error result))))

        (testing "teams are returned"
          (is (= [expected-team] teams)))

        (testing "operations are returned"
          (is (= [:list-pipelines] (:ops result))))

        (testing "context is attached to team"
          (is (= {:context/team-name (:name team)}
                 (meta team))))))))

(deftest returns-pipelines-for-team
  (let [team (data/random-team)
        pipeline (data/random-pipeline)
        teams-response (:success api-responses/get-teams)
        pipelines-response (:success (api-responses/get-team-pipelines
                                       (:name team)))
        client (data/random-client server-url)
        expected-pipeline {:id   (:id pipeline)
                           :name (:name pipeline)}]

    (wmk/with-stubs
      [((:->wmk-stub teams-response) [team])
       ((:->wmk-stub pipelines-response) [pipeline])]
      (let [result
            (as-> (concourse/invoke client {:op :list-all-teams})
                  $
                  (-> $ :data first)
                  (concourse/invoke client
                                    {:op   :list-pipelines
                                     :team $}))

            pipelines (:data result)
            pipeline (first pipelines)]

        (testing "no errors"
          (is (nil? (:error result))))

        (testing "pipelines are returned"
          (is (= [expected-pipeline] pipelines)))

        (testing "operations are returned"
          (is (= [:list-jobs] (:ops result))))

        (testing "context is attached to pipeline"
          (is (= {:context/team-name     (:name team)
                  :context/pipeline-name (:name pipeline)}
                 (meta pipeline))))))))

(deftest returns-jobs-for-pipeline
  (let [team (data/random-team)
        pipeline (data/random-pipeline)
        job (data/random-job)
        teams-response (:success api-responses/get-teams)
        pipelines-response (:success (api-responses/get-team-pipelines
                                       (:name team)))
        jobs-response (:success (api-responses/get-pipeline-jobs
                                  (:name team)
                                  (:name pipeline)))
        client (data/random-client server-url)
        expected-job {:id   (:id job)
                      :name (:name job)}]

    (wmk/with-stubs
      [((:->wmk-stub teams-response) [team])
       ((:->wmk-stub pipelines-response) [pipeline])
       ((:->wmk-stub jobs-response) [job])]
      (let [result
            (as-> (concourse/invoke client {:op :list-all-teams})
                  $
                  (-> $ :data first)
                  (concourse/invoke client
                                    {:op   :list-pipelines
                                     :team $})
                  (-> $ :data first)
                  (concourse/invoke client
                                    {:op   :list-jobs
                                     :pipeline $}))

            jobs (:data result)]

        (testing "no errors"
          (is (nil? (:error result))))

        (testing "jobs are returned"
          (is (= [expected-job] jobs)))))))


(deftest handles-server-error
  (let [api-response api-responses/generic-error
        client (data/random-client server-url)]

    (wmk/with-stubs
      [((:->wmk-stub api-response))]
      (is (= {:error {:status      500
                      :description (:body api-response)}}
             (concourse/invoke client {:op :list-all-jobs}))))))
