(ns register.sqlcheck
  (require java)
  (require [clojure.java.jdbc :as jdbc])
  (:gen-class))

(def dbinfo {:classname "com.mysql.cj.jdbc.Driver"
	     :subprotocol "mysql"
	     :subname "//127.0.0.1:3306/finances" ;; host:port/database-name
	     :user "paw"
	     :password "15t2chr2"})

;; This does a direct query and returns a lazy seq of maps, one per row.
;; The DB is opened, queried and closed as an atomic operation.
(defn finance-data []
  (jdbc/query dbinfo
	      ["select * from outgoings;"]))

;; See http://clojure-doc.org/articles/ecosystem/java_jdbc/home.html
;; for java.jdbc information.
