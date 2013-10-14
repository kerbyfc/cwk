(ns cwk.core-test
  (:use [clojure.test]
            [cwk.core]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(defn example-fixture [f]
  (reval 'cwk.core "api"
         (do
           (println "CONFIGURING " *ns* "define greeting fn")
           (defn greeting [x] (println "Greetings," x)))
         )
  (f))

(use-fixtures :once example-fixture)

(deftest a-test
  (testing ""
    (is (nil? (ns-resolve 'cwk.api 'g)))
    (is (= #'cwk.api/greeting (ns-resolve 'cwk.api 'greeting)))
    ))
