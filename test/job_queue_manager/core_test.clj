(ns job-queue-manager.core-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [job-queue-manager.core :as cr]
            [job-queue-manager.protocols.coreprotocols :as coreptls]
            [job-queue-manager.protocols.deferror :as derror]))

(defn ^:private check-map-by-list
  [list-map key list]
  
  (nil? (some #(false? %) (map (fn [map val] (= (get map key) val)) 
                                  list-map
                                  list))))

(deftest test-check-map-by-list-test
  
  (testing "Check if the function is returning true"
           (is (true? (check-map-by-list [{:id 1} {:id 2} {:id 3}] :id [1 2 3]))))
  (testing "Check if the function is returning false"
           (is (false? (check-map-by-list [{:id 1} {:id 2} {:id 3}] :id [1 2 4])))))

(deftest get-queued-jobs-test
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (cr/job-request-by-agentid  "260")
  
  (testing "No jobs must return"
    (is (check-map-by-list (cr/get-queued-jobs (coreptls/get-all-jobs)) :id ["864" "c36"])))
  (testing "Only 2 queued jobs have to return"
    (is (= (count (cr/get-queued-jobs (coreptls/get-all-jobs))) 2))))

(deftest get-urgent-jobs-test
  (coreptls/clean-all-jobs)
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
    
  (testing "Only urgent jobs must return"
    (is (check-map-by-list (cr/get-urgent-jobs (coreptls/get-all-jobs)) :id ["1d2"])))
  (testing "Only 1 urgent job have to return"
    (is (= (count (cr/get-urgent-jobs (coreptls/get-all-jobs))) 1))))

(deftest get-not-urgent-jobs-test
  (coreptls/clean-all-jobs)
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
    
  (testing "Only not-urgent jobs must return"
    (is (check-map-by-list (cr/get-not-urgent-jobs (coreptls/get-all-jobs)) :id ["864" "c36"])))
  (testing "Only 2 not-urgent job have to return"
    (is (= (count (cr/get-not-urgent-jobs (coreptls/get-all-jobs))) 2))))

(deftest sort-jobs-by-date-test
  
  (coreptls/clean-all-jobs)
  
  (coreptls/new-job "c36" "rewards-question" false)     
  (Thread/sleep 100)
  (coreptls/new-job "864" "bills-questions" false)
  (Thread/sleep 100)
  (coreptls/new-job "1d2" "bills-questions" true)
      
  (testing "Check if the jobs are sorted by the creation date"
           (is (true? (check-map-by-list (cr/sort-jobs-by-date (coreptls/get-all-jobs)) :id ["c36" "864" "1d2"])))))

(deftest get-agent-by-id-test
  
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])
  
  (testing "Check if the agent returned has the same Id"
                 (is (=  (:id (cr/get-agent-by-id (coreptls/get-all-agents) "260")) "260")))
  (testing "Check if the agent returned has the same name"
                 (is (=  (:name (cr/get-agent-by-id (coreptls/get-all-agents) "260")) "BoJack Horseman")))
  (testing "Check if the agent returned has the same primary skil set"
                 (is (=  (:primary-skillset (cr/get-agent-by-id (coreptls/get-all-agents) "c88")) ["rewards-question"])))
  (testing "Check if the agent returned has the same secondary skil set"
                 (is (=  (:secondary-skillset (cr/get-agent-by-id (coreptls/get-all-agents) "c88")) ["bills-questions"]))))

(deftest get-jobs-by-primaryskillset-agent-test
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question" "crossover-questions"] ["bills-questions"])
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
  (coreptls/new-job "864" "crossover-questions" false)
  
  (testing "Check if the correct list of jobs related to the primary skil set of Bojack"
           (is (true? (some (fn [job] (some #(= % (:type job)) ["bills-questions"]))
                            (cr/get-jobs-by-primaryskillset-agent (coreptls/get-all-jobs) 
                                                                  (cr/get-agent-by-id (coreptls/get-all-agents) "260"))))))
  (testing "Check if the correct list of jobs related to the primary skil set of Peanute Butter"
           (is (true? (some (fn [job] (some #(= % (:type job)) ["rewards-question" "crossover-questions"]))
                            (cr/get-jobs-by-primaryskillset-agent (coreptls/get-all-jobs) 
                                                                  (cr/get-agent-by-id (coreptls/get-all-agents) "c88")))))))

(deftest get-jobs-by-secundaryskillset-agent-test
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] ["magic-questions"])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["crossover-questions" "dota-issues"])
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "crossover-questions" false)
  (coreptls/new-job "1d2" "magic-questions" true)
  (coreptls/new-job "864" "dota-issues" false)
  (coreptls/new-job "1d2" "bills-questions" true)
  
  (testing "Check if the correct list of jobs related to the secondary skil set of Bojack"
           (is (true? (some (fn [job] (some #(= % (:type job)) ["magic-questions"]))
                            (cr/get-jobs-by-secundaryskillset-agent (coreptls/get-all-jobs) 
                                                                  (cr/get-agent-by-id (coreptls/get-all-agents) "260"))))))
  (testing "Check if the correct list of jobs related to the secondary skil set of Peanute Butter"
           (is (true? (some (fn [job] (some #(= % (:type job)) ["crossover-questions" "dota-issues"]))
                            (cr/get-jobs-by-secundaryskillset-agent (coreptls/get-all-jobs) 
                                                                  (cr/get-agent-by-id (coreptls/get-all-agents) "c88")))))))

(deftest get-jobs-done-by-working-agent-id-test
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
  
  (cr/job-request-by-agentid  "260")
  
  (testing "Check if no jobs returned since none of them were finished"
           (is (true?
                 (check-map-by-list
                   (cr/get-jobs-done-by-working-agent-id
                     (coreptls/get-all-jobs)                     
                     (cr/get-agent-by-id (coreptls/get-all-agents) "260"))
                   :id
                   []))))
  
  (cr/job-request-by-agentid  "260")
  
  (testing "Check if the jobs returned are from the agent that finish them"
           (is (true?
                 (check-map-by-list
                   (cr/get-jobs-done-by-working-agent-id
                     (coreptls/get-all-jobs)
                     (cr/get-agent-by-id (coreptls/get-all-agents) "260"))
                   :id
                   ["1d2"])))))

(deftest get-agent-stats-test
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
  
  (cr/job-request-by-agentid  "c88")
  
    (testing "Check if the count if correct."
           (is (nil? (some #(false? %) (map (fn [result-map skill count] (= (get result-map skill) count)) 
                                  (cr/get-agent-stats "c88")
                                  ["rewards-question" "bills-questions"]
                                  [0 0])))))
  
  (cr/job-request-by-agentid  "c88")  
  (cr/job-request-by-agentid  "260")
  
  (testing "Check if the count if correct."
           (is (nil? (some #(false? %) (map (fn [result-map skill count] (= (get result-map skill) count)) 
                                  (cr/get-agent-stats "260")
                                  ["bills-questions"]
                                  [0])))))
  
  (cr/job-request-by-agentid  "260")
  (cr/job-request-by-agentid  "c88")
  
  (testing "Check if the count if correct."
           (is (nil? (some #(false? %) (map (fn [result-map skill count] (= (get result-map skill) count)) 
                                  (cr/get-agent-stats "c88")
                                  ["rewards-question" "bills-questions"]
                                  [1 1])))))
  (testing "Check if the count if correct."
           (is (nil? (some #(false? %) (map (fn [result-map skill count] (= (get result-map skill) count)) 
                                  (cr/get-agent-stats "260")
                                  ["bills-questions"]
                                  [1]))))))

(deftest get-sorted-jobs-test
  (coreptls/clean-all-jobs)
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
  
  (testing "Check if the get-sorted-jobs return only urgent jobs"
           (is (true? (some #(true? (:urgent %)) (cr/get-sorted-jobs (coreptls/get-all-jobs))))))
  
  (coreptls/clean-all-jobs)
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  
  (testing "Check if the get-sorted-jobs return only not-urgent jobs"
           (is (true? (some #(false? (:urgent %)) (cr/get-sorted-jobs (coreptls/get-all-jobs)))))))

(deftest job-request-by-agentid-test  
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
    
  (testing "TEST-default-test-case: BoJack receives job - 1d2"
    (is (= (:id (cr/job-request-by-agentid  "260")) "1d2")))
  (testing "TEST-default-test-case: BoJack receives job - 1d2"
    (is (= (f/message (cr/job-request-by-agentid  nil)) derror/agent-id-invalid))
  (testing "TEST-default-test-case: Mr. Peanut Butter receives job - c36"
    (is (= (:id (cr/job-request-by-agentid  "c88")) "c36")))
  (testing "TEST-default-test-case: Mr. Peanut Butter receives job - 864"
    (is (= (:id (cr/job-request-by-agentid  "c88")) "864"))))
  (testing "TEST-default-test-case: Return message of no jobs available and updates the last job from Bojack to done"
    (is (= (cr/job-request-by-agentid  "c88") (f/fail derror/no-queued-jobs)))
    (is (= (:status (cr/get-job-by-id (coreptls/get-all-jobs) "864")) :done)))
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
    
  (testing "TEST-error-no-message-queued-test-case: BoJack receives job - 1d2"
    (is (= (:id (cr/job-request-by-agentid  "260")) "1d2")))
  (testing "TEST-error-no-message-queued-test-case: Mr. Peanut Butter receives job - c36"
    (is (= (:id (cr/job-request-by-agentid  "c88")) "c36")))
  (testing "TEST-error-no-message-queued-test-case: Mr. Peanut Butter receives job - 864"
    (is (= (:id (cr/job-request-by-agentid  "c88")) "864")))
  (testing "TEST-error-no-message-queued-test-case: Mr. Peanut Butter receive error message - id = 1"
    (is (= (cr/job-request-by-agentid  "c88")  (f/fail derror/no-queued-jobs))))
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions" "rewards-question"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "1d2" "bills-questions" true)
    
  (testing "TEST-multiprimaryskill-test-case: BoJack receives job - 1d2"
    (is (= (:id (cr/job-request-by-agentid  "260")) "1d2")))
  (testing "TEST-multiprimaryskill-test-case: BoJack receives job - c36"
    (is (= (:id (cr/job-request-by-agentid  "260")) "c36")))
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions" "lost-theft-questions" "card-questions"])
  
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "lost-theft-questions" false)
  (coreptls/new-job "990" "card-questions" true)    
    
  (testing "TEST-multisecondaryskill-test-case: BoJack receives job - 864"
    (is (= (:id (cr/job-request-by-agentid  "260")) "864")))
  (testing "TEST-multisecondaryskill-test-case: Mr. Peanut Butter receives job - 990"
    (is (= (:id (cr/job-request-by-agentid  "c88")) "990")))
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions" "lost-theft-questions" "card-questions"])
  
  (coreptls/new-job "864" "bills-questions" true)
  (Thread/sleep 100)
  (coreptls/new-job "1d2" "lost-theft-questions" true)
  (Thread/sleep 100)
  (coreptls/new-job "990" "card-questions" true)
    
    
  (testing "TEST-dateorder-test-case: BoJack receives job - 864"
    (is (= (:id (cr/job-request-by-agentid  "260")) "864")))
  (testing "TEST-dateorder-test-case: Mr. Peanut Butter receives job - 1d2"
    (is (= (:id (cr/job-request-by-agentid  "c88")) "1d2"))))  
