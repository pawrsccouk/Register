(ns register.core
  "I represent the entrance point to the webserver. 
Here the main application function is created and the Jetty server is launched."
  (require [ring.adapter.jetty :refer [run-jetty]])
  (require [ring.util.response :refer [response]])
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
  (try
   (when (or (nil? userid) (nil? password))
     (throw (NullPointerException. "User ID or password is null")))     
   (let [rows (jdbc/query (db/info) ["select password from users where userid = ?" userid])
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


(defn- bg-stop-server
  "I run on a background thread and attempt to shutdown the jetty server.
I take a reference to the server to shut down."
  [rserver]
  (try (.println *err*  "Shutting down Jetty...")
       (.stop @rserver)
       (.println *err*  "Jetty stopped.")
       (catch Exception ex
	 (.println *err* (str "Error when stopping Jetty: " (.getMessage ex) ex)))))


(defn shutdown-handler
  "I return a function which will shut down the JETTY server when called.
I take a reference to the server to be shut down."
  [rserver]
  (fn [header]
    ;; return the response and stop the server in the background.
    (let [resp (html/content-type
		(response
		 (html/page "Server shutdown"
			    [:h1 "Shutdown"]
			    [:p "The server has shutdown successfully"])))]
      (try (.start (Thread. #(bg-stop-server rserver)))
	   (catch Exception ex
	     (.println *err* (str "Unable to stop Jetty: " + (.getMessage ex) ex))))
      resp)))

(defn get-application
  "I return an application which can be passed to Jetty to run a web app.
I take a reference which will later be set to the running server object.
(Do not use this immediately, only in handlers when the server has been started.)"
  [rserver]
  (let [static-handler (-> html/not-found-handler
			   (wrap-resource "public")
			   (wrap-content-type)
			   (wrap-not-modified))]
    (-> (dispatch static-handler ; I use the static handler as a not-found handler
		  "/children/" (wrap-basic-authentication children-handler authfn?)
		  "/shutdown"  (shutdown-handler rserver))
	wrap-stacktrace
	(wrap-transform-root-uri "/index.html"))))

(defn- -run
  "I run the server as a background thread and return the server object.
I take the port to run it on."
  [port]
  ; Use a reference to the server so we can pass it to the handler before the server is created.
  (let [rserver (ref nil)
	app (get-application rserver)
	server (run-jetty app {:port port :join? false})]
    ; Allow current requests to stop and clean up when the JVM shuts down.
    (.setStopTimeout server 1000)
    (.setStopAtShutdown server true)
    (dosync (ref-set rserver server))
    server))


(defn run
  "I return a running Jetty server set up to run this webapp.
This doesn't block and is intended to be used in the REPL."
  []
  (-run 3000))

(defn -main
  "I run a server set up to display this webapp.
I block until the server is shut down, so I am used in 'lein run' calls."
  []
  (-run 3000))
