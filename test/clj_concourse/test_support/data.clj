(ns clj-concourse.test-support.data
  (:require
    [clj-concourse.specs]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [faker.internet :as faker-internet]
    [faker.lorem :as faker-lorem]))

(defn random-pos-int
  []
  (gen/generate (gen/such-that pos? (gen/int))))

(defn random-client
  [url]
  {:url url})

(defn create-sem-ver-generator
  [part-count]
  (fn []
    (gen/fmap (fn [vs] (clojure.string/join "." vs))
              (apply gen/tuple (repeat part-count (s/gen (s/int-in 0 100)))))))

(defn random-server-version
  []
  (gen/generate (s/gen (s/with-gen :clj-concourse/server-version
                                   (create-sem-ver-generator 3)))))

(defn random-worker-version
  []
  (gen/generate (s/gen (s/with-gen :clj-concourse/worker-version
                                   (create-sem-ver-generator 2)))))

(defn random-absolute-url
  []
  (str (gen/generate (s/gen #{"http" "https"}))
       "://ci."
       (faker-internet/domain-name)))

(def random-cluster-name
  #(->> (faker-lorem/words)
        (take (rand-int 5))
        (clojure.string/join " ")))

(defn random-token
  []
  (gen/generate (gen/such-that seq (gen/string-alphanumeric))))

(defn random-info
  []
  {:version        (random-server-version)
   :worker-version (random-worker-version)
   :external-url   (random-absolute-url)
   :cluster-name   (random-cluster-name)})

(def random-team-id random-pos-int)

(defn random-team
  []
  {:id   (random-team-id)
   :name (first (faker.lorem/words))})

(def random-job-id random-pos-int)

(defn random-job
  []
  {:id   (random-job-id)
   :name (first (faker.lorem/words))})

(defn random-pipeline
  []
  {:id   (random-pos-int)
   :name (first (faker.lorem/words))})
