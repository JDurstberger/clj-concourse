(ns clj-concourse.json
  (:require
    [camel-snake-kebab.core :refer [->snake_case_string
                                    ->kebab-case-keyword]]
    [jason.core :as json]))

(declare <-concourse-json
         ->concourse-json)

#_:clj-kondo/ignore

(json/defcoders
  concourse
  :encode-key-fn (json/->encode-key-fn ->snake_case_string)
  :decode-key-fn (json/->decode-key-fn ->kebab-case-keyword)
  :pretty true)

