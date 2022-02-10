(ns clj-concourse.operations
  (:require
    [clj-http.client :as http]))

(defn no-op-attach-context
  [_ x]
  x)

(def operations
  {:list-all-teams
   {:http-method http/get
    :path        "/api/v1/teams"
    :ops         [:list-pipelines]
    :attach-context
    #(map
       (fn [team]
         (with-meta
           team
           (assoc %1 :context/team-name (:name team))))
       %2)}

   :list-all-pipelines
   {:http-method    http/get
    :path           "/api/v1/pipelines"
    :attach-context no-op-attach-context}

   :list-all-jobs
   {:http-method    http/get
    :path           "/api/v1/jobs"
    :attach-context no-op-attach-context}

   :get-server-info
   {:http-method    http/get
    :path           "/api/v1/info"
    :attach-context no-op-attach-context}

   :list-pipelines
   {:http-method http/get
    :path        "/api/v1/teams/{team-name}/pipelines"
    :ops         [:list-jobs]
    :attach-context
    #(map
       (fn [pipeline]
         (with-meta
           pipeline
           (assoc %1 :context/pipeline-name (:name pipeline))))
       %2)
    :param-key   :team}

   :list-jobs
   {:http-method http/get
    :path        "/api/v1/teams/{team-name}/pipelines/{pipeline-name}/jobs"
    :param-key   :pipeline
    :attach-context no-op-attach-context}})
