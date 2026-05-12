# Slate - ClojureScript UI Service

📺 A Kubernetes-ready ClojureScript UI service for managing Pseudovision and Tunarr Scheduler ecosystems.

## Features

- **Modern UI Stack**: ClojureScript + Reagent + Re-frame + Tailwind CSS
- **Kubernetes-Native**: Complete K8s manifests with deployments, services, HPA, and network policies
- **Scalable Architecture**: Multi-replica deployment with horizontal pod autoscaling
- **API Client Scaffolding**: Ready-to-integrate API client structure for Tunarr and Pseudovision
- **Production Ready**: Health checks, readiness probes, security contexts, resource limits
- **Development Optimized**: Shadow CLJS with hot reloading, Nix dev environment, comprehensive tooling

## Quick Start

### Development

Prerequisites: Node.js 20+, Java 17+, Clojure

```bash
nix flake update
nix develop
npm install
npm run watch
```

Visit `http://localhost:3000` in your browser.

### Production Build

```bash
npm run build
npm start
```

### Docker

```bash
npm run docker:build
npm run docker:run
```

## Project Structure

```
slate/
├── src/
│   └── slate/
│       ├── app.cljs              # Application entry point
│       ├── db.cljs               # Re-frame database schema
│       ├── events.cljs           # Re-frame event handlers
│       ├── subs.cljs             # Re-frame subscriptions
│       ├── api/
│       │   └── client.cljs       # API client for Tunarr & Pseudovision
│       ├── components/
│       │   ├── layout.cljs       # Main layout components
│       │   ├── dashboard.cljs    # Dashboard view
│       │   └── media_browser.cljs # Media browser view
│       └── views/
│           └── root.cljs         # Root view component
├── resources/
│   └── public/
│       ├── index.html            # HTML entry point
│       └── css/
│           └── styles.css        # Tailwind & custom styles
├── k8s/
│   ├── deployment.yaml           # K8s deployment, service, RBAC
│   ├── network-policy.yaml       # Network policies
│   └── hpa.yaml                  # Horizontal Pod Autoscaler
├── flake.nix                      # Nix development & build configuration
├── Dockerfile                     # Multi-stage production image
├── package.json                   # npm configuration
├── shadow-cljs.edn              # Shadow CLJS configuration
├── tailwind.config.js            # Tailwind CSS configuration
├── deps.edn                       # Clojure dependencies
└── server.js                      # Express server with API routes
```

## Configuration

### Environment Variables

```bash
NODE_ENV=production                    # Node environment
PORT=3000                             # Server port
TUNARR_API_URL=http://tunarr:8000     # Tunarr Scheduler API endpoint
PSEUDOVISION_API_URL=http://pseudovision:9000  # Pseudovision API endpoint
```

### Kubernetes Deployment

Edit `k8s/deployment.yaml` ConfigMap to configure API endpoints:

```yaml
data:
  tunarr_api_url: "http://tunarr:8000"
  pseudovision_api_url: "http://pseudovision:9000"
```

Deploy to Kubernetes:

```bash
kubectl apply -f k8s/
```

Verify deployment:

```bash
kubectl -n slate get pods
kubectl -n slate get svc
kubectl -n slate logs -f deployment/slate
```

## Development

### Shadow CLJS

Watch for changes and hot-reload:

```bash
npm run watch
```

### REPL

Connect your editor to port 7002 for interactive development.

### Testing

(Tests structure ready for implementation)

```bash
npm run test
```

## API Integration

The API client in `src/slate/api/client.cljs` provides scaffolding for:

### Tunarr Scheduler API

- `get-channels()` - Fetch all channels
- `get-programs()` - Fetch all programs
- `get-channel(channel-id)` - Fetch channel details

### Pseudovision API

- `get-library()` - Fetch media library
- `get-media(media-id)` - Fetch media details
- `search-media(query)` - Search media

Event handlers in `src/slate/events.cljs` dispatch results to the store.

## State Management

Re-frame subscriptions and events manage application state:

- `:nav/current-page` - Current page view
- `:dashboard/stats` - Dashboard statistics
- `:media/items` - Media library items
- `:media/filter` - Media search filter
- `:api/error` - API error messages

Add events and subscriptions in `src/slate/events.cljs` and `src/slate/subs.cljs`.

## Styling

Tailwind CSS with custom configuration in `tailwind.config.js`:

- Dark theme with slate color palette
- Responsive grid and spacing utilities
- Custom animations and transitions

## Build & Deployment

### Local Development

```bash
npm run watch        # Watch mode
npm start           # Run server
```

### Production Build

```bash
npm run build       # Release build
npm run docker:build # Build Docker image
```

### Kubernetes

```bash
kubectl apply -f k8s/
kubectl -n slate port-forward svc/slate 3000:80
```

## Troubleshooting

### Port already in use

Change `PORT` environment variable:

```bash
PORT=3001 npm start
```

### API connection errors

Check `TUNARR_API_URL` and `PSEUDOVISION_API_URL` environment variables point to correct endpoints.

View logs:

```bash
npm run watch   # Watch console for errors
```

### Kubernetes pod fails to start

```bash
kubectl -n slate describe pod <pod-name>
kubectl -n slate logs <pod-name>
```

## Contributing

This is a PoC service designed to grow with the ecosystem. Areas for future development:

- Real API integration for Tunarr Scheduler
- Real API integration for Pseudovision
- Advanced media browser with filters
- Channel management UI
- Program scheduling interface
- User authentication
- Dark/light theme toggle
- Internationalization (i18n)

## License

MIT - See LICENSE file

## Resources

- [ClojureScript](https://clojurescript.org)
- [Reagent](https://reagent-project.github.io)
- [Re-frame](https://re-frame.day8.com.au)
- [Tailwind CSS](https://tailwindcss.com)
- [Shadow CLJS](https://shadow-cljs.github.io/user-guide)
- [Kubernetes](https://kubernetes.io)
