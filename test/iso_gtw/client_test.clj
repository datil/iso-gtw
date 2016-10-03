(ns iso-gtw.client-test
  (:require [clojure.test :refer :all]
            [iso-gtw.client :as c]
            [iso-gtw.mti :as mti])
  (:import [com.solab.iso8583 IsoType]
           [com.solab.iso8583.parse ConfigParser]))

(def msg-factory (ConfigParser/createDefault))

(deftest iso-msg-test
  (testing "Sets the Message Type Indicator"
    (with-redefs [mti/mti {:0200 0x200
                           :0400 0x400}]
      (is (= 0x200
             (.getType (c/iso-msg msg-factory {:mti :0200}))))
      (is (= 0x400
             (.getType (c/iso-msg msg-factory {:mti :0400}))))))
  (testing "Sets message field"
    (let [msg (c/iso-msg msg-factory {:mti :0200
                                      :fields [{:field 2
                                                :type "LLVAR"
                                                :value "1234"}]})]
      (is (= "1234"
             (.getValue (.getField msg 2))))))
  (testing "Sets multiple message fields"
    (let [msg (c/iso-msg msg-factory {:mti :0200
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
    (let [msg (c/iso-msg msg-factory {:mti :0200
                                      :fields [{:field 2
                                                :type "ALPHA"
                                                :value "1234"
                                                :length 4}]})]
      (is (= 4
             (.getLength (.getField msg 2))))))
  (testing "Sets field type"
    (let [msg (c/iso-msg msg-factory {:mti :0200
                                      :fields [{:field 2
                                                :type "ALPHA"
                                                :value "1234"
                                                :length 4}]})]
      (is (= true
             (.equals IsoType/ALPHA (.getType (.getField msg 2))))))))
