(ns job-queue-manager.core
  (:require [job-queue-manager.protocols.coreprotocols :as coreptls]
            [job-queue-manager.protocols.deferror :as derror]
            [failjure.core :as f])
  (:import [java.util Date]))

(defn get-queued-jobs
  [jobs]
  (filter #(= (:status %) :waiting) jobs))

(defn get-job-by-id
  [jobs job-id]
  (first (filter #(= (:id %) job-id) jobs)))

(defn ^:private get-jobs-by-urgency
  [jobs f]
  (filter #(f (:urgent %)) jobs))

(defn get-urgent-jobs
  [jobs]
  (get-jobs-by-urgency jobs true?))

(defn get-not-urgent-jobs
  [jobs]
  (get-jobs-by-urgency jobs false?))

(defn sort-jobs-by-date
  [jobs]
  (sort #(compare (:entry-date %1) (:entry-date %2)) jobs))

(defn get-agent-by-id  
  [agents id]
  (if (or (not (string? id)) (empty? id)) (f/fail derror/agent-id-invalid)
    (let [agent-list (filter #(= (:id %) id) agents)]
      (if (empty? agent-list) (f/fail derror/agent-doesnot-exist) (first agent-list)))))
      
(defn ^:private get-jobs-by-skill
  [jobs skill]
  (filter #(= skill (:type %)) jobs))

(defn ^:private get-jobs-by-skillset
  [jobs skillset]
  (for [skill skillset] (get-jobs-by-skill jobs skill)))

(defn ^:private get-jobs-by-skillsetkey-agent
  [jobs skillsetkey agent]
  (get-jobs-by-skillset jobs (skillsetkey agent)))

(defn get-jobs-by-primaryskillset-agent
  [jobs agent]
  (flatten (get-jobs-by-skillsetkey-agent jobs :primary-skillset agent)))

(defn get-jobs-by-secundaryskillset-agent
  [jobs agent]
  (flatten (get-jobs-by-skillsetkey-agent jobs :secondary-skillset agent)))

(defn get-jobs-done-by-working-agent-id
  [jobs agent]
  (filter #(and (= (:working-agent-id %) (:id agent)) (= (:status %) :done)) jobs))

(defn map-jobs-by-agent-skillset
  [jobs agent]
  (let [skillset (flatten (into (:primary-skillset agent) (:secondary-skillset agent)))]
    (map (fn [skill] {skill (count (get-jobs-by-skill jobs skill))}) skillset)))

(defn get-agent-stats
  [agent-id]
  (f/if-let-failed? [agent-result (get-agent-by-id (coreptls/get-all-agents) agent-id)]
   agent-result                 
   (map-jobs-by-agent-skillset (get-jobs-done-by-working-agent-id (coreptls/get-all-jobs) agent-result) agent-result)))

(defn get-sorted-jobs
  "Get from job list the urgent ones (with urgent flag as true), if there isn't any \r
  get the not urgent ones. After that sorted the results."
  [jobs]
  (let [urgent-jobs (get-urgent-jobs jobs)]
    (if-not (empty? urgent-jobs) (sort-jobs-by-date urgent-jobs) (sort-jobs-by-date (get-not-urgent-jobs jobs)))))

(defn get-sorted-jobs-tobe-assigned
  "Try to get jobs that match with the agent's primary skill set, if there isn't any, \r
try to match with the agent's secondary skill set. After that execute get-sorted-jobs."
  [jobs agent]
  (let [queued-jobs (get-queued-jobs jobs)]
    (if (empty? queued-jobs) (f/fail derror/no-queued-jobs) 
      (let [jobs-by-primaryskill (get-jobs-by-primaryskillset-agent queued-jobs agent)] 
        (if-not (empty? jobs-by-primaryskill) (get-sorted-jobs jobs-by-primaryskill)
          (let [jobs-by-secundaryskill (get-jobs-by-secundaryskillset-agent queued-jobs agent)] 
            (if (empty? jobs-by-secundaryskill)  (f/fail derror/no-queued-jobs-for-skillset)
              (get-sorted-jobs jobs-by-secundaryskill))))))))

(defn job-request-by-agentid
  [id]
  (f/if-let-failed? [agent-result (get-agent-by-id (coreptls/get-all-agents) id)]
    agent-result
    (do 
      (coreptls/finish-job agent-result)
      (f/if-let-failed? [job-result (get-sorted-jobs-tobe-assigned (coreptls/get-all-jobs) agent-result)]
        job-result
        (coreptls/assign-job (first job-result) agent-result)))))

