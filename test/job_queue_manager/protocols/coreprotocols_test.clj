(ns job-queue-manager.protocols.coreprotocols_test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]  
            [job-queue-manager.core :as cr]
            [job-queue-manager.protocols.coreprotocols :as coreptls]
            [job-queue-manager.protocols.deferror :as derror]))

(deftest agent-exists?-test
  
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])     
    
  (testing "TEST-agent-exists?: Agent BoJack Horseman exists"
    (is (true?(coreptls/agent-exists?  "260"))))
  (testing "TEST-agent-exists?: Agent Mr. Peanut Butter exists"
    (is (true?(coreptls/agent-exists?  "c88"))))
  (testing "TEST-agent-exists?: Agent Mr. Ronaldo doesn't exist"
    (is (not (true? (coreptls/agent-exists?  "123"))))))

(deftest job-exists?-test
  
  (coreptls/clean-all-jobs)
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
    
  (testing "TEST-job-exists?: Job c36 exists"
    (is (true? (coreptls/job-exists?  "c36"))))
  (testing "TEST-job-exists?: Job 864 exists"
    (is (true? (coreptls/job-exists?  "864"))))
  (testing "TEST-job-exists?: Job 1d2 exists"
    (is (true? (coreptls/job-exists?  "1d2"))))
  (testing "TEST-job-exists?: Job 123 doesn't exist"
    (is (not (true? (coreptls/job-exists?  "123"))))))

(deftest validate-agent-input-test
  
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])     
  
  (testing "TEST-validate-agent-input: Agent has already been created - error message"
    (is (= (f/message (coreptls/validate-agent-input "260" "Ronaldo" ["bills-questions"] [])) derror/agent-already-exists)))
  (testing "TEST-new-agent: Agent id is null - error message"
    (is (= (f/message (coreptls/validate-agent-input nil "Ronaldo" ["bills-questions"] [])) derror/agent-id-invalid)))
  (testing "TEST-validate-agent-input: Agent's name not valid - error message"
    (is (= (f/message (coreptls/validate-agent-input "123" 101010 ["life-questions"] [])) derror/agent-name-not-valid))
  (testing "TEST-validate-agent-input: Agent's primary skill set was not provided - error message"
    (is (= (f/message (coreptls/validate-agent-input "123" "Ronaldo" [] [])) derror/agent-primaryskilset-not-provided))))
  (testing "TEST-validate-agent-input: OK"
    (is (nil? (coreptls/validate-agent-input "123" "Ronaldo" ["Soccer-Questions" ] ["Futebol-questions"])))))

(deftest validate-job-input-test
  
  (coreptls/clean-all-jobs)
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
    
  (testing "TEST-validate-job-input: Job c36 has already been added - error message"
    (is (= (f/message (coreptls/validate-job-input "c36" "rewards-question" false)) derror/job-already-exists)))
  (testing "TEST-validate-job-input: JobId is null - error message"
    (is (= (f/message (coreptls/validate-job-input nil "rewards-question" false)) derror/job-id-invalid)))
  (testing "TEST-validate-job-input: Job's type is not properly provided - error message 1"
    (is (= (f/message (coreptls/validate-job-input "777" 123 false)) derror/job-type-not-provided)))
  (testing "TEST-validate-job-input: Job's type is not properly provided - error message 2"
    (is (= (f/message (coreptls/validate-job-input "777" "" false)) derror/job-type-not-provided)))
  (testing "TEST-validate-job-input: Job's type is not properly provided - error message 3"
    (is (= (f/message (coreptls/validate-job-input "777" nil false)) derror/job-type-not-provided)))
  (testing "TEST-validate-job-input: Job's urgent flag is not correct - error message 1"
    (is (= (f/message (coreptls/validate-job-input "777" "Heskel-issues-questions" 123)) derror/job-urgent-flag-not-correct)))
  (testing "TEST-validate-job-input: Job's urgent flag is not correct - error message 2"
    (is (= (f/message (coreptls/validate-job-input "777" "Heskel-issues-questions" nil)) derror/job-urgent-flag-not-correct)))
  (testing "TEST-validate-job-input: Job is OK"
    (is (nil? (coreptls/validate-job-input "777" "Heskel-issues-questions" true)))))

(deftest new-agent-test
  
  (coreptls/clean-all-agents)
  
  (coreptls/new-agent "260" "BoJack Horseman" ["bills-questions"] [])
  (coreptls/new-agent "c88"  "Mr. Peanut Butter" ["rewards-question"] ["bills-questions"])     
  
  (testing "TEST-new-agent: Agent has already been created - error message"
    (is (= (f/message (coreptls/new-agent "260" "Ronaldo" ["bills-questions"] [])) derror/agent-already-exists)))
  (testing "TEST-new-agent: Agent id is null - error message"
    (is (= (f/message (coreptls/new-agent nil "Ronaldo" ["bills-questions"] [])) derror/agent-id-invalid)))
  (testing "TEST-new-agent: Agent's name not valid - error message"
    (is (= (f/message (coreptls/new-agent "123" 101010 ["life-questions"] [])) derror/agent-name-not-valid))
  (testing "TEST-new-agent: Agent's primary skill set was not provided - error message"
    (is (= (f/message (coreptls/new-agent "123" "Ronaldo" [] [])) derror/agent-primaryskilset-not-provided))))
  (testing "TEST-new-agent: OK"
    (is (nil? (coreptls/new-agent "123" "Ronaldo" ["Soccer-Questions" ] ["Futebol-questions"])))))

(deftest new-job-test
  
  (coreptls/clean-all-jobs)
  
  (coreptls/new-job "c36" "rewards-question" false)
  (coreptls/new-job "864" "bills-questions" false)
  (coreptls/new-job "1d2" "bills-questions" true)
    
  (testing "TEST-validate-job-input: Job c36 has already been added - error message"
    (is (= (f/message (coreptls/new-job "c36" "rewards-question" false)) derror/job-already-exists)))
  (testing "TEST-validate-job-input: JobId is null - error message"
    (is (= (f/message (coreptls/new-job nil "rewards-question" false)) derror/job-id-invalid)))
  (testing "TEST-validate-job-input: Job's type is not properly provided - error message 1"
    (is (= (f/message (coreptls/new-job "777" 123 false)) derror/job-type-not-provided)))
  (testing "TEST-validate-job-input: Job's type is not properly provided - error message 2"
    (is (= (f/message (coreptls/new-job "777" "" false)) derror/job-type-not-provided)))
  (testing "TEST-validate-job-input: Job's type is not properly provided - error message 3"
    (is (= (f/message (coreptls/new-job "777" nil false)) derror/job-type-not-provided)))
  (testing "TEST-validate-job-input: Job's urgent flag is not correct - error message 1"
    (is (= (f/message (coreptls/new-job "777" "Heskel-issues-questions" 123)) derror/job-urgent-flag-not-correct)))
  (testing "TEST-validate-job-input: Job's urgent flag is not correct - error message 2"
    (is (= (f/message (coreptls/new-job "777" "Heskel-issues-questions" nil)) derror/job-urgent-flag-not-correct)))
  (testing "TEST-validate-job-input: Job is OK"
    (is (nil? (coreptls/new-job "777" "Heskel-issues-questions" true)))))
