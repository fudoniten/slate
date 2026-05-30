(ns slate.api.client
  (:require [re-frame-http-fx.http-fx]))

;; Side-effectful require registers the :http-xhrio effect handler.

(def tunarr-base-url "")      ;; proxied through server.js
(def pseudovision-base-url "") ;; proxied through server.js

(defn- get-request [uri on-success]
  {:method          :get
   :uri             uri
   :timeout         8000
   :response-format {:read          js/JSON.parse
                     :description   "JSON"
                     :content-type  "application/json"}
   :on-success      on-success
   :on-failure      [:api/fetch-failed]})

(defn get-channels []
  (get-request (str tunarr-base-url "/api/v1/channels")
               [:tunarr/channels-received]))

(defn get-programs []
  (get-request (str tunarr-base-url "/api/v1/programs")
               [:tunarr/programs-received]))

(defn get-library []
  (get-request (str pseudovision-base-url "/api/v1/library")
               [:pseudovision/library-received]))
