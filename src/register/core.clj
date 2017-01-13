(ns register.core
  "I represent the entrance point to the webserver. 
Here the main application function is created and the Jetty server is launched."
  (require [ring.adapter.jetty :refer [run-jetty]])
  (require [ring.middleware
	    [stacktrace   :refer [wrap-stacktrace]]
	    [not-modified :refer [wrap-not-modified]]
	    [resource     :refer [wrap-resource]]
	    [content-type :refer [wrap-content-type]]
	    [basic-authentication :refer [wrap-basic-authentication]]])
  (require [register
	    [html     :as    html]
	    [database :as    db]
	    [dispatch :refer [dispatch]]
	    [children :refer [children-handler]]])
  (require [crypto.password.pbkdf2 :as password])
  (require [clojure.java.jdbc :as jdbc])
  (:gen-class))

(defn- wrap-transform-root-uri
  "I wrap a handler and check the URI in the header passed in.
If the header refers to a root-uri (e.g. nil or / or the empty string)
then I replace it with root-replacement (for example /index.html)."
  [handler root-replacement]
  (fn [header]
    (let [new-header (update-in header [:uri] #(if (or (nil? %)
						       (.isEmpty %)
						       (= "/" %))
						 root-replacement
						 %))]
      (handler new-header))))

(defn- authfn?
  "Checks the user and password against the database and returns true
if the user is authenticated."
  [userid password]
  ;; Passwords were encrypted with 
  ;; (password/encrypt <password> 100000 "HMAC-SHA256")
  ;; not that this matters, these details are included in the encrypted string.
  (println "Testing " userid password)
  (try
   (when (or (nil? userid) (nil? password))
     (throw (NullPointerException. "User ID or password is null")))     
   (let [rows (jdbc/query db/info ["select password from users where userid = ?" userid])
	 row (first rows)
	 encrypted (:password row)]
     (if (nil? encrypted) ; If there was no row in the database or the row had no password entry.
       false		  ; then return false;
       (password/check password encrypted)))  ; Check the encrypted password and return true if it matches.
   (catch Exception e
     ;; Log exceptions but always return false, so there's no chance of an exception
     ;; causing the authority check to be bypassed.
     (.printStackTrace e *err*)
     false)))





(defn get-application
  "I return an application which can be passed to Jetty to run a web app."
  []
  (let [static-handler (-> html/not-found-handler
			   (wrap-resource "public")
			   (wrap-content-type)
			   (wrap-not-modified))]
    (-> (dispatch static-handler ; I use the static handler as a not-found handler
		  "/children/" (wrap-basic-authentication children-handler authfn?))
	wrap-stacktrace
	(wrap-transform-root-uri "/index.html"))))

(defn main []
  "I return a running Jetty server set up to run this webapp."
  (let [app (get-application)]
    (run-jetty app {:port 3000 :join? false})))


