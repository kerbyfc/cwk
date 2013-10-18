(defproject cwk "0.1.0-SNAPSHOT"
  :description "evals all source code from concrete inner-resource folder"
  :url "https://github.com/kerbyfc/cwk"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :deploy-repositories [["internal" "/Users/kerbyfc/Dropbox/clojure_rep"]]
  :main cwk.core
  :profiles {:uberjar {:aot [cwk.core]}})
