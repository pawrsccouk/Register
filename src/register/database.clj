(ns register.database
  "I hold information for specifying the current database and any helper functions
which may be useful for database work."
  (:gen-class))


(defn info
  "Returns the database information required to connect via jdbc."
  []
  (let [user     (System/getenv "REGISTER_DB_USER")
	password (System/getenv "REGISTER_DB_PASSWORD")
	dbinfo   (System/getenv "REGISTER_DB_INFO")]  ;; "//127.0.0.1:3306/register"
    (when (some nil? [user password dbinfo])
      (throw (NullPointerException. "Missing one of REGISTER_DB_INFO, REGISTER_DB_PASSWORD, REGISTER_DB_USER")))
    {:classname "com.mysql.cj.jdbc.Driver"
     :subprotocol "mysql"
     :subname dbinfo ;; host:port/database-name
     :user user
     :password password }))

;; See http://clojure-doc.org/articles/ecosystem/java_jdbc/home.html
;; for java.jdbc information.
