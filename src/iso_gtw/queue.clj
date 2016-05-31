(ns iso-gtw.queue
  (require [immutant.messaging :as m]))

(def resp-queue (m/queue "response-queue"))
