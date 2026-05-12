# Getting Started with Slate

**Slate** is a Kubernetes-ready ClojureScript UI service for managing your Pseudovision/Tunarr Scheduler ecosystem.

## 🚀 Quick Start (5 minutes)

### 1. Clone to `fudohermes` (GitHub)

```bash
# Set up on GitHub first, then:
git clone https://github.com/fudohermes/slate.git
cd slate
```

### 2. Install & Run Locally

```bash
# Enter dev environment
nix flake update
nix develop

# Install dependencies
npm install

# Start dev server with hot reload
npm run watch
```

Visit **http://localhost:3000** — you should see the Slate dashboard with a dark theme.

### 3. Check What's Running

- **Frontend**: ClojureScript compiled to JS, served by Express
- **API client**: Configured to call Tunarr (`localhost:8000`) and Pseudovision (`localhost:9000`)
- **Re-frame events**: Watching for navigation, API responses, errors
- **Tailwind CSS**: All styling via utility classes

## 📁 Project Structure at a Glance

```
src/slate/
├── app.cljs              # React root, entry point
├── db.cljs               # Re-frame database (initial state)
├── events.cljs           # Event handlers (user actions)
├── subs.cljs             # Subscriptions (data queries)
├── api/client.cljs       # API client scaffolding
├── components/
│   ├── layout.cljs       # Header, sidebar, footer
│   ├── dashboard.cljs    # Dashboard view
│   └── media_browser.cljs # Media browser view
└── views/root.cljs       # Root router component
```

## 🔌 Integrating APIs (Next Steps)

The API client is ready for real integration. Currently stubbed:

### Tunarr Scheduler
- `/api/v1/channels` — get all channels
- `/api/v1/programs` — get all programs
- `/api/v1/channels/{id}` — get single channel

### Pseudovision
- `/api/v1/library` — get media library
- `/api/v1/media/{id}` — get media details
- `/api/v1/search?q=...` — search media

**To integrate:**
1. Add event handlers in `src/slate/events.cljs` for API responses
2. Update subscriptions in `src/slate/subs.cljs` to expose data to components
3. Call API functions from components or use re-frame effects

Example in `src/slate/events.cljs`:
```clojure
(rf/reg-event-db
  :dashboard/load-stats
  (fn [db _]
    ;; Dispatch API calls
    (rf/dispatch [:http/get-tunarr-channels])
    (rf/dispatch [:http/get-pseudovision-library])
    db))
```

## 🐳 Docker & Kubernetes

### Build Docker image
```bash
npm run docker:build   # Builds image: slate:latest
npm run docker:run     # Runs locally on port 3000
```

### Deploy to Kubernetes
```bash
kubectl apply -f k8s/deployment.yaml    # Deployment + service
kubectl apply -f k8s/network-policy.yaml # Network policies
kubectl apply -f k8s/hpa.yaml            # Auto-scaling
```

Check status:
```bash
kubectl -n slate get pods
kubectl -n slate describe service slate
```

## 🎨 Styling & Components

All styling uses **Tailwind CSS**. Edit `src/slate/components/` to see examples:

```clojure
[:div.flex.gap-4.p-6.bg-slate-900
 [:h1.text-2xl.font-bold.text-white "Dashboard"]
 [:p.text-slate-400 "Welcome to Slate"]]
```

Add new components by creating new `.cljs` files in `src/slate/components/`.

## 🔧 Development Workflow

### Hot reload
ClojureScript watches `src/` and recompiles on save. Browser auto-refreshes.

### REPL
```bash
npm run cljs:repl  # Opens ClojureScript REPL connected to browser
```

### Tailwind IntelliSense
If using VS Code, install **Tailwind CSS IntelliSense** extension for autocomplete.

## 📊 Re-frame Architecture

**Data flow:**
```
Component calls (rf/dispatch [:event-name data])
         ↓
Event handler processes (db change)
         ↓
Subscription notified (Component re-renders)
```

**Example: Dashboard card clicks**
```clojure
;; Event: update selected media
(rf/reg-event-db
  :media/select
  (fn [db [_ media-id]]
    (assoc db :selected-media-id media-id)))

;; Subscription: get selected media
(rf/reg-sub
  :media/selected
  (fn [db _]
    (get db :selected-media-id)))

;; Component uses it
(defn media-card [media]
  [:div
   {:on-click #(rf/dispatch [:media/select (:id media)])}
   (:name media)])
```

## 🚨 Troubleshooting

### Port 3000 already in use
```bash
lsof -i :3000  # Find process
kill -9 <PID>  # Kill it
npm run watch  # Restart
```

### ClojureScript won't compile
```bash
rm -rf .shadow-cljs node_modules
npm install
npm run watch
```

### API calls return 404
Check that Tunarr (`localhost:8000`) and Pseudovision (`localhost:9000`) are running:
```bash
curl http://localhost:8000/api/version
curl http://localhost:9000/api/version
```

If they're on different hosts, update `src/slate/api/client.cljs`:
```clojure
(def tunarr-base-url "http://tunarr.your-cluster:8000")
(def pseudovision-base-url "http://pseudovision.your-cluster:9000")
```

## 📖 Learning Resources

- **Reagent**: https://reagent-project.github.io/
- **Re-frame**: https://day8.github.io/re-frame/
- **Tailwind**: https://tailwindcss.com/docs
- **Shadow CLJS**: https://shadow-cljs.github.io/user-guide/index.html

## 🤝 Next Steps

1. **Integrate Tunarr API** — Fetch channels, programs, display in UI
2. **Integrate Pseudovision API** — Fetch media library, search, tagging
3. **Add job tracking** — Display async job status (retag, sync, etc.)
4. **Add Tunabrain config** — UI for toggling special flags, cost settings
5. **Deploy to cluster** — Push Kubernetes manifests to your namespace

## 📝 Notes

- API client uses `re-frame-http-fx` for HTTP requests
- All API calls go through Re-frame events for centralized error handling
- Error banner component shows any API errors to the user
- Dark theme enabled by default (Tailwind dark: prefix)
- Production builds use Nix flake + Docker

---

**Happy coding! 🎉**
