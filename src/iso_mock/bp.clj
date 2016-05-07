(ns iso-mock.bp
  (:import [org.jreactive.iso8583.server Iso8583Server]
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

(def srvr (new Iso8583Server 9999 msg-factory))

(defn mti-810-msg
  []
  (let [iso-msg (.newMessage msg-factory 0x810)]
    (doto iso-msg
      (.setField 2 (.value IsoType/ALPHA "5555555555554444" 16))
      (.setField 7 (.value IsoType/DATE10 "0505050505" 10))
      (.setField 11 (.value IsoType/NUMERIC 123456 6))
      (.setField 32 (.value IsoType/NUMERIC 921802 6))
      (.setField 39 (.value IsoType/ALPHA "00" 2))
      (.setField 70 (.value IsoType/NUMERIC 000 3)))))

;; Handlers
(def mti-0800 (reify IsoMessageListener
                (applies [this iso-msg]
                  (println "receiving msg: " (.debugString iso-msg))
                  (= (.getType iso-msg) 0x800))
                (onMessage [this ctx iso-msg]
                  (println "processing received! " (.debugString iso-msg))
                  (.writeAndFlush ctx (mti-810-msg))
                  false)))

;;0800
;(.addMessageListener srvr mti-0800)
