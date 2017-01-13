(defproject register "0.1.0-SNAPSHOT"
  :description "An example registration webproject to take names and addresses for a database."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
					; Ring web development system.
		 [ring/ring-core "1.5.0"]
		 [ring/ring-jetty-adapter "1.5.0"]
		 [ring/ring-devel "1.5.0"]
		 [ring-basic-authentication "1.0.5"]
					; MySQL JDBC layer
		 [mysql/mysql-connector-java "6.0.5"]
		 [org.clojure/java.jdbc "0.7.0-alpha1"]
					; HTML generator
		 [hiccup "1.0.5"]
					; Password Encryption
		 [crypto-password "0.2.0"]]
  :main ^:skip-aot register.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
