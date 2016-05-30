(ns iso-gtw.client-test
  (:require [clojure.test :refer :all]
           [iso-gtw.client :as c])
  (:import [com.solab.iso8583 IsoType]
           [com.solab.iso8583.parse ConfigParser]))

(def msg-factory (ConfigParser/createDefault))

(deftest iso-msg-test
  (testing "Sets the Message Type Indicator"
    (let [msg (c/iso-msg msg-factory {:mti 0x200})]
      (is (= 0x200
             (.getType msg)))))
  (testing "Sets message fields"
    (let [msg (c/iso-msg msg-factory {:mti 0x200
                                      :fields [{:field 2
                                                :type "LLVAR"
                                                :value "1234"}]})]
      (is (= "1234"
             (.getValue (.getField msg 2))))))
  (testing "Sets multiple message fields"
    (let [msg (c/iso-msg msg-factory {:mti 0x200
                                      :fields [{:field 2
                                                :type "LLVAR"
                                                :value "1234"}
                                               {:field 3
                                                :type "LLVAR"
                                                :value "1234"}]})]
      (is (= ["1234" "1234"]
             [(.getValue (.getField msg 2))
              (.getValue (.getField msg 3))]))))
  (testing "Sets field length"
    (let [msg (c/iso-msg msg-factory {:mti 0x200
                                      :fields [{:field 2
                                                :type "ALPHA"
                                                :value "1234"
                                                :length 4}]})]
      (is (= 4
             (.getLength (.getField msg 2))))))
  (testing "Sets field type"
    (let [msg (c/iso-msg msg-factory {:mti 0x200
                                      :fields [{:field 2
                                                :type "ALPHA"
                                                :value "1234"
                                                :length 4}]})]
      (is (= true
             (.equals IsoType/ALPHA (.getType (.getField msg 2))))))))
