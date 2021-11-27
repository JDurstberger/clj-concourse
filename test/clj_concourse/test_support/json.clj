(ns clj-concourse.test-support.json
  (:require
    [jason.core :as jason]))

(declare ->default-json
         <-default json)

(jason/defcoders default :pretty true)
