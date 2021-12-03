(ns job-queue-manager.protocols.coreprotocols
  (:require [failjure.core :as f]
            [job-queue-manager.protocols.deferror :as derror])
  (:import [java.util Date]))

(defrecord Agent [id name primary-skillset secondary-skillset])

(defrecord Job [id type urgent entry-date status working-agent-id])

(defrecord AtomStore [data])

(defprotocol AgentStore
  (get-agents [store])
  (clean-agents! [store])
  (put-agent! [store agent]))

(defprotocol JobStore
  (get-jobs [store])
  (put-job! [store job])
  (clean-jobs! [store])
  (finish-job! [store agent])
  (assign-job! [store job agent])
  )

(extend-protocol AgentStore
  AtomStore
  (get-agents [store]
    (get @(:data store) :agents))
  (clean-agents! [store]    
      (swap! (:data store) 
             update-in [:agents] (fn [agents] '())))
  (put-agent! [store agent]
    (swap! (:data store) 
           update-in [:agents] conj agent)))

(extend-protocol JobStore
  AtomStore
  (get-jobs [store]
    (get @(:data store) :jobs))
  (put-job! [store job]
    (dosync
      (commute (:data store) 
             update-in [:jobs] conj job)))
  (clean-jobs! [store]
    (dosync
      (commute (:data store) 
             update-in [:jobs] (fn [jobs] '()))))
  (finish-job! [store agent]
    (dosync           
      (commute (:data store) update-in [:jobs] (fn [jobs]
                                                 (map #(if (and (= (:working-agent-id %) (:id agent)) (= (:status %) :working))
                                                         (assoc % :status :done) %) jobs)))))
  (assign-job! [store job agent]
    (dosync           
      (commute (:data store) update-in [:jobs]
               (fn [jobs] 
                 (let [job-id (:id job)] 
                   (map 
                     #(if (= (:id %) job-id) 
                        (assoc (assoc % :status :working) :working-agent-id (:id agent)) %) jobs)))))
    (first (filter #(= (:id %) (:id job)) (get-jobs store)))))


(def ^:private job-store (->AtomStore (ref {:jobs '()})))
(def ^:private agent-store (->AtomStore (atom {:agents '()})))

(defn get-all-agents
  []  
  (get-agents agent-store))

(defn get-all-jobs
  []
  (get-jobs job-store))

(defn clean-all-agents
  []
  (clean-agents! agent-store))

(defn clean-all-jobs
  []
  (clean-jobs! job-store))

(defn assign-job
  [job agent]
  (assign-job! job-store job agent))

(defn agent-exists? 
  [agent-id]
  (some #(= (:id %)  agent-id) (get-agents agent-store)))

(defn job-exists? 
  [job-id]
  (some #(= (:id %) job-id) (get-jobs job-store)))

(defn finish-job 
  [agent]
  (finish-job! job-store agent))

(defn validate-agent-input
  [id name primary-skillset secondary-skillset]
  (if (or (not (string? id)) (empty? id)) (f/fail derror/agent-id-invalid)
    (if (true? (agent-exists? id)) (f/fail derror/agent-already-exists)
      (if (or (not (string? name)) (empty? name)) (f/fail derror/agent-name-not-valid)
        (if (empty? primary-skillset) (f/fail derror/agent-primaryskilset-not-provided))))))

(defn validate-job-input
  [id type urgent]
  (if (or (not (string? id)) (empty? id)) (f/fail derror/job-id-invalid)
    (if (true? (job-exists? id)) (f/fail derror/job-already-exists)
      (if (or (not (string? type)) (empty? type)) (f/fail derror/job-type-not-provided)
        (if-not (or (true? urgent) (false? urgent)) (f/fail derror/job-urgent-flag-not-correct))))))

(defn new-agent
  "Creates a new agent and add to the AgentRecords."
  [id name primary-skillset secondary-skillset]
  (f/if-let-failed? [validate-result (validate-agent-input id name primary-skillset secondary-skillset)]   
    validate-result                
    ((put-agent! agent-store (->Agent id name primary-skillset secondary-skillset)) nil)))

(defn new-job
  "Creates a new job and add to the JobRecords."
  [id type urgent]     
  (f/if-let-failed? [validate-result (validate-job-input id type urgent)]   
    validate-result
    ((put-job! job-store (->Job id type urgent (Date.) :waiting nil)) nil)))

 ;(new-agent "260" "BoJack Horseman" ["bills-questions"] [])
 ;(new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])
  
 ;(new-job "c36" "rewards-question" false)
 ;(new-job "864" "bills-questions" false)
 ;(new-job "1d2" "bills-questions" true)

;(new-agent 123 "Ronaldo" ["bills-questions"] [])
;(new-agent 987 "Pluto" ["rewards-question"] [])
;(new-agent 409 "Batman" ["rewards-question"] ["bills-questions"])
;(new-agent 786 "Belom" ["dota-questions"] [])
;(new-agent 367 "Sãnic" ["running-questions"] [])

;(new-job 456  "bills-questions" false)
;(new-job 542 "bills-questions" true)
;(new-job 142 "rewards-question" true)
;(new-job 731 "rewards-question" false)
;(new-job  89 "dota-question" false)

;(get-agents agent-store)
;(get-jobs job-store)