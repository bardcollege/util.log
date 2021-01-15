(defproject edu.bard/idm.log "0.1.3-SNAPSHOT"
  :description "Logging baseline for Bard IdM"
  :url "https://github.com/bardcollege/idm.log"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :repl-options {:init-ns idm.log}
  :deploy-repositories [["github" {:url "https://maven.pkg.github.com/bardcollege/idm.log"
                                   ; causes leiningen to look for LEIN_USERNAME and LEIN_PASSWORD
                                   :username :env
                                   :password :env
                                   :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.apache.logging.log4j/log4j-api "2.13.3"]
                 [org.apache.logging.log4j/log4j-core "2.13.3"]
                 [org.apache.logging.log4j/log4j-jcl "2.13.3"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.13.3"]
                 [org.slf4j/slf4j-api "1.7.30"]
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-yaml "2.11.1"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" ":release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])

