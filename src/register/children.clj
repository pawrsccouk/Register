(ns register.children
  (require java)			; My java debugging file.
  (require [hiccup [form :as form] [page :as page] [util :as util]])
  (require [ring.middleware [params :refer [wrap-params]] [keyword-params :refer [wrap-keyword-params]]])
  (require [ring.util.response :refer [response content-type]])
  (require [clojure.java.jdbc :as jdbc])
  (require [clojure.pprint :refer [pprint]])
  (:gen-class))

;; The database information required to connect via jdbc.
(def dbinfo {:classname "com.mysql.cj.jdbc.Driver"
             :subprotocol "mysql"
             :subname "//127.0.0.1:3306/register" ;; host:port/database-name
             :user "paw"
             :password "15t2chr2"})

(def website-base
     "The base where this website is found."
     "http://localhost:3000")

(defn- html-head
  "This returns a vector containing Hiccup components for a standard HTML HEAD tag
including charset, author, description etc.
TITLE is the title which will be shown for the page."
  [title]
  [:head
   [:meta {:charset "UTF-8"}]
   [:meta {:name "description" :content "Child registration website."}]
   [:meta {:name "author" :content "Patrick A Wallace pawrsccouk@googlemail.com"}]
   ;; This tells the HTML to display at the standard width of smaller devices e.g. iPad etc.
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   ;; Load our stylesheet.
   [:link {:rel "stylesheet" :type "text/css" :href (str website-base "/static/stylesheet.css")}]
   ;; Specify a favicon to show for the website.
   [:link {:rel "icon" :type "image/png" :href (str website-base "/static/icon.png")}]
   [:title (str title)]])


;; Adding a new child to the database.

(defn make-add-child-form
  "Returns a form for registering child information."
  []
  (page/html5
   (html-head "Add a child to the register.")
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


(defn parse-add-child-form
  "Extracts data from the form created in make-form and stores it to the database.
This can throw exceptions, use higher-up middleware to catch them."
  [{params :params}]
  ;; First execute the SQL
  (let [db-row-data {:childname  (params :childname)
		     :parentname (params :yourname)
		     :address    (params :address)}]
    ;;(pprint db-row-data)
    (jdbc/insert! dbinfo :children db-row-data)
    ;; Return a notification that the operation succeeded.
    (page/html5
     (html-head "Child added")
     [:h1 "Child added"]
     [:p "Your child has been added to the database."]
     [:p "Please click on the link below to return."]
     [:a {:href (util/url website-base "/children/")} "All children"])))


(def add-child-handler
     "A Ring middleware handler which requests a form to add child data
or parses the results of said form to update the database."
     (wrap-params				; puts parameters in :form-params or :query-params
      (wrap-keyword-params			; as clojure keywords, not strings.
       (fn [header]
	 ;;(pprint header)
	 (content-type
	  (response
	    ;; GET is a request for the form itself, POST is the completed form.
	    (if (= (:request-method header) :post)
	      (parse-add-child-form header)
	      (make-add-child-form)))
	  "text/html; charset=UTF-8")))))

;; Showing the children in the database

(defn get-all-children
  "Returns all the children in the database as a vector of Hiccup codes."
  []
  (let [to-hiccup (fn [rsmap]
		   `[ :tr
		     [ :td ~(:childname rsmap)]
		     [ :td ~(:parentname rsmap)]
		     [ :td ~(:address rsmap)]])]
     (jdbc/query dbinfo
		 ["select * from children"]
		 { :row-fn to-hiccup })))




(defn show-children-handler
  "A function which returns a page showing all the children in the database."
  [header]
  (content-type
   (response
    (page/html5 (html-head "Children")
		[:h1 "Children"]
		[:p "Here are all the children we have registered."]
		[:table
		 [:tr [:th "Child's Name"] [:th "Parent's Name"] [:th "Address"]]
		 (get-all-children)]
		[:p
		 [:a {:href (util/url "add/")} "Add a child"]]))
    "text/html; charset=UTF-8"))


;; Not implemented yet.

(defn- not-implemented-handler
  "A function to return some HTML indicating a not-implemented page."
  [header]
  (content-type
   (response
    (page/html5 [:div [:title "Not implemented."] "This is not implemented yet."]))
   "text/html; charset=UTF-8"))


(def remove-child-handler
     "A Ring handler function to handle deleting a child from the database.
Currently not implemented."
     not-implemented-handler)

(def edit-child-handler
     "A Ring handler function to handle deleting a child from the database.
Currently not implemented."
     not-implemented-handler)

