(ns slate.events
  (:require [re-frame.core :as rf]
            [slate.db :as db]
            [slate.api.client :as client]))

;; Initialize app state
(rf/reg-event-db
 :app/initialize
 (fn [_ _]
   db/default-db))

;; Navigation events
(rf/reg-event-db
 :nav/goto
 (fn [db [_ page]]
   (assoc db :current-page page)))

;; Dashboard events
(rf/reg-event-db
 :dashboard/set-stats
 (fn [db [_ stats]]
   (assoc db :dashboard/stats stats)))

(rf/reg-event-db
 :dashboard/set-loading
 (fn [db [_ loading?]]
   (assoc db :dashboard/loading? loading?)))

;; Media browser events
(rf/reg-event-db
 :media/set-items
 (fn [db [_ items]]
   (assoc db :media/items items)))

(rf/reg-event-db
 :media/set-filter
 (fn [db [_ filter-str]]
   (assoc db :media/filter filter-str)))

(rf/reg-event-db
 :media/select
 (fn [db [_ item-id]]
   (assoc db :media/selected-id item-id)))

(rf/reg-event-db
 :media/set-view
 (fn [db [_ view]]
   (assoc db :media/view view)))

;; API events
(rf/reg-event-db
 :api/set-error
 (fn [db [_ error]]
   (assoc db :api/error error)))

(rf/reg-event-db
 :api/clear-error
 (fn [db _]
   (dissoc db :api/error)))

(rf/reg-event-db
 :api/fetch-failed
 (fn [db [_ response]]
   (assoc db :api/error (str "API request failed: " (or (get-in response [:response :message])
                                                        (:status response)
                                                        "unknown error")))))

;; Fetch effects — trigger HTTP calls
(rf/reg-event-fx
 :api/fetch-channels
 (fn [_ _]
   {:http-xhrio (client/get-channels)}))

(rf/reg-event-fx
 :api/fetch-programs
 (fn [_ _]
   {:http-xhrio (client/get-programs)}))

(rf/reg-event-fx
 :api/fetch-library
 (fn [_ _]
   {:http-xhrio (client/get-library)}))

(rf/reg-event-fx
 :api/fetch-all
 (fn [{:keys [db]} _]
   {:db (assoc db :dashboard/loading? true)
    :dispatch-n [[:api/fetch-channels]
                 [:api/fetch-programs]
                 [:api/fetch-library]]}))

;; API response handlers
(rf/reg-event-db
 :tunarr/channels-received
 (fn [db [_ response]]
   (let [channels (if (sequential? response) response (or (get response "data") []))
         count    (count channels)]
     (-> db
         (assoc-in [:dashboard/stats :total-channels] count)
         (assoc :dashboard/loading? false)))))

(rf/reg-event-db
 :tunarr/programs-received
 (fn [db [_ response]]
   (let [programs (if (sequential? response) response (or (get response "data") []))
         count    (count programs)]
     (-> db
         (assoc-in [:dashboard/stats :total-programs] count)
         (assoc :dashboard/loading? false)))))

(rf/reg-event-db
 :pseudovision/library-received
 (fn [db [_ response]]
   (let [items (if (sequential? response) response (or (get response "data") []))
         count (count items)]
     (-> db
         (assoc-in [:dashboard/stats :total-library-items] count)
         (assoc :media/items items)
         (assoc :dashboard/loading? false)))))
