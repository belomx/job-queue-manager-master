(ns job-queue-manager.adapters.http-adapter
  (:require [compojure.api.sweet :refer [defapi api GET POST]]
            [ring.adapter.jetty :as jetty]
            [job-queue-manager.service :as service]
            [ring.util.http-response :as http-response]
            [failjure.core :as f]
            [schema.core :as schema]))

(schema/defschema Agent
  {:id schema/Str   
   :name schema/Str
   :primary_skillset [schema/Str]
   :secondary_skillset [schema/Str]})

(schema/defschema Job
  {:id schema/Str   
   :type schema/Str
   :urgent schema/Bool})

(schema/defschema JobDetailed
  {:id schema/Str   
   :type schema/Str
   :urgent schema/Bool
   :entry-date schema/Inst
   :status (schema/enum :waiting :working :done)
   (schema/optional-key :working-agent-id) schema/Any})

(schema/defschema JobRequest
  {:agent_id schema/Str})

(defn ^:private call-service
  ([response-http service-result]
    (f/if-let-failed? [result service-result]
      (http-response/internal-server-error (f/message result))                  
      (response-http result))))

(def job-queue-manager-api
  (api 
    {:swagger
     {:ui "/job-queue-manager-api-docs"
      :spec "/swagger.json"
      :data {:info {:title "Job Queue Manager API"
                    :description "The Job Queue Manager is an application responsible to handle jobs to agents."}
             :tags [{:name "job-queue-manager-api", :description "apis of job queue manager"}]
             :consumes ["application/json"]
             :produces ["application/json"]}}}
  (GET
    "/jobs/count_by_agent"
    []
    :return schema/Any
    :query-params [agent-id :- schema/Str]
    :summary "Given an agent, return how many jobs of each type this agent has performed."
    (call-service http-response/ok (service/call-get-agent-stats agent-id)))
  
  (GET
    "/jobs" []
    :return [JobDetailed]
    :summary "Returns a breakdown of the job queue, consisting of all jobs."
    (call-service http-response/ok (service/call-get-all-jobs)))
  
  (POST
    "/agents" []
    :return {}
    :body [input Agent]
    :summary "Creates an agent."
    (call-service http-response/created (service/call-new-agent input)))
  
  (POST
    "/jobs" []
    :return {}
    :body [input Job]
    :summary "Creates a job."
    (call-service http-response/created (service/call-new-job input)))
  
  (POST
    "/jobs/request" []
    :return schema/Any
    :body [input JobRequest]
    :summary "Assign a job to a agent, and returns the id of the assigned job or indicate the lack of one."
    (call-service http-response/ok (service/call-job-request input)))))

(defn start-server 
  [port]
  (jetty/run-jetty job-queue-manager-api {:port (Long/valueOf port)}))

