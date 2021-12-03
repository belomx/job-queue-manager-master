(ns job-queue-manager.protocols.deferror
  (:require [failjure.core :as f]))

(defrecord GenericError [id message-text])

(extend-protocol f/HasFailed
  GenericError
  (message [this] (:message this))
  (failed? [this] true))

(def no-queued-jobs                     (->GenericError 1 "No Queued jobs available."))
(def no-queued-jobs-for-skillset        (->GenericError 2 "No Queued jobs available for yours skillsets."))
(def agent-already-exists               (->GenericError 3 "Another agent with this id has already been added."))
(def job-already-exists                 (->GenericError 4 "Another job with this id has already been added."))
(def agent-doesnot-exist                (->GenericError 5 "There is not an agent with this id."))
(def agent-name-not-valid               (->GenericError 6 "The agent name is not valid."))
(def agent-primaryskilset-not-provided  (->GenericError 7 "The agent primary skillset was not provided."))
(def job-type-not-provided              (->GenericError 8 "The job type was not provided properly."))
(def job-urgent-flag-not-correct        (->GenericError 9 "The job urgent flag was not provided properly."))
(def service-name-invalid               (->GenericError 10 "The service name is not valid."))
(def job-id-invalid                     (->GenericError 11 "The job id is not valid."))
(def agent-id-invalid                   (->GenericError 12 "The agent id is not valid."))
(def file-path-invalid                  (->GenericError 13 "The file path is invalid."))
(def directory-path-invalid             (->GenericError 14 "The directory path is invalid."))
(def input-arguments-invalid            (->GenericError 15 "ERROR: The input arguments are invalid."))