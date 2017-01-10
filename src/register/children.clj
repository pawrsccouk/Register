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
	    [html :as html]])
  (:gen-class))

;; Adding a new child to the database.

(defn- make-add-child-form
  "Returns a form for registering child information."
  []
  (html/page "Add a child to the register."
	     (form/form-to [:post "/children/add/"]
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
		     :address    (params :address)}]
    (jdbc/insert! db/info :children db-row-data)
    ;; Return a notification that the operation succeeded.
    (html/page "Child added"
	       [:h1 "Child added"]
	       [:p "Your child has been added to the database."]
	       [:p "Please click on the link below to return."]
	       [:a {:href (util/url html/website-base "/children/")} "All children"])))


(def add-child-handler
     "A Ring middleware handler which requests a form to add child data
or parses the results of said form to update the database."
     (wrap-params				; puts parameters in :form-params or :query-params
      (wrap-keyword-params			; as clojure keywords, not strings.
       (fn [header]
	 (html/content-type
	  (response
	    ;; GET is a request for the form itself, POST is the completed form.
	    (if (= (:request-method header) :post)
	      (parse-add-child-form header)
	      (make-add-child-form))))))))

;; Showing the children in the database

(defn- row-to-hiccup
  [rowmap]
  (let [cname (:childname rowmap)
	pname (:parentname rowmap)
	addr  (:address rowmap)
	chid  (:id rowmap)
	delform (form/form-to [:put "/children/remove/"]
			      (form/hidden-field "id" chid)
			      (form/submit-button "Delete"))]
    [:tr [:td cname] [:td pname] [:td addr] [:td delform]]))


(defn- get-all-children
  "Returns all the children in the database as a vector of Hiccup codes."
  []
  (jdbc/query db/info
	      ["select * from children"]
	      { :row-fn row-to-hiccup }))




(defn show-children-handler
  "A function which returns a page showing all the children in the database."
  [header]
  (html/content-type
   (response
    (html/page "Children"
	       [:h1 "Children"]
	       [:p "Here are all the children we have registered."]
	       [:table
		[:tr [:th "Child's Name"] [:th "Parent's Name"] [:th "Address"] [:th "Actions"]]
		(get-all-children)]
	       [:p
		[:a {:href (util/url "add/")} "Add a child"]]))))


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
	rows (jdbc/delete! db/info :children ["id = ?" chid])]
    (if (= (first rows) 1)
					; Return a notification that the operation succeeded.
      (html/page "Child removed"
		 [:h1 "Child remove"]
		 [:p "Your child has been removed from the database."]
		 [:p "Please click on the link below to return."]
		 [:a {:href (util/url html/website-base "/children/")} "All children"])
					; Ditto if it failed.
      (html/page "Error removing child"
		 [:h1 "Error removing child"]
		 [:p (str "There was a problem removing your child (ID " chid ") from the database.")]
		 [:p "Please click on the link below to return to the children table and try again,"
		  "or contact an administrator."]
		 [:a {:href (util/url html/website-base "/children/")} "All children"]))))


(def remove-child-handler
     "A Ring handler function to handle deleting a child from the database."
     (wrap-params				; puts parameters in :params... 
      (wrap-keyword-params			; ...as clojure keywords, not strings.
       (fn [header]
	 (html/content-type
	  (response
	   (parse-child-remove-form header)))))))


(def edit-child-handler
     "A Ring handler function to handle deleting a child from the database.
Currently not implemented."
     not-implemented-handler)

