(ns job-queue-manager.adapters.file-adapter-test
  (:require [clojure.test :refer :all] 
            [clojure.data :as data]
            [clojure.java.io :as io]
            [failjure.core :as f]
            [job-queue-manager.protocols.deferror :as derror]
            [job-queue-manager.protocols.coreprotocols :as coreptls]
            [job-queue-manager.adapters.file-adapter :as file-adp]))

(def basic-input-file              (io/file (.getFile (io/resource "example_inputs/basic/input.json" ))))
(def basic-input-jvmpath           (.getFile (io/resource "example_inputs/basic/input.json" )))
(def basic-input-path              (.getAbsolutePath basic-input-file))
(def basic-input-directory-file    (io/file (.getFile (io/resource "example_inputs/basic/" ))))
(def basic-input-directory         (.getAbsolutePath basic-input-directory-file))

(def basic-out-file                (io/file (.getFile (io/resource "example_inputs/basic/output/output.json" ))))
(def basic-out-path                (.getAbsolutePath basic-out-file))
(def basic-out-directory-file      (io/file (.getFile (io/resource "example_inputs/basic/output/" ))))
(def basic-out-directory           (.getAbsolutePath basic-out-directory-file))

(def multiskill-out-file           (io/file (.getFile (io/resource "example_inputs/multiskill/output/output.json" ))))
(def multiskill-out-path           (.getAbsolutePath multiskill-out-file))
(def multiskill-out-directory-file (io/file (.getFile (io/resource "example_inputs/multiskill/output/" ))))
(def multiskill-out-directory      (.getAbsolutePath multiskill-out-directory-file))

(def multiskill-input-file           (io/file (.getFile (io/resource "example_inputs/multiskill/input.json" ))))
(def multiskill-input-path           (.getAbsolutePath multiskill-input-file))
(def multiskill-input-directory-file (io/file (.getFile (io/resource "example_inputs/multiskill/" ))))
(def multiskill-input-directory      (.getAbsolutePath multiskill-input-directory-file))

(def multiskill-secondary-out-file           (io/file (.getFile (io/resource "example_inputs/multiskill_secondary/output/output.json" ))))
(def multiskill-secondary-out-path           (.getAbsolutePath multiskill-secondary-out-file))
(def multiskill-secondary-out-directory-file (io/file (.getFile (io/resource "example_inputs/multiskill_secondary/output/" ))))
(def multiskill-secondary-out-directory      (.getAbsolutePath multiskill-secondary-out-directory-file))

(def multiskill-secondary-input-file           (io/file (.getFile (io/resource "example_inputs/multiskill_secondary/input.json" ))))
(def multiskill-secondary-input-path           (.getAbsolutePath multiskill-secondary-input-file))
(def multiskill-secondary-input-directory-file (io/file (.getFile (io/resource "example_inputs/multiskill_secondary/" ))))
(def multiskill-secondary-input-directory      (.getAbsolutePath multiskill-secondary-input-directory-file))

(def multiskill-urgent-out-file           (io/file (.getFile (io/resource "example_inputs/multiskill_urgent/output/output.json" ))))
(def multiskill-urgent-out-path           (.getAbsolutePath multiskill-urgent-out-file))
(def multiskill-urgent-out-directory-file (io/file (.getFile (io/resource "example_inputs/multiskill_urgent/output/" ))))
(def multiskill-urgent-out-directory      (.getAbsolutePath multiskill-urgent-out-directory-file))

(def multiskill-urgent-input-file           (io/file (.getFile (io/resource "example_inputs/multiskill_urgent/input.json" ))))
(def multiskill-urgent-input-path           (.getAbsolutePath multiskill-urgent-input-file))
(def multiskill-urgent-input-directory-file (io/file (.getFile (io/resource "example_inputs/multiskill_urgent/" ))))
(def multiskill-urgent-input-directory      (.getAbsolutePath multiskill-urgent-input-directory-file))

(def sample-out-file           (io/file (.getFile (io/resource "example_inputs/sample/output/output_sample.json" ))))
(def sample-out-path           (.getAbsolutePath sample-out-file))
(def sample-out-directory-file (io/file (.getFile (io/resource "example_inputs/sample/output/" ))))
(def sample-out-directory      (.getAbsolutePath sample-out-directory-file))

(def sample-input-file           (io/file (.getFile (io/resource "example_inputs/sample/input_sample.json" ))))
(def sample-input-path           (.getAbsolutePath sample-input-file))
(def sample-input-directory-file (io/file (.getFile (io/resource "example_inputs/sample/" ))))
(def sample-input-directory      (.getAbsolutePath sample-input-directory-file))

(def secondary-out-file           (io/file (.getFile (io/resource "example_inputs/secondary/output/output.json" ))))
(def secondary-out-path           (.getAbsolutePath secondary-out-file))
(def secondary-out-directory-file (io/file (.getFile (io/resource "example_inputs/secondary/output/" ))))
(def secondary-out-directory      (.getAbsolutePath secondary-out-directory-file))

(def secondary-input-file           (io/file (.getFile (io/resource "example_inputs/secondary/input.json" ))))
(def secondary-input-path           (.getAbsolutePath secondary-input-file))
(def secondary-input-directory-file (io/file (.getFile (io/resource "example_inputs/secondary/" ))))
(def secondary-input-directory      (.getAbsolutePath secondary-input-directory-file))

(def urgent-out-file           (io/file (.getFile (io/resource "example_inputs/urgent/output/output.json" ))))
(def urgent-out-path           (.getAbsolutePath urgent-out-file))
(def urgent-out-directory-file (io/file (.getFile (io/resource "example_inputs/urgent/output/" ))))
(def urgent-out-directory      (.getAbsolutePath urgent-out-directory-file))

(def urgent-input-file           (io/file (.getFile (io/resource "example_inputs/urgent/input.json" ))))
(def urgent-input-path           (.getAbsolutePath urgent-input-file))
(def urgent-input-directory-file (io/file (.getFile (io/resource "example_inputs/urgent/" ))))
(def urgent-input-directory      (.getAbsolutePath urgent-input-directory-file))

(def invalid-input                 "example_inputs/invalid-input.json")
(def invalid-path                  "example_inputs/xablau")

(defn equal-files?
  ([a b]
  (= [nil nil a]  (data/diff a b)))
  ([a b f]
  (= [nil nil (f a)]  (data/diff (f a) (f b)))))

(defn not-equal-files?
  ([a b]
  (not (equal-files? a b)))
  ([a b f]
  (not (equal-files? a b f))))

(deftest check-file-path  
  (testing "TEST-check-file-path: checking a valid file"
           (is (nil? (file-adp/check-file-path basic-input-file))))
  (testing "TEST-check-file-path: checking an invalid file - an error must return"
           (= (f/message (file-adp/check-file-path invalid-input)) derror/file-path-invalid)))

(deftest windows-path?
  (testing "TEST-windows-path?: The files must not be windows path as the JVM handles the files Unix like"
           (is (not (true? (file-adp/windows-path? basic-input-jvmpath))))))

(deftest get-directory-delimiter
  (testing "TEST-get-directory-delimiter: The function must return the Unix like directory delimiter '/'"
           (is (= (file-adp/get-directory-delimiter basic-input-jvmpath) "/")))
  (testing "TEST-get-directory-delimiter: The function must return the Windows like directory delimiter '\\'"
           (is (= (file-adp/get-directory-delimiter "C:\\Fake\\path\\just\\to\\test") "\\"))))

(deftest get-file-name
  (testing "TEST-get-file-name: The function must return the file name from a Unix like valid file - input.json"
           (is (= (file-adp/get-file-name basic-input-jvmpath) "input.json")))
  (testing "TEST-get-file-name: The function must return the file name from a Windows(maybe) like valid file - input.json"
           (is (= (file-adp/get-file-name basic-input-path) "input.json")))
  (testing "TEST-get-file-name: The function must return an error message, due to the invalid path file"
           (is (= (f/message (file-adp/get-file-name invalid-input)) derror/file-path-invalid))))

(deftest get-directory
  (testing "TEST-get-directory: The function must return the directory of the Unix like file"
           (is (= (file-adp/get-directory basic-input-jvmpath) basic-input-directory-file)))
  (testing "TEST-get-directory: The function must return an error message, due to the invalid path file"
           (is (= (f/message (file-adp/get-directory invalid-input)) derror/directory-path-invalid))))

(deftest check-directory-path
  (testing "TEST-check-directory-path: The function must return the direcory path"
           (is (= (.getAbsolutePath (file-adp/check-directory-path basic-input-directory-file)) basic-input-directory)))
  (testing "TEST-get-directory: The function must return an error message, due to the invalid path file"
           (is (= (f/message (file-adp/check-directory-path invalid-input)) derror/directory-path-invalid))))

(deftest get-jsonfiles-from-directory
  (testing "TEST-get-jsonfiles-from-directory: The function must return at least the basic input json file"
           (is (true? (some #(= basic-input-file %) (file-adp/get-jsonfiles-from-directory basic-input-directory))))))

(deftest create-output-file
  (testing "TEST-create-output-file: check if the file created is correct"
           (is (equal-files? (file-adp/create-output-file (file-adp/read-file basic-out-file) basic-out-directory) basic-out-file file-adp/read-file)))
  (testing "TEST-create-output-file: Fail test, there must be differences between the files"
           (is (not-equal-files?  (file-adp/create-output-file (file-adp/read-file basic-out-file) basic-out-directory) basic-input-file file-adp/read-file))))

(deftest create-output-directory
  (testing "TEST-create-output-directory: check if the new directory was created"
           (is (true? (.isDirectory (io/file (file-adp/create-output-directory basic-input-directory))))))
  (testing "TEST-create-output-directory: check if the new directory was created"
           (is (true? (.isDirectory (io/file (file-adp/create-output-directory basic-input-directory "new-output"))))))
  (testing "TEST-create-output-directory: check if the existed directory path was returned"
           (is (true? (.isDirectory (io/file  (file-adp/create-output-directory basic-input-directory "output"))))))
  (testing "TEST-create-output-directory: the function must return an error due to the invalid directory"
           (is (= (f/message (file-adp/create-output-directory invalid-path "output")) derror/directory-path-invalid))))
  (testing "TEST-create-output-directory: the function must return an error due to the invalid directory"
           (is (= (f/message (file-adp/create-output-directory invalid-path)) derror/directory-path-invalid)))
  
(deftest process-file
  
  ;Testing Basic
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "TEST-process-file-basic: check if the results are the same comparing with the expected file"
           (is (equal-files? (file-adp/process-file basic-input-path) (file-adp/read-file basic-out-file))))
  
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "TEST-process-file-basic: Fail test to check if the test mechanic is working"
           (is (not (equal-files? (file-adp/process-file basic-input-path) (file-adp/read-file multiskill-out-file)))))

  ;Testing multiskill
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "TEST-process-file-multiskill: check if the results are the same comparing with the expected file"
           (is (equal-files? (file-adp/process-file multiskill-input-path) (file-adp/read-file multiskill-out-file))))
  ;Testing multiskill-secondary
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "TEST-process-file-multiskill-secondary: check if the results are the same comparing with the expected file"
           (is (equal-files? (file-adp/process-file multiskill-secondary-input-path) (file-adp/read-file multiskill-secondary-out-file))))
  ;Testing multiskill-urgent
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "TEST-process-file-multiskill-urgent: check if the results are the same comparing with the expected file"
           (is (equal-files? (file-adp/process-file multiskill-urgent-input-path) (file-adp/read-file multiskill-urgent-out-file))))
  ;Testing sample
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "TEST-process-file-sample: check if the results are the same comparing with the expected file"
           (is (equal-files? (file-adp/process-file sample-input-path) (file-adp/read-file sample-out-file))))
  ;Testing secondary
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "TEST-process-file-secondary: check if the results are the same comparing with the expected file"
           (is (equal-files? (file-adp/process-file secondary-input-path) (file-adp/read-file secondary-out-file))))
  ;Testing urgent
  (coreptls/clean-all-jobs)
  (coreptls/clean-all-agents)
  
  (testing "TEST-process-file-basic: check if the results are the same comparing with the expected file"
           (is (equal-files? (file-adp/process-file urgent-input-path) (file-adp/read-file urgent-out-file)))))  
