(ns clj-concourse.core
  (:require
    [clj-concourse.auth :as auth]
    [clj-concourse.body-coercion]
    [clj-concourse.json :as json]
    [clj-concourse.specs]
    [clj-http.client :as http]
    [clojure.spec.alpha :as s]
    [clojure.string :as str]))

(defn exception->error
  [e]
  (let [{:keys [error error-description]}
        (-> (ex-data e)
            :body
            (json/<-concourse-json))]
    (if error
      {:error       error
       :description error-description}
      {:error       (ex-cause e)
       :description (ex-message e)})))

(defn client
  [{:keys [url] :as config}]
  {:pre [(s/valid? :clj-concourse/client-config config)]}
  (try
    (let [access-token (auth/create-access-token config)]
      {:url          url
       :access-token access-token})
    (catch Exception e (exception->error e))))

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
                        :context-builder identity}})
(defn parse-response-body
  [response]
  (-> response
      :body
      (json/<-concourse-json)))

(defn path->template-names
  [path]
  (->> (re-matcher #"(\{(.*?)\})" path)
       ((fn [matcher] (repeatedly #(re-find matcher))))
       (map last)
       (take-while some?)))

(defn build-path
  [context path]
  (let [template-names (path->template-names path)]
    (reduce (fn [path template-name]
              (str/replace path
                           (str "{" template-name "}")
                           (get context (keyword "context" template-name))))
            path
            template-names)))

(defn invoke
  [{:keys [url access-token]}
   {:keys [op] :as command}]
  (let [{:keys [http-method path context-builder ops]} (op operations)
        context (-> command :context meta)
        response (http-method (str url (build-path context path))
                              {:oauth-token      access-token
                               :throw-exceptions false})]
    (if (http/success? response)
      {:data (context-builder (parse-response-body response))
       :ops  ops}
      {:error {:status      (:status response)
               :description (:body response)}})))

(comment
  (require '[dev]
           '[clojure.pprint :refer [pprint]])

  (def config {:url      dev/server-url
               :username dev/username
               :password dev/password})
  (def c (client config))

  (pprint (invoke c {:op :get-server-info}))
  (def teams (:data (invoke c {:op :list-all-teams})))
  (meta (first teams))
  (invoke c {:op :list-pipelines
             :context (first teams)})

  (->> (invoke c {:op :list-all-pipelines})
       (first)))
