(ns iso-gtw.client
  (:import [org.jreactive.iso8583.client Iso8583Client]
           [org.jreactive.iso8583 IsoMessageListener]
           [com.solab.iso8583 IsoMessage]
           [com.solab.iso8583 IsoType]
           [com.solab.iso8583 MessageFactory]
           [com.solab.iso8583.parse ConfigParser]
           [java.nio.charset StandardCharsets]))

(def msg-factory (ConfigParser/createFromUrl
                  (clojure.java.io/resource "bp/res.xml")))

(.setCharacterEncoding msg-factory (.name StandardCharsets/US_ASCII))
(.setUseBinaryMessages msg-factory true)

(def client (new Iso8583Client msg-factory))

;; Handlers
(def mti-0810 (reify IsoMessageListener
                (applies [this iso-msg]
                  ;;(= (.getType iso-msg) 0x810)
                  (println (.debugString iso-msg))
                  true)
                (onMessage [this ctx iso-msg]
                  (println "server response code: "
                           (.debugString iso-msg))
                  true)))

(.addMessageListener client mti-0810)

(defn mti-800-msg
  []
  (let [iso-msg (.newMessage msg-factory 0x800)]
    (doto iso-msg
      (.setField 2 (.value IsoType/ALPHA "5555555555554444" 16))
      (.setField 7 (.value IsoType/DATE10 "0505050505" 10))
      (.setField 11 (.value IsoType/NUMERIC 123456 6))
      (.setField 32 (.value IsoType/NUMERIC 921802 6))
      (.setField 70 (.value IsoType/NUMERIC 000 3)))))

(defn connect
  [client host port]
  (.init client)
  (.connect client host port))

(defn shutdown
  [client]
  (.shutdown client))

(defn send-msg
  [client msg]
  (if (.isConnected client)
    (.send client msg)))
