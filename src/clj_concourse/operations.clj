(ns clj-concourse.operations
  (:require
    [clj-http.client :as http]))

(def operations
  {:list-all-teams     {:http-method http/get
                        :path        "/api/v1/teams"
                        :ops         [:list-pipelines]
                        :context-builder
                        #(map
                           (fn [team]
                             (with-meta
                               team
                               {:context/team-name (:name team)}))
                           %)}

   :list-all-pipelines {:http-method     http/get
                        :path            "/api/v1/pipelines"
                        :context-builder identity}
   :list-all-jobs      {:http-method     http/get
                        :path            "/api/v1/jobs"
                        :context-builder identity}
   :get-server-info    {:http-method     http/get
                        :path            "/api/v1/info"
                        :context-builder identity}
   :list-pipelines     {:http-method     http/get
                        :path            "/api/v1/teams/{team-name}/pipelines"
                        :context-builder identity
                        :param-key       :team}})
