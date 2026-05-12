(ns slate.views.root
  (:require [re-frame.core :as rf]
            [slate.components.layout :as layout]
            [slate.components.dashboard :as dashboard]
            [slate.components.media-browser :as media-browser]))

(defn root []
  (let [page @(rf/subscribe [:nav/current-page])
        error @(rf/subscribe [:api/error])]
    [:div
     (when error
       [layout/error-banner error])
     [layout/main-layout
      (case page
        :dashboard [dashboard/dashboard]
        :media [media-browser/media-browser]
        :dashboard)]]))
