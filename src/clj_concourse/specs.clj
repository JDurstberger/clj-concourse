(ns clj-concourse.specs
  (:require
    [clojure.spec.alpha :as s]))

(defn server-version?
  [s]
  (re-matches #"^\d+\.\d+\.\d+$" s))

(s/def :clj-concourse/server-version server-version?)

(defn worker-version?
  [s]
  (re-matches #"^\d+\.\d+$" s))

(s/def :clj-concourse/worker-version worker-version?)

;; Taken with gratitude from https://ihateregex.io/expr/url/
(def absolute-url-regex
  #"https:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()!@:%_\+.~#?&\/\/=]*)")

(defn absolute-url?
  [s]
  (re-matches absolute-url-regex s))

(s/def :clj-concourse/absolute-url absolute-url?)

(rand-nth ["http" "https"])
