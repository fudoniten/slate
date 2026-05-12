(ns slate.events
  (:require [re-frame.core :as rf]
            [slate.db :as db]))

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

;; API events
(rf/reg-event-db
 :api/set-error
 (fn [db [_ error]]
   (assoc db :api/error error)))

(rf/reg-event-db
 :api/clear-error
 (fn [db _]
   (dissoc db :api/error)))
