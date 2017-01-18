(ns register.children
  "I hold functions for administering a database of children belonging to a nursery.
I generate HTML from database queries and update the database with the result of 
HTML forms posted."
  (require [hiccup
	    [form :as form]
	    [util :as util]])
  (require [ring.middleware
	    [params :refer [wrap-params]]
	    [keyword-params :refer [wrap-keyword-params]]])
  (require [ring.util.response :refer [response]])
  (require [clojure.java.jdbc :as jdbc])
  (require [register
	    [database :as db]
	    [html :as html]
	    [dispatch :refer [dispatch]]])
  (:gen-class))

;; Database connection for this session.
(def dbinfo (db/info))

;; Adding a new child to the database.

(defn- make-add-child-form
  "Returns a form for registering child information."
  []
  (html/page "Add a child to the register."
	     (form/form-to [:post "/children/add/submit/"]
			   [:h1 "Add a child to the register"]
			   [:p "Please fill out the details to register your child's information."]
			   [:p
			    (form/label "childname" "Child's name")
			    (form/text-area "childname")
			    [:br]
			    (form/label "yourname" "Your name")	     
			    (form/text-area "yourname")
			    [:br]
			    (form/label "address" "Your address")
			    (form/text-area "address")
			    [:br]
			    (form/label "email" "Your email")
			    (form/text-area "email")
			    ]
			   [:br]		
			   (form/submit-button "Add child"))))


(defn- parse-add-child-form
  "Extracts data from the form created in make-form and stores it to the database.
This can throw exceptions, use higher-up middleware to catch them."
  [{params :params}]
  ;; First execute the SQL
  (let [db-row-data {:childname  (params :childname)
		     :parentname (params :yourname)
		     :address    (params :address)
		     :email      (params :email)}]
    (jdbc/insert! dbinfo :children db-row-data)
    ;; Return a notification that the operation succeeded.
    (html/page "Child added"
	       [:h1 "Child added"]
	       [:p "Your child has been added to the database."]
	       [:p "Please click on the link below to return."]
	       [:a {:href (util/url "../../")} "All children"])))


(def add-child-handler
     "A Ring middleware handler which requests a form to add child data
or parses the results of said form to update the database."
     (wrap-params				; puts parameters in :form-params or :query-params
      (wrap-keyword-params			; as clojure keywords, not strings.
       (fn [header]
	 (html/content-type
	  (response
	   ;; The completed form has a URI ending in /submit/. Process it.
	   ;; Otherwise this is a request for the form to be filled in.
	    (if (.endsWith (:uri header) "/submit/")
	      (parse-add-child-form header)
	      (make-add-child-form))))))))

;; Showing the children in the database

(defn- row-to-hiccup
  "Converts a table row given as a map by JDBC into a vector of hiccup symbols representing an HTML table row."
  [rowmap]
  (let [cname (:childname rowmap)
	pname (:parentname rowmap)
	addr  (:address rowmap)
	email (:email rowmap)
	chid  (:id rowmap)
	delform (form/form-to [:put "/children/remove/"]
			      (form/hidden-field "id" chid)
			      (form/submit-button "Delete"))
	updform (form/form-to [:put "/children/update/"]
			      (form/hidden-field "id" chid)
			      (form/submit-button "Update"))]
    [:tr [:td cname] [:td pname] [:td addr] [:td email]  [:td delform updform]]))


(defn show-children-handler
  "A function which returns a page showing all the children in the database."
  [header]
  ;; Run the query and then return an HTML page with the output.
  (let [dbinfo dbinfo
	children (jdbc/query dbinfo ["select * from children"] { :row-fn row-to-hiccup })]
    (html/content-type
     (response
      (html/page "Children"
		 [:h1 "Children"]
		 [:p "Here are all the children we have registered."]
		 [:table
		  [:tr [:th "Child's Name"] [:th "Parent's Name"] [:th "Address"] [:th "Email"] [:th "Actions"]]
		  children
		  ]
		 [:p
		  [:a {:href (util/url "add/")} "Add a child"]])))))


;; Not implemented yet.

(defn- not-implemented-handler
  "A function to return some HTML indicating a not-implemented page."
  [header]
  (html/content-type
   (response
    (html/page "Not implemented"
	       [:p "This is not implemented yet."]))))

(defn- parse-child-remove-form
  "I take the parameters from the header file, extract the ID from them
and use that to delete a row in the database, returning an HTML page with
an error or signifying success."
  [{params :params}]
  (let [chid (:id params)
	rows (jdbc/delete! dbinfo :children ["id = ?" chid])]
    (if (= (first rows) 1)
					; Return a notification that the operation succeeded.
      (html/page "Child removed"
		 [:h1 "Child remove"]
		 [:p "Your child has been removed from the database."]
		 [:p "Please click on the link below to return."]
		 [:a {:href (util/url "../")} "All children"])
					; Ditto if it failed.
      (html/page "Error removing child"
		 [:h1 "Error removing child"]
		 [:p (str "There was a problem removing your child (ID " chid ") from the database.")]
		 [:p "Please click on the link below to return to the children table and try again,"
		  "or contact an administrator."]
		 [:a {:href (util/url "../")} "All children"]))))


(def remove-child-handler
     "A Ring handler function to handle deleting a child from the database."
     (wrap-params				; puts parameters in :params... 
      (wrap-keyword-params			; ...as clojure keywords, not strings.
       (fn [header]
	 (html/content-type
	  (response
	   (parse-child-remove-form header)))))))



(defn- make-update-child-form
  "Returns a form for updating information about an existing child."
  [{params :params}]
  (let [chid (:id params)
	rows (jdbc/query dbinfo ["select * from children where id = ?" chid])
	row  (first rows)]
    (html/page (str "Update information for " (:childname row))
	       (form/form-to [:post "/children/update/submit/"]
			     (form/hidden-field "id" chid)
			     [:h1 "Update information for " (:childname row)]
			     [:p "Please change any details which are wrong and update the details."]
			     [:p
			      (form/label "childname" "Child's name")
			      (form/text-area "childname" (:childname row))
			      [:br]
			      (form/label "yourname" "Your name")	     
			      (form/text-area "yourname" (:parentname row))
			      [:br]
			      (form/label "address" "Your address")
			      (form/text-area "address" (:address row))
			      [:br]
			      (form/label "email" "Your email")
			      (form/text-area "email" (:email row))
			      ]
			     [:br]		
			     (form/submit-button "Update"))
	       [:a {:href (util/url "../")} "Cancel"])))


(defn- parse-update-child-form
  "Extracts data from the form created in make-form and stores it to the database.
This can throw exceptions, use higher-up middleware to catch them."
  [{params :params}]
  ;; First execute the SQL
  (let [chid (:id params)
	db-row-data {:childname  (params :childname)
		     :parentname (params :yourname)
		     :address    (params :address)
		     :email      (params :email)}
	row (jdbc/update! dbinfo :children db-row-data ["id = ?" (params :id)])]
    (if (= (first row) 1)
      ;; Return a notification that the operation succeeded.
      (html/page "Child updated"
		 [:h1 (params :childname) " updated"]
		 [:p  (params :childname) " has been updated in the database."]
		 [:p "Please click on the link below to return."]
		 [:a {:href (util/url "../../")} "All children"])
      ;; ditto if it failed.
      (html/page "Error updating child"
		 [:h1 "Error updating details for " (params :childname) ]
		 [:p "There was a problem updating your child (ID " chid ") in the database."]
		 [:p "Please click on the link below to return to the children table and try again,"
		  "or contact an administrator."]
		 [:a {:href (util/url "../../")} "All children"]))))

(def update-child-handler
     "A Ring middleware handler which pre-fills a form with child data and presents it to the user
or parses the results of said form to update the child's entry in the database."
     (wrap-params				; puts parameters in :form-params or :query-params
      (wrap-keyword-params			; as clojure keywords, not strings.
       (fn [header]
	 (html/content-type
	  (response
	   ;; a URI ending in /submit/ is the completed form needing processing
	   ;; otherwise we want the form to be filled in.
	   (if (.endsWith (:uri header) "/submit/")
	     (parse-update-child-form header)
	     (make-update-child-form header))))))))

(def children-handler
     "I am a handler to call functions based on the URI for the children subsite."
     ;; Note the dispatch checks the strings in order, so children/ must come after children/add
     ;; or it will always match.
     (dispatch html/not-found-handler
	       "/children/add/"    add-child-handler
	       "/children/remove/" remove-child-handler
	       "/children/update/" update-child-handler
	       "/children/"        show-children-handler))

