(ns register.dispatch
  (:use ring.adapter.jetty
	ring.util.response
	ring.middleware.resource
        ring.middleware.content-type
        ring.middleware.not-modified
	ring.middleware.basic-authentication
	ring.handler.dump)
  (:require (java)
	    (clojure [string :as string]))
  (:gen-class))

;; I want to make a dispatch-handling wrapper which will test the URI and will call a different function depending
;; on what is there.

;; Usage
(comment
  (let [dispatching-wrapper (dispatch not-found-handler
				      "/add/" add-handler
				      "/remove/" remove-handler)]
    ;; do something with dispatching-wrapper
    ))
;;
;; This returns a ring middleware wrapper which checks for strings on the URI entry in the request map
;; and calls handlers if the strings match.
;; If no string matches, it calls not-found-handler, which will probably return a 404.




(defmacro dispatch
  "This takes a default function and a sequence of string, function params.
It returns a handler function which checks the URI of the request to see if it
matches one of the substring sequences. If so, the associated function is called.
If none match, the default function is called."
  [defaultfn & args]
  (let [pairs (partition 2 args)  ; the list of (string, function) args
	urisym (gensym "uri")
	headersym (gensym "header")]
    `(fn dispatch-handler [~headersym]
       (let [~urisym (:uri ~headersym)]
	 ;; Convert the paired arguments into a list of ((.startsWith URI STRING) (FUNCTION HEADER)) pairs
	 ;; then remove the outer list with (apply concat...) and pass the rest into the (cond) macro.
	 (cond ~@(apply concat (map (fn [[s f]] `((.startsWith ~urisym ~s) (~f ~headersym))) pairs))
	       true (~defaultfn ~headersym))))))







