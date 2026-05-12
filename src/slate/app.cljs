(ns slate.app
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [slate.events]
            [slate.subs]
            [slate.views.root :as root]))

(defn mount-root []
  (rdom/render [root/root] (js/document.getElementById "app")))

(defn init []
  (rf/dispatch-sync [:app/initialize])
  (mount-root))

(init)
