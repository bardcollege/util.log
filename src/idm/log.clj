(ns idm.log
  "Logging helper macros"
  (:require [clojure.tools.logging :as log])
  (:import [org.apache.logging.log4j.core.config Configurator]
           [org.apache.logging.log4j LogManager Level]))

(defmacro pass
  "Convenience macro to log `msg` while yielding `expr`."
  ([expr msg]
   `(pass :debug ~expr ~msg))
  ([expr level msg]
   `(do (log/log ~level ~msg)
        ~expr))
  ([expr level fmt fmt-args]
   ; ~@ syntax expands seq from macro context so you don't have to quote
   `(do (log/log ~level (format ~fmt ~@fmt-args))
        ~expr)))

(defmacro passf
  "Convenience macro to log `(format fmt args)` while yielding `expr`.

  Acceptable forms:
  ```clojure
  (passf expr :info fmt & fmt-args)
  (passf expr fmt & fmt-args)
  ```

  `fmt-or-level` is checked for being a keyword, in which case it is treated
  as the log level; otherwise, a default level of `:debug` is assumed.

  Although this breaks with the tools.logging convention of `expr` being last,
  writing it in that way is incredibly confusing. This way, even the
  thread-last wrapper [[passf-last]] is fairly simple.

  The use case is situations where you want to essentially log that something
  passed through some particular place in the code without necessarily wanting
  to log the thing itself. For example, when resolving a user, logging the user
  becomes more and more ridiculous at INFO-level.

  This is convenient to avoid something like this:
  ```clojure
  (def x {:thing {:somekey :some value}})
  (if (contains? x :thing)
    (do (log/info blah blah blah)
        x)
    (do (log/warn some other blah blah)
        x))
  ```
  That's purely convenience, and making it easier to understand what the code
  is doing. An example of where this is almost required:
  ```clojure
  (def x {:thing {:somekey :some value}})
  (some-> x
          an-operation
          (passf :info \"User %s has passed the first hurdle!\" user-id)
          another-operation
          (passf :info \"User has passed another hurdle!\" user-id))
  ```
  In the above scenario, we use `some->` to short-circuit on a `nil` value
  coming back from one of the operations in the chain. Without this macro, the
  only way to have visibility into the process would be either to ensure that
  each step logs aggressively, impossible if using low-level functions, or to
  break the logical sequence into a series of `when-some`s. Although using
  `if-some` would allow for logging each step that may or may not fail, and 
  indeed I use that idiom in many places, there are any number of situations
  where you want visibility in the form of 'we got here!' without breaking up
  the logical flow of the larger operation into something harder to read."
  [expr fmt-or-level & args]
  (if (keyword? fmt-or-level)
    ; fmt-or-level is level
    (let [[fmt & fmt-args] args]
      `(pass ~expr ~fmt-or-level ~fmt ~fmt-args))
    ; fmt-or-level is format string
    `(pass ~expr :debug ~fmt-or-level ~args)))

(defmacro passf-last
  "thread-last wrapper for [[passf]]. See [[spy-first]] for justification.

  Args should be the same as [[passf]], except that `expr` comes last."
  [& args]
  (let [expr (last args)
        args (butlast args)]
  `(passf ~expr ~@args)))

(defmacro spy-first
  "Things I like:
  1. threading macros
  2. inline logging in my threading macros

  Things I don't like:
  ```clojure
  (-> thing
      (some-other thing)
      (->> (log/spy :trace))
      another-thing)
  ```

  That's right: this is a macro so I can use `log/spy` with (-> ).

  The single arrow denotes thread-first."
  [expr & args]
  `(log/spy ~@args ~expr))

(defmacro spyf-first
  [expr & args]
  `(log/spyf ~@args ~expr))

(defmacro strace
  "No relation to the syscall tracer. Logs `expr` at TRACE log level, then 
  yields `expr`.

  Most of the time that I find myself using [[log/spy]], I'm inserting some
  trace-level info into a threading macro. This removes a lot of the resulting
  cruft and also probably takes over the role of [[spy-first]].

  Before:
  ```clojure
  (-> {:important :thing}
      some-operation
      (->> (log/spy :trace))
      another-operation)
  ```
  After:
  ```clojure
  (-> {:important :thing}
      some-operation
      strace
      another-operation)
  ```

  Look, this happens in more places than you'd think."
  [expr]
  `(log/spy :trace ~expr))

; Note that none of the below logic to change runtime log level actually works.

(defn- log4j-level
  "Get an actual Log4j2 Level for a given keyword"
  [level]
  (case level
    :all    Level/ALL
    :trace  Level/TRACE
    :debug  Level/DEBUG
    :info   Level/INFO
    :warn   Level/WARN
    :error  Level/ERROR
    :fatal  Level/FATAL
    :off    Level/OFF))

(defn get-loglevel!
  "Retrieve the current log level"
  ([] (.getLevel (LogManager/getRootLogger)))
  ([cls]
   (.getLevel (LogManager/getLogger cls))))

(defn set-loglevel!
  "Update the global log level"
  [level]
  (log/infof "Changing global log level to %s" level)
  (let [root LogManager/ROOT_LOGGER_NAME
        lg (LogManager/getLogger root)]
    (.debug lg "test debug")
    (.error lg "test error")
    (let [ctx (LogManager/getContext false)
          cfg (.getConfiguration ctx)]
      (-> cfg
          (.getLoggerConfig root)
          (.setLevel (log4j-level level)))
      (Configurator/reconfigure cfg)
      ;(Configurator/setAllLevels root (log4j-level level))
      ;(.updateLoggers ctx cfg)
      (.debug lg "test debug again")
      (.error lg "test error again"))))
