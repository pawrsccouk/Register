(ns register.core
  (require java)
  (require [ring.util.response :refer [not-found response]])
  (require [ring.adapter.jetty :refer [run-jetty]])
  (require [ring.middleware
	    [stacktrace   :refer [wrap-stacktrace]]
	    [not-modified :refer [wrap-not-modified]]
	    [resource     :refer [wrap-resource]]
	    [content-type :refer [wrap-content-type]]]) 
  (require [register
	    [dispatch :refer [dispatch]]
	    [children :as children]])
  (:gen-class))

(defn- not-found-handler [header]
  (not-found (str "The resource you requested [" (:uri header) "] was not available.")))

(defn get-application
  "I return an application which can be passed to Jetty to run a web app."
  []
  ;; Note the dispatch checks the strings in order, so children/ must come after children/add
  ;; or it will always match.
  (let [static-handler (-> not-found-handler
			   (wrap-resource "public")
			   (wrap-content-type)
			   (wrap-not-modified))]
    (-> (dispatch static-handler ; I use the static handler as a not-found handler
		  "/children/add/"    children/add-child-handler
		  "/children/remove/" children/remove-child-handler
		  "/children/edit/"   children/edit-child-handler
		  "/children/"        children/show-children-handler)
	wrap-stacktrace)))

(defn main []
  "I return a running Jetty server set up to run this webapp."
  (let [app (get-application)]
    (run-jetty app {:port 3000 :join? false})))


