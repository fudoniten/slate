(ns slate.components.dashboard
  (:require [re-frame.core :as rf]))

(defn stat-card [title value icon]
  [:div.bg-slate-800.rounded-lg.p-6.shadow-lg.border.border-slate-700.hover:border-slate-600.transition
   [:div.flex.items-center.justify-between
    [:div
     [:p.text-slate-400.text-sm.font-medium title]
     [:p.text-3xl.font-bold.mt-2 value]]
    [:div.text-4xl.opacity-20 icon]]])

(defn stats-grid [stats]
  [:div.grid.grid-cols-1.md:grid-cols-2.lg:grid-cols-4.gap-6
   [stat-card "Channels" (or (:total-channels stats) 0) "📡"]
   [stat-card "Programs" (or (:total-programs stats) 0) "🎬"]
   [stat-card "Library Items" (or (:total-library-items stats) 0) "🎞️"]
   [stat-card "Status" (str (or (:status stats) "idle")) "🟢"]])

(defn recent-activity-card []
  [:div.bg-slate-800.rounded-lg.p-6.shadow-lg.border.border-slate-700
   [:h3.text-lg.font-semibold.mb-4 "Recent Activity"]
   [:div.space-y-3
    [:div.p-3.bg-slate-700.rounded
     [:p.text-sm.text-slate-300 "System initialized"]]
    [:div.p-3.bg-slate-700.rounded
     [:p.text-sm.text-slate-300 "Waiting for API connections..."]]]])

(defn quick-actions []
  [:div.bg-slate-800.rounded-lg.p-6.shadow-lg.border.border-slate-700
   [:h3.text-lg.font-semibold.mb-4 "Quick Actions"]
   [:div.space-y-2
    [:button.w-full.bg-blue-600.hover:bg-blue-700.text-white.font-medium.py-2.px-4.rounded.transition
     "Refresh All Data"]
    [:button.w-full.bg-green-600.hover:bg-green-700.text-white.font-medium.py-2.px-4.rounded.transition
     "Add Channel"]
    [:button.w-full.bg-purple-600.hover:bg-purple-700.text-white.font-medium.py-2.px-4.rounded.transition
     "Browse Media"]]])

(defn dashboard []
  (let [stats @(rf/subscribe [:dashboard/stats])
        loading? @(rf/subscribe [:dashboard/loading?])]
    [:div.space-y-6
     [:div
      [:h1.text-4xl.font-bold.mb-2 "Dashboard"]
      [:p.text-slate-400 "Welcome to Slate - Manage your Pseudovision and Tunarr ecosystem"]]

     (when loading?
       [:div.bg-slate-700.p-4.rounded.text-center
        [:p "Loading data..."]])

     [stats-grid stats]

     [:div.grid.grid-cols-1.lg:grid-cols-3.gap-6
      [:div.lg:col-span-2
       [recent-activity-card]]
      [:div
       [quick-actions]]]]))
