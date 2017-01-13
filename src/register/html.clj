(ns register.html
  "I hold support functions useful for generating HTML pages."
  (require [hiccup [form :as form] [page :as page] [util :as util]])
  (require [ring.util.response :as response])
  (require [clojure.java.jdbc :as jdbc])
  (require [register.database :as db])
  (:gen-class))

(defn- head
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
   [:link {:rel "stylesheet" :type "text/css" :href (util/url "/stylesheet.css")}]
   ;; Specify a favicon to show for the website.
   [:link {:rel "icon" :type "image/png" :href (util/url "/icon.png")}]
   [:title (str title)]])

(defmacro content-type
  "I return the standard content-type for HTML output on this site
which is HTML with charset of UTF-8"
  [body]
  `(response/content-type
    ~body
    "text/html; charset=UTF-8"))


(defn page
  "An HTML page with a standard <head> section including the title TITLE
and the contents of BODY wrapped in a <body> section.
TITLE should be a string and BODY should be arrays of hiccup keywords.

Example: (page \"Welcome\" [:p \"Welcome\"]...)"
  [title & body]
  (page/html5
   (head title)
   [:body
    body]))

(defn not-found-handler
  "I return a 404-not-found response with a custom HTML page indicating what failed."
  [{uri :uri}]
  (content-type
   (response/not-found
    (page "Page Not Found"
	  [:h1 "404: Page not found"]
	  [:p "The resource you requested [" [:i uri] "] was not available."]))))


