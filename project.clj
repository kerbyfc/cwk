(defproject cwk "0.1.4-SNAPSHOT"
  :description "small clojure web kit, that works around liberator and compojure"
  :url "https://github.com/kerbyfc/cwk"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [compojure "1.1.5"]
                 [liberator "0.9.0"]]
  :deploy-repositories [["internal" "/Users/kerbyfc/Dropbox/clojure_rep"]]
  :profiles {:uberjar {:aot :all}})
