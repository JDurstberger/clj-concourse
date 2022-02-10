(ns clj-concourse.path
  (:require
    [clojure.string :as str]))

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
