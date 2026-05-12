(ns slate.components.layout
  (:require [re-frame.core :as rf]))

(defn header []
  [:header.bg-slate-800.text-white.shadow-lg
   [:div.max-w-7xl.mx-auto.px-4.py-6
    [:div.flex.items-center.justify-between
     [:div.flex.items-center.space-x-3
      [:div.text-3xl.font-bold "📺"]
      [:h1.text-2xl.font-bold "Slate"]]
     [:nav.flex.items-center.space-x-4
      [:button.px-4.py-2.rounded.hover:bg-slate-700.transition
       "Dashboard"]
      [:button.px-4.py-2.rounded.hover:bg-slate-700.transition
       "Media"]
      [:button.px-4.py-2.rounded.hover:bg-slate-700.transition
       "Channels"]]]]])

(defn sidebar []
  [:aside.bg-slate-700.text-white.w-64.min-h-screen.p-6
   [:nav.space-y-4
    [:h2.text-lg.font-semibold.mb-4 "Navigation"]
    [:button.block.w-full.text-left.px-4.py-2.rounded.hover:bg-slate-600.transition
     "📊 Dashboard"]
    [:button.block.w-full.text-left.px-4.py-2.rounded.hover:bg-slate-600.transition
     "🎬 Media Browser"]
    [:button.block.w-full.text-left.px-4.py-2.rounded.hover:bg-slate-600.transition
     "📡 Channels"]
    [:button.block.w-full.text-left.px-4.py-2.rounded.hover:bg-slate-600.transition
     "⚙️ Settings"]]])

(defn footer []
  [:footer.bg-slate-800.text-slate-400.text-center.py-4.mt-auto.border-t.border-slate-700
   [:p.text-sm
    "Slate v0.1.0 · "
    [:a.text-blue-400.hover:text-blue-300 {:href "#"} "GitHub"]
    " · "
    [:a.text-blue-400.hover:text-blue-300 {:href "#"} "Docs"]]])

(defn main-layout [content]
  [:div.flex.flex-col.min-h-screen.bg-slate-900.text-slate-100
   [header]
   [:div.flex.flex-1
    [sidebar]
    [:main.flex-1.p-8
     content]]
   [footer]])

(defn error-banner [error]
  (when error
    [:div.bg-red-900.text-red-100.p-4.rounded.mb-4.flex.items-center.space-x-3
     [:span "⚠️"]
     [:div.flex-1 error]
     [:button.text-red-100.hover:text-red-200
      {:on-click #(rf/dispatch [:api/clear-error])}
      "✕"]]))
