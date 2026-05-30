(ns slate.components.media-browser
  (:require [re-frame.core :as rf]))

(defn media-item [item]
  [:div.bg-slate-800.rounded-lg.overflow-hidden.hover:shadow-lg.hover:border-slate-500.border.border-slate-700.transition.cursor-pointer
   {:on-click #(rf/dispatch [:media/select (:id item)])}
   [:div.bg-gradient-to-br.from-slate-700.to-slate-800.h-32.flex.items-center.justify-center
    [:span.text-4xl "🎬"]]
   [:div.p-3
    [:h4.font-semibold.text-white.truncate (:title item)]
    [:p.text-sm.text-slate-400 (:type item)]
    [:p.text-xs.text-slate-500.mt-1.truncate (:id item)]]])

(defn media-list-item [item]
  [:div.bg-slate-800.rounded.px-4.py-3.flex.items-center.gap-4.hover:bg-slate-700.transition.cursor-pointer.border.border-slate-700.hover:border-slate-500
   {:on-click #(rf/dispatch [:media/select (:id item)])}
   [:span.text-2xl "🎬"]
   [:div.flex-1.min-w-0
    [:p.font-medium.text-white.truncate (:title item)]
    [:p.text-sm.text-slate-400 (:type item)]]
   [:p.text-xs.text-slate-500.shrink-0 (:id item)]])

(defn media-grid [items]
  [:div.grid.grid-cols-2.sm:grid-cols-3.lg:grid-cols-4.xl:grid-cols-5.gap-4
   (if (empty? items)
     [:div.col-span-full.text-center.py-16
      [:p.text-slate-500.text-4xl.mb-3 "🎞️"]
      [:p.text-slate-400 "No media items found"]]
     (for [item items]
       ^{:key (:id item)} [media-item item]))])

(defn media-list [items]
  [:div.space-y-2
   (if (empty? items)
     [:div.text-center.py-16
      [:p.text-slate-500.text-4xl.mb-3 "🎞️"]
      [:p.text-slate-400 "No media items found"]]
     (for [item items]
       ^{:key (:id item)} [media-list-item item]))])

(defn search-bar []
  (let [filter-text @(rf/subscribe [:media/filter])]
    [:div.flex.gap-2
     [:input.flex-1.bg-slate-800.border.border-slate-700.rounded.px-4.py-2.text-white.placeholder-slate-500.focus:outline-none.focus:border-blue-500
      {:type        "text"
       :placeholder "Search media…"
       :value       filter-text
       :on-change   #(rf/dispatch [:media/set-filter (.. % -target -value)])}]
     (when-not (empty? filter-text)
       [:button.bg-slate-700.hover:bg-slate-600.text-slate-300.px-3.py-2.rounded.transition.text-sm
        {:on-click #(rf/dispatch [:media/set-filter ""])}
        "✕"])]))

(defn view-toggle []
  (let [view @(rf/subscribe [:media/view])]
    [:div.flex.gap-1
     [:button.px-3.py-1.rounded.text-sm.transition
      {:class    (if (= view :grid)
                   "bg-blue-600 text-white"
                   "bg-slate-800 border border-slate-700 text-slate-300 hover:bg-slate-700")
       :on-click #(rf/dispatch [:media/set-view :grid])}
      "Grid"]
     [:button.px-3.py-1.rounded.text-sm.transition
      {:class    (if (= view :list)
                   "bg-blue-600 text-white"
                   "bg-slate-800 border border-slate-700 text-slate-300 hover:bg-slate-700")
       :on-click #(rf/dispatch [:media/set-view :list])}
      "List"]]))

(defn media-browser []
  (let [items @(rf/subscribe [:media/filtered-items])
        view  @(rf/subscribe [:media/view])]
    [:div.space-y-4
     [:div
      [:h1.text-3xl.font-bold "Media Browser"]
      [:p.text-slate-400.mt-1 "Browse and manage media in your library"]]

     [search-bar]

     [:div.flex.items-center.justify-between
      [:p.text-sm.text-slate-400 (str (count items) " items")]
      [view-toggle]]

     (if (= view :grid)
       [media-grid items]
       [media-list items])]))
