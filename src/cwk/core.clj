;; cwk.core builds rest api to communicate with application (web interface etc.)
;;
;; It uses:
;;
;;  - [ring](https://github.com/ring-clojure/ring) with jetty server
;;  - [compojure](https://github.com/weavejester/compojure)
;;  - [liberator](http://clojure-liberator.github.io/liberator)
;;
(ns cwk.core
  (:gen-class)
  (:require ring.adapter.jetty
            ring.middleware.params
            compojure.core
            compojure.route
            liberator.core
            liberator.dev))

;; stores compojure routes,
;; created by res-handler
;; to involve them in cwk.core/api
(def routes-map (ref {}))

(defn fname
  "Returns alpha-numeric name of the function

    (fname string?)
    ; string

  "
  [f]
  (subs (first (re-find #"(\$)[A-Za-z0-9]*" (.toString f))) 1))

(defn ensure-argument
  "Throws an exception if given variable didn't passed type checks.

    (ensure-argument \"seventh\" #(string? %))
    ; nil

    (ensure-argument 1 string? non-string-arg)
    ; IllegalArgumentException Please, pass string as first argument.

    (ensure-argument 6 #(or (seq? %) (symbol? %) 123 \"sequence or symbol\")
    ; IllegalArgumentException Please, pass sequence or symbol as sixth argument.

  "
  [pos checker-function given-var & expected]
  (let [stance ["first" "second" "third" "forth" "fifth" "sixth"]]
    (if (not (checker-function given-var))
      (throw (IllegalArgumentException.
							(str "Please, pass "
									 (or (first expected) (fname checker-function))
									 " as "
									 (or (get stance pos) pos)
									 " argument."))))))

(defmacro defres
  "Creates liberator resource, create compojure route,
  bind resource to it and merge it to routes-map.

    (defres \"/posts/:id\" [id] {
      :handle-ok \"Posts!\"})

    ; @cwk.core/routes-map {:posts/id #<core$if_method...>}

  "
  [route args & kvs]
  `(dosync
    (ref-set
		 cwk.core/routes-map
     (merge
      @cwk.core/routes-map
      {~(keyword (clojure.string/replace (if (vector? route) (first route) route) #"(/:)([\w]*)" "/$2"))
       (compojure.core/ANY
        (str "/" ~route)
        ~args
        (fn
          [request#]
					(liberator.core/run-resource request# ~@kvs)))}))))

(defmacro defresources
  "Creates resources by res-handler.
  It merges common resource handlers and route with
	appropriate options of each resource.

    (defresources \"posts\"
      {:allowed-methods [:get :put :post]}
      \"/\" [] {
        :handle-ok \"Posts\"
      }
      \"/:id\" [id] {
        :handle-ok (=> ctx
                      (str \"Post with id \" id))
      })

    ; @cwk.core/routes-map {:posts/id #<core$if_method...>, :posts #<co...>}

  "
  [& form]
  (let [[root common route args res & nxt] (vec form)
        factory `(defres ~(str root route) ~args (merge ~common ~res))]
    (ensure-argument 1 string? root)
    (cond (nil? nxt) factory
          :else `(do
                   (defresources ~root ~common ~@nxt)
                   ~factory))))

(defmacro =>
  "Creates liberator handler, binds first argument to liberator
	context and allows to create context bindings.
	See [execution model](http://clojure-liberator.github.io/liberator/doc/execution-model.html).

    ...
    :handle-ok (=> ctx :representation :media-type mt :request :query-params \"name\" name
                  (str \"Greetings, \" name \". Request media-type is \" mt))

    ; curl http://......?name=Rick
    ; Greetings, Rick. Request media-type is text/plain

  "
  [ctx & kvs]
  (let
		[bindings (vec (map-indexed
										#(if (even? %1) (vec %2) (first %2))
										(partition-by #(not (symbol? %))
																	(remove seq? (pop (vec kvs))))))
        vars (vec (keep-indexed
							 #(if (odd? %1) %2)
									 bindings))

        values (vec	(keep-indexed
								 #(if (even? %1) %2)
										 bindings))]

    (if (and (> (count values) 0) (= (count values) (count vars)))
      `(fn [$#] (apply (fn [~ctx ~@vars] ~(last kvs))
											 (apply conj [$#] (vec (map #(get-in $# %) ~values)))))
      `(fn [$#] (apply (fn [~ctx] ~(last kvs)) [$#]))
      )
    ))

(defn make-handler
  "Return compojure routes handler from vals of cwk.core/routes map.
  Adds static resources serving."
  []
  (dosync
   (ref-set cwk.core/routes-map
           (merge @cwk.core/routes-map {:staticres (compojure.route/resources "/")})))

  (def cwk.core/routes (apply compojure.core/routes
                              'cwk.core/routes
                              (vals @cwk.core/routes-map)))
  'cwk.core/routes)

(defmacro wrapped-handler
	"Wraps cwk.core/make-handler results.

		(def handler (wrapped-handler ->
                ring.middleware.params/wrap-params
                (liberator.dev/wrap-trace :header :ui)))

	"
  [& wrappers]
  `(-> ~(make-handler)
       ~@wrappers))

(defn run
	"Run jetty with given handler and options

		(cwk.core/run handler {:port 3000})

	"
  [handler options]
  (ring.adapter.jetty/run-jetty handler options))


