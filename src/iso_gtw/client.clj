(ns iso-gtw.client
  (:require [byte-streams :as b]
            [immutant.messaging :as m]
            [iso-gtw.config :as config]
            [iso-gtw.queue :as queue])
  (:import [org.jreactive.iso8583.client Iso8583Client]
           [org.jreactive.iso8583 IsoMessageListener]
           [com.solab.iso8583 IsoMessage]
           [com.solab.iso8583 IsoType]
           [com.solab.iso8583 MessageFactory]
           [com.solab.iso8583.parse ConfigParser]
           [java.nio.charset StandardCharsets]))

(defn msg-factory
  [conf-xml]
  (let [factory (ConfigParser/createFromUrl
                 (clojure.java.io/resource conf-xml))]
    (doto factory
      (.setCharacterEncoding (.name StandardCharsets/US_ASCII))
      (.setUseBinaryBitmap true))))

(def mti-0210 (reify IsoMessageListener
                (applies [this iso-msg]
                  (= (.getType iso-msg) 0x210))
                (onMessage [this ctx iso-msg]
                  (let [resp-code (.toString (.getField iso-msg 39))]
                    (if (= resp-code "00")
                      (m/publish queue/resp-queue
                                 {:stan (.toString (.getField iso-msg 11))
                                  :response-code (.toString (.getField iso-msg 39))
                                  :authorization (.toString (.getField iso-msg 38))
                                  :network-data (.toString (.getField iso-msg 63))}
                                 :properties {:stan (.toString (.getField iso-msg 11))})
                      (m/publish queue/resp-queue
                                 {:stan (.toString (.getField iso-msg 11))
                                  :response-code (.toString (.getField iso-msg 39))
                                  :authorization ""
                                  :network-data (.toString (.getField iso-msg 63))}
                                 :properties {:stan (.toString (.getField iso-msg 11))})))
                  false)))

(defn client
  [msg-factory]
  (let [new-client (new Iso8583Client msg-factory)]
    (doto new-client
      (.addMessageListener mti-0210))))

(defonce clients {:mock {:client (let [msg-factory (msg-factory
                                                    (:iso-config (:mock config/hosts)))
                                       msg-client (client msg-factory)]
                                   msg-client)
                         :host (:host (:mock config/hosts))
                         :port (:port (:mock config/hosts))}})

(defn iso-msg
  [msg-factory msg]
  (let [iso-msg (.newMessage msg-factory 0x200)]
    (if (:fields msg)
      (do
        (doseq [field (:fields msg)]
          (if (:length field)
            (.setField iso-msg
                       (:field field)
                       (.value (IsoType/valueOf (:type field))
                               (:value field)
                               (:length field)))
            (.setField iso-msg
                       (:field field)
                       (.value (IsoType/valueOf (:type field)) (:value field)))))
        iso-msg)
      iso-msg)))

(defn is-connected?
  [client]
  (.isConnected client))

(defn connect
  [client host port]
  (doto client
    (.init)
    (.connect host port)))

(defn shutdown
  [client]
  (.shutdown client))

(defn send-msg
  [client msg]
  (if (.isConnected client)
    (.send client msg)
    (throw (ex-info "Client not connected."))))
