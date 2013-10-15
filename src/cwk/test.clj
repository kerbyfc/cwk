(ns cwk.test
  (use cwk.core))

(defn greeting [x] (println "Hello" x "in" *ns*) x)

(def once-only (memoize (fn [] (println "Called!"))))

(memoize (fn [] (println "Called!")))

(macroexpand `(reval 'mkmsxc "api" (println "OOOOOOOOOO")))

(reval 'mkmss "api" (use 'cwk.test) (println "lol"))
