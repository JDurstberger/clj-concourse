(ns clj-concourse.path-test
  (:require
    [clj-concourse.core :as concourse]
    [clojure.test :refer :all]))

(deftest does-not-change-non-templated-path
  (let [path "/v1/api/some/path"]
    (is (= path (concourse/build-path {} path)))))

(deftest replaces-single-part-in-templated-path
  (let [context {:context/team-name "team1"}
        path "/v1/api/teams/{team-name}"
        expected-path "/v1/api/teams/team1"]
    (is (= expected-path (concourse/build-path context path)))))

(deftest replaces-multiple-parts-in-templated-path
  (let [context {:context/team-name "team1"
                 :context/pipeline-name "pipeline1"}
        path "/v1/api/teams/{team-name}/pipelines/{pipeline-name}"
        expected-path "/v1/api/teams/team1/pipelines/pipeline1"]
    (is (= expected-path (concourse/build-path context path)))))

