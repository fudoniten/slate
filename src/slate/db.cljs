(ns slate.db)

(def default-db
  {:current-page :dashboard

   :dashboard/stats {:total-channels 0
                     :total-programs 0
                     :total-library-items 0
                     :status :idle}
   :dashboard/loading? false

   :media/items []
   :media/filter ""
   :media/selected-id nil

   :api/error nil})
