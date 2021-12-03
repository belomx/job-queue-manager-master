(ns job-queue-manager.service
  (:require [failjure.core :as f]
            [job-queue-manager.core :as cr]
            [job-queue-manager.protocols.coreprotocols :as coreptls]
            [job-queue-manager.protocols.deferror :as derror]
            [scjsv.core :as json-checker]))


(def new-agent-schema {:$schema "http://json-schema.org/draft-04/schema#"
             :type "object"
             :properties {:id {:type "string"} :name {:type "string"}
                           :primary_skillset {:type "array"}
                           :secondary_skillset {:type "array"}}
             :required [:id :name :primary_skillset :secondary_skillset]})

(def new-job-schema {:$schema "http://json-schema.org/draft-04/schema#"
             :type "object"
             :properties {:id {:type "string"} :type {:type "string"}
                           :urgent {:type "boolean"}}
             :required [:id :type :urgent]})

(def job-request-schema {:$schema "http://json-schema.org/draft-04/schema#"
             :type "object"
             :properties {:agent_id {:type "string"}}
             :required [:agent_id]})


(def new-agent-validate (json-checker/validator new-agent-schema))

(def new-job-validate (json-checker/validator new-job-schema))

(def job-request-validate (json-checker/validator job-request-schema))

(defn call-new-agent [input]
  (let [result (new-agent-validate input)]
    (if-not (nil? result)
    result
    (coreptls/new-agent (:id input) (:name input) (:primary_skillset input) (:secondary_skillset input)))))

(defn call-new-job [input]
  (let [result (new-job-validate input)]
    (if-not (nil? result)
      result
      (coreptls/new-job (:id input) (:type input) (:urgent input)))))

(defn call-job-request [input]
   (let [result (job-request-validate input)]
     (if-not (nil? result)
       result
       (f/if-let-failed? [request-result (cr/job-request-by-agentid (:agent_id input))]
         request-result
         {:job_id (:id request-result) :agent_id (:working-agent-id request-result)}))))

(defn ^:private wrap-job-request
  [job-request-result]
  (f/if-let-failed? [result job-request-result]
    result                
    {:job_assigned result}))

(defn call-get-all-jobs
  []
  (coreptls/get-all-jobs))

(defn call-get-agent-stats
  [agent-id]
  (cr/get-agent-stats agent-id))

(defn call-service
  [input]
  (let [service-name (first (keys input))]
    (case service-name
      :new_agent (call-new-agent (:new_agent input))
      :new_job (call-new-job (:new_job input))
      :job_request (wrap-job-request (call-job-request (:job_request input)))
      (f/fail derror/service-name-invalid))))

