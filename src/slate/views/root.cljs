(ns slate.views.root
  (:require [re-frame.core :as rf]
            [slate.components.layout :as layout]
            [slate.components.dashboard :as dashboard]
            [slate.components.media-browser :as media-browser]
            [slate.components.channels :as channels]
            [slate.components.settings :as settings]))

(defn root []
  (let [page  @(rf/subscribe [:nav/current-page])
        error @(rf/subscribe [:api/error])]
    [layout/main-layout
     [:div
      [layout/error-banner error]
      (case page
        :dashboard [dashboard/dashboard]
        :media     [media-browser/media-browser]
        :channels  [channels/channels]
        :settings  [settings/settings]
        [dashboard/dashboard])]]))
