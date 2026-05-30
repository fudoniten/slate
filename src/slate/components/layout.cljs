(ns slate.components.layout
  (:require [re-frame.core :as rf]))

(defn- nav-btn [label page current-page]
  [:button.px-4.py-2.rounded.transition
   {:class    (if (= page current-page)
                "bg-slate-600 text-white"
                "hover:bg-slate-700 text-slate-300 hover:text-white")
    :on-click #(rf/dispatch [:nav/goto page])}
   label])

(defn header []
  (let [current @(rf/subscribe [:nav/current-page])]
    [:header.bg-slate-800.text-white.shadow-lg
     [:div.max-w-7xl.mx-auto.px-4.py-4
      [:div.flex.items-center.justify-between
       [:div.flex.items-center.space-x-3
        [:div.text-3xl.font-bold "📺"]
        [:h1.text-2xl.font-bold "Slate"]]
       [:nav.flex.items-center.space-x-2
        [nav-btn "Dashboard" :dashboard current]
        [nav-btn "Media" :media current]
        [nav-btn "Channels" :channels current]]]]]))

(defn- sidebar-btn [label page current-page]
  [:button.block.w-full.text-left.px-4.py-2.rounded.transition
   {:class    (if (= page current-page)
                "bg-slate-600 text-white font-medium"
                "text-slate-300 hover:bg-slate-600 hover:text-white")
    :on-click #(rf/dispatch [:nav/goto page])}
   label])

(defn sidebar []
  (let [current @(rf/subscribe [:nav/current-page])]
    [:aside.bg-slate-700.text-white.w-56.min-h-screen.p-4
     [:nav.space-y-1
      [:h2.text-xs.font-semibold.uppercase.tracking-wider.text-slate-400.px-4.py-2 "Navigation"]
      [sidebar-btn "📊 Dashboard" :dashboard current]
      [sidebar-btn "🎬 Media Browser" :media current]
      [sidebar-btn "📡 Channels" :channels current]
      [sidebar-btn "⚙️ Settings" :settings current]]]))

(defn footer []
  [:footer.bg-slate-800.text-slate-400.text-center.py-3.mt-auto.border-t.border-slate-700
   [:p.text-sm
    "Slate v0.1.0 · "
    [:a.text-blue-400.hover:text-blue-300 {:href "#"} "GitHub"]
    " · "
    [:a.text-blue-400.hover:text-blue-300 {:href "#"} "Docs"]]])

(defn error-banner [error]
  (when error
    [:div.bg-red-900.text-red-100.px-4.py-3.rounded.mb-4.flex.items-center.space-x-3
     [:span "⚠️"]
     [:div.flex-1 error]
     [:button.text-red-100.hover:text-white.ml-4
      {:on-click #(rf/dispatch [:api/clear-error])}
      "✕"]]))

(defn main-layout [content]
  [:div.flex.flex-col.min-h-screen.bg-slate-900.text-slate-100
   [header]
   [:div.flex.flex-1
    [sidebar]
    [:main.flex-1.p-6
     content]]
   [footer]])
