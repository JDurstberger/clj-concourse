# clj-concourse

A convenient way to interact with concourse-ci

## Usage

### Construction

`clj-concourse` provides a client for interaction with the Concourse CI API.  
To construct a client:

```clojure
(require '[clj-concourse.core :as concourse])

(def concourse/client {:url "https://concourse.example.com"})
```

### Examples

```clojure
;;Get basic server information
(concourse/get-info client)
;=> {:version "6.5.0"
;    :worker-version "2.2"
;    ...}
 ```

## Development

`go` is used as the entry-point for all tasks.
When no arguments are passed the default checks are run.

Use `go tasks` to discover all tasks.

### REPL

For the best user experience run the REPL with the `:test` alias.

### Rich Comments

Rich comments can be found in some files.  
Some invocations might depend on user specific data like credentials or a server url.  
In order to make working with those more convenient they are imported from the `dev` namespace.

Copy the `dev.clj.tpl` file to `dev.clj` and add the desired values.  
The `dev.clj` file is ignored by git so no user specific data is commited to the repository.
