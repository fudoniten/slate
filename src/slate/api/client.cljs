(ns slate.api.client
  (:require [re-frame.core :as rf]
            [re-frame-http-fx.http-fx]))

;; Tunarr Scheduler API client

(def tunarr-base-url
  (or (js/process.env.TUNARR_API_URL) "http://localhost:8000"))

(def pseudovision-base-url
  (or (js/process.env.PSEUDOVISION_API_URL) "http://localhost:9000"))

;; Generic HTTP GET request
(defn http-get [url on-success on-failure]
  {:method :get
   :uri url
   :timeout 5000
   :on-success on-success
   :on-failure on-failure})

;; Generic HTTP POST request
(defn http-post [url data on-success on-failure]
  {:method :post
   :uri url
   :json-params data
   :timeout 5000
   :on-success on-success
   :on-failure on-failure})

;; Tunarr API endpoints

(defn get-channels []
  (http-get
   (str tunarr-base-url "/api/v1/channels")
   #(rf/dispatch [:tunarr/channels-received %])
   #(rf/dispatch [:api/set-error (str "Failed to fetch channels: " %)])))

(defn get-programs []
  (http-get
   (str tunarr-base-url "/api/v1/programs")
   #(rf/dispatch [:tunarr/programs-received %])
   #(rf/dispatch [:api/set-error (str "Failed to fetch programs: " %)])))

(defn get-channel [channel-id]
  (http-get
   (str tunarr-base-url "/api/v1/channels/" channel-id)
   #(rf/dispatch [:tunarr/channel-received %])
   #(rf/dispatch [:api/set-error (str "Failed to fetch channel: " %)])))

;; Pseudovision API endpoints

(defn get-library []
  (http-get
   (str pseudovision-base-url "/api/v1/library")
   #(rf/dispatch [:pseudovision/library-received %])
   #(rf/dispatch [:api/set-error (str "Failed to fetch library: " %)])))

(defn get-media [media-id]
  (http-get
   (str pseudovision-base-url "/api/v1/media/" media-id)
   #(rf/dispatch [:pseudovision/media-received %])
   #(rf/dispatch [:api/set-error (str "Failed to fetch media: " %)])))

(defn search-media [query]
  (http-get
   (str pseudovision-base-url "/api/v1/search?q=" (js/encodeURIComponent query))
   #(rf/dispatch [:pseudovision/search-results-received %])
   #(rf/dispatch [:api/set-error (str "Failed to search media: " %)])))

;; Health check
(defn health-check []
  (http-get
   "/health"
   #(rf/dispatch [:app/health-ok %])
   #(rf/dispatch [:app/health-error %])))
