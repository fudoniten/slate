(ns slate.components.settings)

(defn settings []
  [:div.space-y-4
   [:div
    [:h1.text-3xl.font-bold "Settings"]
    [:p.text-slate-400.mt-1 "Configure your Slate instance"]]
   [:div.bg-slate-800.rounded-lg.p-8.border.border-slate-700.text-center
    [:p.text-5xl.mb-4 "⚙️"]
    [:p.text-slate-300.font-medium "Settings coming soon"]
    [:p.text-slate-500.text-sm.mt-2 "Configuration options will appear here."]]])
