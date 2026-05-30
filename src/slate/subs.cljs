(ns slate.subs
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

;; Navigation subscriptions
(rf/reg-sub
 :nav/current-page
 (fn [db]
   (:current-page db)))

;; Dashboard subscriptions
(rf/reg-sub
 :dashboard/stats
 (fn [db]
   (:dashboard/stats db)))

(rf/reg-sub
 :dashboard/loading?
 (fn [db]
   (:dashboard/loading? db)))

;; Media subscriptions
(rf/reg-sub
 :media/items
 (fn [db]
   (:media/items db)))

(rf/reg-sub
 :media/filter
 (fn [db]
   (:media/filter db)))

(rf/reg-sub
 :media/filtered-items
 :<- [:media/items]
 :<- [:media/filter]
 (fn [[items filter-text]]
   (if (str/blank? filter-text)
     items
     (let [lower (str/lower-case filter-text)]
       (filter #(str/includes? (str/lower-case (str (:title %) " " (:type %))) lower)
               items)))))

(rf/reg-sub
 :media/selected-id
 (fn [db]
   (:media/selected-id db)))

(rf/reg-sub
 :media/view
 (fn [db]
   (:media/view db)))

;; API subscriptions
(rf/reg-sub
 :api/error
 (fn [db]
   (:api/error db)))
