(ns slate.components.channels
  (:require [re-frame.core :as rf]))

(defn channels []
  [:div.space-y-4
   [:div
    [:h1.text-3xl.font-bold "Channels"]
    [:p.text-slate-400.mt-1 "View and manage your Tunarr channels"]]
   [:div.bg-slate-800.rounded-lg.p-8.border.border-slate-700.text-center
    [:p.text-5xl.mb-4 "📡"]
    [:p.text-slate-300.font-medium "No channels configured"]
    [:p.text-slate-500.text-sm.mt-2 "Connect a Tunarr instance and click Refresh to load channels."]
    [:button.mt-4.bg-blue-600.hover:bg-blue-500.text-white.font-medium.py-2.px-6.rounded.transition
     {:on-click #(rf/dispatch [:api/fetch-channels])}
     "Fetch Channels"]]])
