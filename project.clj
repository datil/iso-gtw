(defproject iso-gtw "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.pedestal/pedestal.service "0.4.1"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.4.1"]
                 ;; [io.pedestal/pedestal.immutant "0.4.1"]
                 ;; [io.pedestal/pedestal.tomcat "0.4.1"]

                 [ch.qos.logback/logback-classic "1.1.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.12"]
                 [org.slf4j/jcl-over-slf4j "1.7.12"]
                 [org.slf4j/log4j-over-slf4j "1.7.12"]

                 [environ "1.0.3"]
                 [org.clojure/core.async "0.2.374"]
                 [org.jreactive/netty-iso8583 "0.1.3-SNAPSHOT"]
                 [byte-streams "0.2.2"]
                 [org.immutant/messaging "2.1.4"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :repl-options {:welcome (println "Welcome Master!")
                 :host "0.0.0.0"
                 :port 9999}
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "iso-gtw.rest-service.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.4.1"]]}
             :uberjar {:aot [iso-gtw.rest-service.server]}}
  :main ^{:skip-aot true} iso-gtw.rest-service.server)
