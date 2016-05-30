(ns iso-gtw.rest-service.service
  (:require [clojure.core.async :as async :refer :all]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor.helpers :as interceptor]
            [iso-gtw.client :as iso]
            [ring.util.response :as ring-resp]
            [iso-gtw.rest-service.responses :as resp]
            [immutant.messaging :as m]
            [iso-gtw.queue :as q]))

(def assoc-iso-client
  (interceptor/before
   ::assoc-db
   (fn
     [context]
     (assoc-in context [:request :iso-client] (:mock iso/clients)))))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defn get-field
  [fields field]
  (first (filter #(and (= (compare (% :field) field) 0)) fields)))

(interceptor/defbefore message
  [context]
  (let [channel (chan)]
    (go
      (let [request (:request context)
            incoming (:json-params request)
            iso-client (:iso-client request)
            factory (iso/msg-factory "bp/res.xml")
            stan (:value (get-field (:fields incoming) 11))
            msg (iso/iso-msg factory incoming)
            sent (iso/send-msg (:client iso-client) msg)
            rec-msg (m/receive q/resp-queue :selector (str "stan = '" stan "'"))
            response (assoc context :response (resp/ok rec-msg))]
        (>! channel response)))
    channel))

(defn start-client
  [request]
  (let [iso-client (:iso-client request)]
    (println "starting...")
    (future
      (iso/connect (:client iso-client) (:host iso-client) (:port iso-client)))
    (resp/ok {:message "Client started."})))

(defn shutdown-client
  [request]
  (let [iso-client (:iso-client request)]
    (iso/shutdown (:client iso-client))
    (resp/ok {:message "Client stopped."})))

(defn client-status
  [request]
  (let [iso-client (:iso-client request)]
    (resp/ok {:connected (iso/is-connected? (:client iso-client))})))

(defroutes routes
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
  [[["/" {:get home-page}
     ^:interceptors [(body-params/body-params)
                     bootstrap/json-body
                     assoc-iso-client]
     ["/about" {:get about-page}]
     ["/client"
      ["/status" {:get client-status}]
      ["/start" {:post start-client}]
      ["/shutdown" {:post shutdown-client}]]
     ["/message" {:post message}]]]])

;; Consumed by iso-gtw.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 8080})
