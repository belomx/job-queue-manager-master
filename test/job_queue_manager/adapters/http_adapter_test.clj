(ns job-queue-manager.adapters.http_adapter_test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer :all]
            [job-queue-manager.adapters.http-adapter :as http-adp]
            [job-queue-manager.protocols.coreprotocols :as coreptls]
            [ring.mock.request :as mock]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(deftest job-queue-manager-api-test
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "Test POST request to /jobs returns expected response"
            
    (let [job {:id "690de6bc-163c-4345-bf6f-25dd0c58e864"
               :type "bills-questions"
               :urgent false
                 }
        response (http-adp/job-queue-manager-api (-> (mock/request :post "/jobs")
                          (mock/content-type "application/json")
                          (mock/body  (cheshire/generate-string job))))]   
      (is (= (:status response) 201))))
  
  (testing "Test GET request to /jobs check if the job was inserted properly."
                 
    (let [response (http-adp/job-queue-manager-api (-> (mock/request :get  "/jobs")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (=  (:id (first body) "690de6bc-163c-4345-bf6f-25dd0c58e864")))
      (is (=  (:type (first body) "bills-questions")))
      (is (=  (:urgent (first body) false)))
      (is (nil? (:working-agent-id (first body))))
      (is (= (:status (first body)) "waiting"))))
  
  (let [job {:id "f26e890b-df8e-422e-a39c-7762aa0bac36"
               :type "rewards-question"
               :urgent false
                 }
        response (http-adp/job-queue-manager-api (-> (mock/request :post "/jobs")
                          (mock/content-type "application/json")
                          (mock/body  (cheshire/generate-string job))))]   
      (is (= (:status response) 201)))
    
  (testing "Test POST to /agents returns expected response"
             
      (let [agent {:id "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"
                   :name "Mr. Peanut Butter"
                   :primary_skillset ["rewards-question"]
                   :secondary_skillset ["bills-questions"]
                 }
        response (http-adp/job-queue-manager-api (-> (mock/request :post "/agents")
                          (mock/content-type "application/json")
                          (mock/body  (cheshire/generate-string agent))))]   
      (is (= (:status response) 201))))
    
  (testing "Test POST /jobs/request assigns the job to the correct agent"
      (let [job-request {:agent_id "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"}
            response (http-adp/job-queue-manager-api (-> (mock/request :post "/jobs/request")
                          (mock/content-type "application/json")
                          (mock/body  (cheshire/generate-string job-request))))
            body          (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (:job_id body) "f26e890b-df8e-422e-a39c-7762aa0bac36"))
      (is (= (:agent_id body) "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"))))
  
  (testing "Test GET to /job returns the assigned job" 
    (let [response (http-adp/job-queue-manager-api (-> (mock/request :get  "/jobs")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (=  (:id (first body)) "f26e890b-df8e-422e-a39c-7762aa0bac36"))
      (is (=  (:type (first body)) "rewards-question"))
      (is (=  (:urgent (first body)) false))
      (is (= (:working-agent-id (first body)) "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"))
      (is (= (:status (first body)) "working"))))
  
  (testing "Test GET to /jobs/count_by_agent returns the correct count" 
    (let [response (http-adp/job-queue-manager-api (-> (mock/request :get  "/jobs/count_by_agent?agent-id=ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (=  (:rewards-question (first body)) 0))
      (is (=  (:bills-questions (last body)) 0))))
  
  (testing "Test POST /jobs/request assigns job"
      (let [job-request {:agent_id "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"}
            response (http-adp/job-queue-manager-api (-> (mock/request :post "/jobs/request")
                          (mock/content-type "application/json")
                          (mock/body  (cheshire/generate-string job-request))))
            body          (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (:job_id body) "690de6bc-163c-4345-bf6f-25dd0c58e864"))
      (is (= (:agent_id body) "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"))))
  
  (testing "Test GET to /jobs/count_by_agent returns the correct count" 
    (let [response (http-adp/job-queue-manager-api (-> (mock/request :get  "/jobs/count_by_agent?agent-id=ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (=  (:rewards-question (first body)) 1))
      (is (=  (:bills-questions (last body)) 0))))
  
  (testing "Test GET request to /jobs check if the job was assigned properly."
                 
    (let [response (http-adp/job-queue-manager-api (-> (mock/request :get  "/jobs")))
          body     (parse-body (:body response))]
      (is (= (:status response)) 200)
      (is (=  (:id (last body)) "690de6bc-163c-4345-bf6f-25dd0c58e864"))
      (is (=  (:type (last body)) "bills-questions"))
      (is (=  (:urgent (last body)) false))
      (is (= (:working-agent-id (last body)) "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"))
      (is (= (:status (last body)) "working"))))
  
  (testing "Test GET to /job returns the finished job" 
    (let [response (http-adp/job-queue-manager-api (-> (mock/request :get  "/jobs")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (=  (:id (first body)) "f26e890b-df8e-422e-a39c-7762aa0bac36"))
      (is (=  (:type (first body)) "rewards-question"))
      (is (=  (:urgent (first body)) false))
      (is (= (:working-agent-id (first body)) "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"))
      (is (= (:status (first body)) "done"))))
  
  (testing "Test POST /jobs/request assigns return message and complete the assigned job"
      (let [job-request {:agent_id "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"}
            response (http-adp/job-queue-manager-api (-> (mock/request :post "/jobs/request")
                          (mock/content-type "application/json")
                          (mock/body  (cheshire/generate-string job-request))))
            body          (parse-body (:body response))]
      (is (= (:status response) 500))
      (is (= (:id body) 1))
      (is (= (:message-text body) "No Queued jobs available."))))
  
  (testing "Test GET to /jobs/count_by_agent returns the correct count" 
    (let [response (http-adp/job-queue-manager-api (-> (mock/request :get  "/jobs/count_by_agent?agent-id=ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (=  (:rewards-question (first body)) 1))
      (is (=  (:bills-questions (last body)) 1))))
  
  (testing "Test GET request to /jobs check if the job was finished properly."
                 
    (let [response (http-adp/job-queue-manager-api (-> (mock/request :get  "/jobs")))
          body     (parse-body (:body response))]
      (is (= (:status response)) 200)
      (is (=  (:id (last body)) "690de6bc-163c-4345-bf6f-25dd0c58e864"))
      (is (=  (:type (last body)) "bills-questions"))
      (is (=  (:urgent (last body)) false))
      (is (= (:working-agent-id (last body)) "ed0e23ef-6c2b-430c-9b90-cd4f1ff74c88"))
      (is (= (:status (last body)) "done")))))

