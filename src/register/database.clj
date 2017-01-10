(ns register.database
  "I hold information for specifying the current database and any helper functions
which may be useful for database work."
  (:gen-class))

;; The database information required to connect via jdbc.
(def info {:classname "com.mysql.cj.jdbc.Driver"
	   :subprotocol "mysql"
	   :subname "//127.0.0.1:3306/register" ;; host:port/database-name
	   :user "paw"
	   :password "15t2chr2"})

;; See http://clojure-doc.org/articles/ecosystem/java_jdbc/home.html
;; for java.jdbc information.
