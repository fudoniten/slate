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

### Development (Nix - Recommended)

Prerequisites: Nix with flakes enabled

```bash
nix develop              # Enter development shell
npm install              # Install npm dependencies
npm run watch            # Start shadow-cljs in watch mode
```

In another terminal:
```bash
nix develop
npm start                # Start the server
```

Visit `http://localhost:3000` in your browser.

### Development (Manual)

Prerequisites: Node.js 20+, Java 17+, Clojure

```bash
npm install
npm run watch            # Start shadow-cljs in watch mode
npm start                # Start the server (in another terminal)
```

### Production Build & Deploy (Nix)

```bash
nix build .#slate                 # Build application
nix run .#deployContainer         # Build and push container to registry
```

### Updating Dependencies

```bash
# After changing package.json or deps.edn:
nix run .#update                  # Updates npm hash
# Then follow prompts to update Maven hash if needed
```

See **Dependency Management** section for details.

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

## Dependency Management

This project uses **Nix** for reproducible builds with fixed-output derivations. Dependencies are cached using cryptographic hashes that must be updated when dependencies change.

### Understanding the Hash System

The project has **TWO** separate dependency systems with different hashes:

1. **npm dependencies** (JavaScript packages)
   - Defined in: `package.json`
   - Lock file: `package-lock.json`
   - Hash stored in: `npm-hash` file
   - Includes: react, express, shadow-cljs npm package, etc.

2. **Maven/Clojure dependencies** (Java/Clojure libraries)
   - Defined in: `deps.edn`
   - Hash stored in: `flake.nix` (line ~33, `mavenDeps.outputHash`)
   - Includes: reagent, re-frame, shadow-cljs jar, etc.

### When to Update Hashes

| You changed... | Update npm hash? | Update Maven hash? |
|----------------|------------------|-------------------|
| `package.json` | ✅ Yes | ❌ No |
| `deps.edn` | ❌ No | ✅ Yes |
| shadow-cljs version | ✅ Yes | ✅ Yes (both!) |

### Quick Update Workflow

**Option 1: Automated (Recommended for npm changes)**

```bash
nix run .#update
```

This automatically:
- Updates `package-lock.json`
- Computes and saves the new npm hash
- Tells you if Maven hash needs updating

**Option 2: Manual (Full control)**

See detailed steps below.

### Detailed Update Steps

#### Step 1: Update npm Hash (when package.json changes)

```bash
# Method A: Use the update script
nix run .#update

# Method B: Manual
# 1. Set placeholder hash
echo "sha256-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=" > npm-hash

# 2. Build to get correct hash
nix build .#slate 2>&1 | grep "got:"
# Output: got:    sha256-HWFJkJKOTUoPbKCzxpeakjji4DHr11WgvtwJ197fFXs=

# 3. Save the hash
echo "sha256-HWFJkJKOTUoPbKCzxpeakjji4DHr11WgvtwJ197fFXs=" > npm-hash
```

#### Step 2: Update Maven Hash (when deps.edn changes)

```bash
# 1. Edit flake.nix, find mavenDeps.outputHash (around line 33)
# Change it to:
outputHash = pkgs.lib.fakeHash;

# 2. Build to get correct hash
nix build .#slate 2>&1 | grep "got:"
# Output: got:    sha256-3xKKyonI0UD7CdvDdBA11e/Kb27wG5AZSo2/+hgtpR4=

# 3. Update flake.nix with the new hash:
outputHash = "sha256-3xKKyonI0UD7CdvDdBA11e/Kb27wG5AZSo2/+hgtpR4=";
```

#### Step 3: Verify the Build

```bash
nix build .#slate
ls -la result/app/server.js  # Should exist
```

### Critical Configuration

**shadow-cljs.edn MUST have `:deps true`**

```clojure
{:deps true          ;; ← REQUIRED! Tells shadow-cljs to use deps.edn
 :source-paths ["src"]
 :builds {...}}
```

Without this, shadow-cljs will try to download dependencies at build time, causing network errors in the Nix sandbox.

### Version Synchronization

**shadow-cljs version must match in both files:**

```bash
# Check versions match
grep "shadow-cljs" package-lock.json deps.edn

# If mismatched, update deps.edn to match package-lock.json
# Then update BOTH hashes (npm and Maven)
```

### Common Issues

#### "Temporary failure in name resolution" or "Could not locate shadow/cljs/..."

**Cause:** Maven hash is incorrect or shadow-cljs trying to download at runtime

**Fix:**
1. Verify `shadow-cljs.edn` has `:deps true`
2. Check shadow-cljs versions match in `deps.edn` and `package-lock.json`
3. Update Maven hash (Step 2 above)

#### "The required namespace X is not available"

**Cause:** Wrong namespace in require statement

**Fix:** Check library documentation for correct namespace:
- `day8.re-frame/http-fx` → `day8.re-frame.http-fx`
- `reagent/reagent` → `reagent.core`
- `re-frame/re-frame` → `re-frame.core`

#### "hash mismatch in fixed-output derivation"

**Cause:** The hash doesn't match the dependencies

**Fix:**
- For npm deps: Update `npm-hash` file (Step 1)
- For Maven deps: Update `mavenDeps.outputHash` in `flake.nix` (Step 2)

### Development Workflow

```bash
# 1. Make code changes
# 2. If you changed dependencies:
nix run .#update              # For npm changes
# ... or manually update hashes

# 3. Build
nix build .#slate

# 4. Deploy
nix run .#deployContainer
```

## Build & Deployment

### Nix Build (Production - Recommended)

```bash
# Build application
nix build .#slate

# Build and deploy container
nix run .#deployContainer
```

The deployment will:
- Build the application using Nix
- Create an OCI container image
- Push to `registry.kube.sea.fudo.link/slate:latest`
- Kubernetes will auto-pull the new image

### Local Development

```bash
nix develop          # Enter development shell
npm run watch        # Watch mode with hot reload
npm start            # Run server (in another terminal)
```

### Kubernetes

```bash
kubectl apply -f k8s/
kubectl -n slate get pods
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

## Documentation

- **[DEPENDENCY_UPDATE_GUIDE.md](DEPENDENCY_UPDATE_GUIDE.md)** - Complete dependency update reference
- **README.md** (this file) - Project overview and documentation
- **flake.nix** - Nix build configuration
- Run `nix develop` for quick command reference

## Resources

- [ClojureScript](https://clojurescript.org)
- [Reagent](https://reagent-project.github.io)
- [Re-frame](https://re-frame.day8.com.au)
- [Tailwind CSS](https://tailwindcss.com)
- [Shadow CLJS](https://shadow-cljs.github.io/user-guide)
- [Kubernetes](https://kubernetes.io)
