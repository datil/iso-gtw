(ns iso-gtw.client
  (:require [byte-streams :as b])
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
;(.setUseBinaryBitmap msg-factory true)

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

(def mti-0210 (reify IsoMessageListener
                (applies [this iso-msg]
                  (println (.debugString iso-msg))
                  true)
                (onMessage [this ctx iso-msg]
                  (println "server response"
                           (.debugString iso-msg)))))

(.addMessageListener client mti-0810)
(.addMessageListener client mti-0210)

(defn mti-0800-msg
  []
  (let [iso-msg (.newMessage msg-factory 0x800)]
    (doto iso-msg
      (.setField 2 (.value IsoType/ALPHA "5555555555554444" 16))
      (.setField 7 (.value IsoType/DATE10 "0505050505" 10))
      (.setField 11 (.value IsoType/NUMERIC 123456 6))
      (.setField 32 (.value IsoType/NUMERIC 921802 6))
      (.setField 70 (.value IsoType/NUMERIC 000 3)))))

(defn mti-0200-msg
  []
  (let [iso-msg (.newMessage msg-factory 0x200)]
    (doto iso-msg
      (.setField 2 (.value IsoType/LLBIN (b/to-byte-array "5555555555554444")))
      (.setField 3 (.value IsoType/BINARY (b/to-byte-array "003000") 6))
      (.setField 4 (.value IsoType/BINARY (b/to-byte-array "000000001000") 12))
      (.setField 7 (.value IsoType/BINARY (b/to-byte-array "0509095721") 10))
      (.setField 11 (.value IsoType/BINARY (b/to-byte-array "000001") 6))
      (.setField 12 (.value IsoType/BINARY (b/to-byte-array "095721") 6))
      (.setField 13 (.value IsoType/BINARY (b/to-byte-array "0509") 4))
      (.setField 14 (.value IsoType/BINARY (b/to-byte-array "1804") 4))
      (.setField 15 (.value IsoType/BINARY (b/to-byte-array "0509") 4))
      (.setField 18 (.value IsoType/BINARY (b/to-byte-array "7832") 4))
      (.setField 22 (.value IsoType/BINARY (b/to-byte-array "012") 3))
      (.setField 32 (.value IsoType/LLBIN (b/to-byte-array "921802")))
      (.setField 33 (.value IsoType/LLBIN (b/to-byte-array "921802")))
      (.setField 37 (.value IsoType/BINARY (b/to-byte-array "621010000999") 12))
      (.setField 41 (.value IsoType/BINARY (b/to-byte-array "APP66699") 8))
      (.setField 42 (.value IsoType/BINARY (b/to-byte-array "1000000209     ") 15))
      (.setField 43 (.value IsoType/BINARY (b/to-byte-array "DATILMEDIA PACIFICARD   Gy           ECU") 40))
      (.setField 48 (.value IsoType/LLLBIN (b/to-byte-array "9203683")))
      (.setField 49 (.value IsoType/BINARY (b/to-byte-array "840") 3))
      (.setField 54 (.value IsoType/LLLBIN (b/to-byte-array "1112000000000000")))
      (.setField 57 (.value IsoType/LLLBIN (b/to-byte-array "00003000")))
      (.setField 58 (.value IsoType/LLLBIN (b/to-byte-array "220100000000000000000000")))
      (.setField 61 (.value IsoType/LLLBIN (b/to-byte-array "10250000006002180000000000"))))))

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
