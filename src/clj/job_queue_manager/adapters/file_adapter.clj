(ns job-queue-manager.adapters.file-adapter
  (:require [clojure.data.json :as json]
            [job-queue-manager.service :as service]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [job-queue-manager.protocols.deferror :as derror]
            [failjure.core :as f])
  (:import  [java.util Date]))

(defn ^:private rand-str []
  (apply str (take 5 (repeatedly #(char (+ (rand 26) 65))))))

(defn read-file [filename] 
  (json/read-str (slurp (str filename)) :key-fn keyword))

(defn check-file-path
  [filepath]
  (if-not (.exists (io/file filepath)) (f/fail derror/file-path-invalid)))

(defn windows-path?
  [filepath]
  (not (boolean (re-find #"/" filepath))))

(defn get-directory-delimiter 
  [filepath]
  (if (true? (windows-path? filepath)) "\\" "/"))

(def get-directory-delimiter (memoize get-directory-delimiter))

(defn ^:private append-directory-delimiter
  ([directory] (append-directory-delimiter directory ""))
  ([directory sufix-val]
    (if (= (str (last (str directory))) (get-directory-delimiter directory))
      (str directory sufix-val) 
      (str directory (get-directory-delimiter directory) sufix-val))))

(def append-directory-delimiter (memoize append-directory-delimiter))

(defn get-file-name
  [filepath]
  (f/if-let-failed? [check-file-path-result (check-file-path filepath)]
    check-file-path-result                
    (subs filepath (+ (str/last-index-of filepath (get-directory-delimiter filepath)) 1) (count filepath))))

(defn check-directory-path 
  [directory-path]
  (if-not (.isDirectory (io/file directory-path)) (f/fail derror/directory-path-invalid) directory-path))

(defn ^:private get-directorypath-from-filepath 
  [filepath]
  (str
    (subs 
      filepath 
      0 
      (str/last-index-of filepath (get-directory-delimiter filepath)))
    (get-directory-delimiter filepath)))

(defn get-directory
  [filepath]
  (f/if-let-failed? [result (check-directory-path filepath)]    
    (f/if-let-failed? [check-directory-path-result
        (check-directory-path (get-directorypath-from-filepath filepath))]
      check-directory-path-result
      (io/file check-directory-path-result))
    result))

(defn get-jsonfiles-from-directory
  [directorypath]
  (filter
    #(and 
       (not (nil? (re-find #".json" (.getName %))))
       (= 
         (append-directory-delimiter directorypath) 
          (str (.getAbsolutePath (get-directory (.getAbsolutePath %))) (get-directory-delimiter directorypath)))) 
    (file-seq (io/file directorypath))))

(defn create-output-file
  [response directory-path]
  (let [directory 
    (append-directory-delimiter directory-path)]
    (let [response-filename 
          (str directory "response-" (.getTime (Date.)) (rand-str) ".json")]
      (println (str "Creating output file: " response-filename))
      (spit response-filename (json/write-str response))
      response-filename)))

(defn ^:private create-output-directory-by-name 
  [directory directory-to-be-created]
  (let [new-directory
            (append-directory-delimiter directory directory-to-be-created)]
        (f/if-let-failed? [check-directory-path-result (check-directory-path new-directory)]      
            (.mkdir (java.io.File. new-directory))) 
    (str new-directory (get-directory-delimiter directory))))

(defn create-output-directory
  ([directory] 
      (create-output-directory 
        (append-directory-delimiter directory)
        (str "output-" (.getTime (Date.)) (rand-str))))
  ([directory directory-to-be-created]
    (f/if-let-failed? [result (check-directory-path directory)]
      result
      (create-output-directory-by-name directory directory-to-be-created))))

(defn process-file
  ([filename] (process-file filename (str (.getPath (get-directory filename)) (get-directory-delimiter filename))))
  ([filename output-directory-path]  
  (println (str "Processing :" filename))
  (let [response (remove #(nil? %) (map #(service/call-service %) (read-file filename)))]
    (create-output-file response output-directory-path)
    response)))

(defn process-files-from-directory
 ([directory-path]
   (let [output-path (create-output-directory directory-path)]
     (process-files-from-directory directory-path output-path (get-jsonfiles-from-directory directory-path))))
 ([directory-path output-path files]
  (if-not (empty? files)
    (do
      (process-file (.getAbsolutePath (first files)) output-path)
      (process-files-from-directory directory-path output-path (rest files))))))
