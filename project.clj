(defproject edu.bard/util.log "0.1.9-SNAPSHOT"
  :description "Logging baseline for Bard Clojure projects"
  :url "https://github.com/bardcollege/util.log"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :repl-options {:init-ns bard.util.log}
  :deploy-repositories {"github" {:url "https://maven.pkg.github.com/bardcollege/util.log"
                                  ; causes leiningen to look for LEIN_USERNAME and LEIN_PASSWORD
                                  :username :env
                                  :password :env
                                  :sign-releases false}
                        "clojars" {:url "https://repo.clojars.org"
                                   :username :env/clojars_user
                                   :password :env/clojars_password
                                   :sign-releases false}}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.logging "1.2.1"]
                 [org.apache.logging.log4j/log4j-api "2.15.0"]
                 [org.apache.logging.log4j/log4j-core "2.15.0"]
                 [org.apache.logging.log4j/log4j-jcl "2.15.0"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.15.0"]
                 [org.slf4j/slf4j-api "1.7.32"]
                 ;; TODO: figure out why this is necessary
                 [com.fasterxml.jackson.dataformat/jackson-dataformat-yaml "2.13.0"]]
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/log4j2-factory"]
  :plugins [[com.github.liquidz/antq "RELEASE"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" ":release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])

