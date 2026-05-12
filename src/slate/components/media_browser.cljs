(ns slate.components.media-browser
  (:require [re-frame.core :as rf]))

(defn media-item [item]
  [:div.bg-slate-800.rounded-lg.overflow-hidden.hover:shadow-lg.transition.cursor-pointer
   [:div.bg-gradient-to-br.from-slate-700.to-slate-800.h-32.flex.items-center.justify-center
    [:span.text-4xl "🎬"]]
   [:div.p-4
    [:h4.font-semibold.text-white.truncate (:title item)]
    [:p.text-sm.text-slate-400 (:type item)]
    [:p.text-xs.text-slate-500.mt-2 (:id item)]]])

(defn media-grid [items]
  [:div.grid.grid-cols-1.sm:grid-cols-2.md:grid-cols-3.lg:grid-cols-4.gap-4
   (if (empty? items)
     [:div.col-span-full.text-center.py-12
      [:p.text-slate-400 "No media items found"]]
     (for [item items]
       ^{:key (:id item)} [media-item item]))])

(defn search-bar []
  (let [filter-text @(rf/subscribe [:media/filter])]
    [:div.flex.gap-2.mb-6
     [:input.flex-1.bg-slate-800.border.border-slate-700.rounded.px-4.py-2.text-white.placeholder-slate-500
      {:type "text"
       :placeholder "Search media..."
       :value filter-text
       :on-change #(rf/dispatch [:media/set-filter (.. % -target -value)])}]
     [:button.bg-blue-600.hover:bg-blue-700.text-white.font-medium.px-6.py-2.rounded.transition
      "Search"]]))

(defn media-browser []
  (let [items @(rf/subscribe [:media/items])]
    [:div.space-y-6
     [:div
      [:h1.text-4xl.font-bold.mb-2 "Media Browser"]
      [:p.text-slate-400 "Browse and manage media in your library"]]

     [search-bar]

     [:div.space-y-4
      [:div.flex.items-center.justify-between
       [:p.text-sm.text-slate-400 (str (count items) " items")]
       [:div.flex.gap-2
        [:button.px-3.py-1.bg-slate-800.border.border-slate-700.rounded.text-sm.hover:bg-slate-700.transition
         "Grid"]
        [:button.px-3.py-1.bg-slate-800.border.border-slate-700.rounded.text-sm.hover:bg-slate-700.transition
         "List"]]]]

     [media-grid items]]))
