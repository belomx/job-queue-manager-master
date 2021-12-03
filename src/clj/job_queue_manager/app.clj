(ns job-queue-manager.app
  (:require [job-queue-manager.adapters.file-adapter :as file-adp]
            [job-queue-manager.adapters.http-adapter :as http-adp]
            [job-queue-manager.protocols.deferror :as derror]
            [failjure.core :as f]
            [job-queue-manager.service :as service]))

(defn help-screen []  (println "
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  \r
//                                                                                                                                          //  \r
//     '||'         '||                                                                                                                     //  \r
//      ||    ...    || ...       ... .  ... ...    ....  ... ...    ....     .. .. ..    ....   .. ...    ....     ... .   ....  ... ..    //  \r
//      ||  .|  '|.  ||'  ||    .'   ||   ||  ||  .|...||  ||  ||  .|...||     || || ||  '' .||   ||  ||  '' .||   || ||  .|...||  ||' ''   //  \r
//      ||  ||   ||  ||    |    |.   ||   ||  ||  ||       ||  ||  ||          || || ||  .|' ||   ||  ||  .|' ||    |''   ||       ||       //  \r
//  || .|'   '|..|'  '|...'     '|..'||   '|..'|.  '|...'  '|..'|.  '|...'    .|| || ||. '|..'|' .||. ||. '|..'|'  '||||.  '|...' .||.      //  \r
//   '''                             ||                                                                           .|....'                   //  \r
//                                  ''''                                                                                                    //  \r
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  \r

USAGE: lein run -m job-queue-manager.app [ARGS...] \r

Argument options: \r
  -c, --console     Process the file inputed in the console, '^c' or '^z' to leave \r
  -f, --file        Process the input json file. Example: lein run -f /home/user/myfile.json \r
  -d, --directory   Process all the json files from the input directory. Example: lein run -f /home/user/ \r
  -w, --webapi      Start a server in the specified port \r
  -h, --help        Print the usage and options. \r
\r
"))

(defn process-file
  [f fp filepath] 
  (f/if-let-failed? [check-file-result (f filepath)]
    (println (:message-text (f/message check-file-result))) 
    (fp filepath)))

(defn process-console []
 (let  [filename (read-line)]  
   (f/if-let-failed? [result (file-adp/check-file-path filename)]
     (println (:message-text (f/message result)))
     (println (remove #(nil? %) (map #(service/call-service %) (file-adp/read-file filename)))))))

(defn -main [& args]  
  (println "Starting... \n")
  (if (true? (or (= "--help" (first args)) (= "-h" (first args))))
    (help-screen)
    (if (true? (or (= "--console" (first args)) (= "-c" (first args))))
      (process-console)
      (if-not (= (count args) 2)
        (println (:message-text derror/input-arguments-invalid))
        (if (true? (or (= "--webapi" (first args)) (= "-w" (first args))))
          (http-adp/start-server (last args))
          (if (true? (or (= "--file" (first args)) (= "-f" (first args))))
            (process-file file-adp/check-file-path file-adp/process-file (last args))
            (if (true? (or (= "--directory" (first args)) (= "-d" (first args))))
              (process-file file-adp/check-directory-path file-adp/process-files-from-directory (last args))
              (println (:message-text derror/input-arguments-invalid))))))))
  (println "Bye"))

