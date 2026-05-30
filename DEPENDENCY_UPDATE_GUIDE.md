# Dependency Update Guide

Quick reference for updating dependencies in the Slate project.

## TL;DR - Quick Commands

```bash
# Changed package.json?
nix run .#update

# Changed deps.edn?
# 1. Edit flake.nix: outputHash = pkgs.lib.fakeHash;
# 2. nix build .#slate 2>&1 | grep "got:"
# 3. Update flake.nix with the hash from step 2
# 4. nix build .#slate
```

## Understanding the System

### Two Dependency Systems

| System | File | Hash Location | Purpose |
|--------|------|---------------|---------|
| **npm** | `package.json` | `npm-hash` file | JavaScript packages (react, express, etc.) |
| **Maven** | `deps.edn` | `flake.nix` line ~33 | Clojure/Java libraries (reagent, re-frame, etc.) |

### Decision Tree

```
Did you change package.json?
├─ Yes → Update npm hash
│   └─ Run: nix run .#update
└─ No → Skip

Did you change deps.edn?
├─ Yes → Update Maven hash
│   └─ Follow Maven update steps
└─ No → Skip

Did you change shadow-cljs version?
├─ Yes → Update BOTH hashes
│   ├─ 1. Run: nix run .#update
│   └─ 2. Follow Maven update steps
└─ No → Skip
```

## Detailed Steps

### Updating npm Hash

**When:** You changed `package.json`

**Method 1: Automated (Recommended)**
```bash
nix run .#update
```

**Method 2: Manual**
```bash
# 1. Set placeholder
echo "sha256-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=" > npm-hash

# 2. Build to get hash
nix build .#slate 2>&1 | grep "got:"

# 3. Save the hash
echo "sha256-<hash-from-step-2>" > npm-hash
```

### Updating Maven Hash

**When:** You changed `deps.edn`

```bash
# 1. Edit flake.nix (line ~33)
outputHash = pkgs.lib.fakeHash;

# 2. Build to get hash
nix build .#slate 2>&1 | grep "got:"

# Example output:
#   got:    sha256-3xKKyonI0UD7CdvDdBA11e/Kb27wG5AZSo2/+hgtpR4=

# 3. Update flake.nix with the real hash
outputHash = "sha256-3xKKyonI0UD7CdvDdBA11e/Kb27wG5AZSo2/+hgtpR4=";

# 4. Verify
nix build .#slate
```

## Common Scenarios

### Scenario 1: Adding a new npm package

```bash
# 1. Edit package.json - add the package
# 2. Update hash
nix run .#update
# 3. Build
nix build .#slate
```

### Scenario 2: Adding a new Clojure library

```bash
# 1. Edit deps.edn - add the library
# 2. Update Maven hash (see above)
# 3. Build
nix build .#slate
```

### Scenario 3: Upgrading shadow-cljs

```bash
# 1. Edit package.json - update shadow-cljs version
# 2. Update npm hash
nix run .#update

# 3. Edit deps.edn - match the shadow-cljs version
# 4. Update Maven hash (see above)
# 5. Build
nix build .#slate
```

### Scenario 4: Build fails with "namespace not available"

```bash
# Example error:
# The required namespace "re-frame-http-fx.http-fx" is not available

# This means the require statement doesn't match the library

# Fix:
# 1. Check what namespace the library provides
#    - day8.re-frame/http-fx → day8.re-frame.http-fx
#    - reagent/reagent → reagent.core
# 2. Update the require statement in your .cljs file
# 3. Build again
nix build .#slate
```

## Critical Configuration

### shadow-cljs.edn MUST have :deps true

**Correct:**
```clojure
{:deps true          ;; ← REQUIRED
 :source-paths ["src"]
 :builds {...}}
```

**Wrong:**
```clojure
{:dependencies [[...]]  ;; ← Don't do this!
 :builds {...}}
```

**Why:** Without `:deps true`, shadow-cljs tries to download dependencies at build time, which fails in the Nix sandbox.

### Version Synchronization

shadow-cljs must match in THREE places:

1. `package.json`: `"shadow-cljs": "^2.28.23"`
2. `package-lock.json`: (auto-updated by npm)
3. `deps.edn`: `thheller/shadow-cljs {:mvn/version "2.28.23"}`

**Check versions:**
```bash
grep "shadow-cljs" package.json package-lock.json deps.edn
```

If they don't match, update `deps.edn` to match `package-lock.json`.

## Troubleshooting

### Error: "hash mismatch in fixed-output derivation"

**For npm-deps:**
```bash
nix run .#update
```

**For maven-deps:**
```bash
# Update Maven hash (see above)
```

### Error: "Temporary failure in name resolution"

**Causes:**
1. Maven hash is incorrect
2. shadow-cljs trying to download deps at runtime

**Fix:**
```bash
# 1. Check shadow-cljs.edn has :deps true
grep "^{:deps" shadow-cljs.edn

# 2. Check versions match
grep "shadow-cljs" package-lock.json deps.edn

# 3. Update Maven hash
```

### Error: "Could not locate shadow/cljs/devtools/cli"

**Cause:** Maven dependencies not properly cached

**Fix:**
```bash
# Update Maven hash (see above)
```

### Error: Build takes forever or hangs

**Cause:** Trying to download dependencies during build

**Fix:**
1. Verify `:deps true` in `shadow-cljs.edn`
2. Update Maven hash
3. Clear build cache: `nix-collect-garbage`

## Complete Workflow Example

```bash
# 1. Make code changes
vim src/slate/app.cljs

# 2. Add a new npm package
vim package.json
# Added: "axios": "^1.6.0"

# 3. Add a new Clojure library
vim deps.edn
# Added: cljs-ajax/cljs-ajax {:mvn/version "0.8.4"}

# 4. Update npm hash
nix run .#update

# 5. Update Maven hash
vim flake.nix  # Set outputHash = pkgs.lib.fakeHash;
nix build .#slate 2>&1 | grep "got:"
vim flake.nix  # Update with real hash

# 6. Verify build
nix build .#slate
ls -la result/app/server.js

# 7. Deploy
nix run .#deployContainer

# 8. Commit
git add package.json package-lock.json npm-hash deps.edn flake.nix src/
git commit -m "Add axios and cljs-ajax, implement new feature"
```

## File Modification Checklist

Before committing, ensure you've updated:

- [ ] `package.json` (if npm deps changed)
- [ ] `package-lock.json` (auto-updated by `nix run .#update`)
- [ ] `npm-hash` (auto-updated by `nix run .#update`)
- [ ] `deps.edn` (if Clojure deps changed)
- [ ] `flake.nix` mavenDeps.outputHash (if deps.edn changed)
- [ ] `shadow-cljs.edn` has `:deps true`
- [ ] shadow-cljs version matches in all three files

## Quick Reference

| Task | Command |
|------|---------|
| Update npm hash | `nix run .#update` |
| Update Maven hash | See "Updating Maven Hash" above |
| Check build | `nix build .#slate` |
| Deploy | `nix run .#deployContainer` |
| Enter dev shell | `nix develop` |
| Check versions | `grep "shadow-cljs" package.json deps.edn` |

## Getting Help

1. Check error message for "hash mismatch" - update that hash
2. Check README.md "Dependency Management" section
3. Run `nix develop` to see quick reference
4. Check this guide

## See Also

- README.md - Full project documentation
- flake.nix - Nix build configuration
- deps.edn - Clojure dependencies
- package.json - npm dependencies
- shadow-cljs.edn - Shadow CLJS configuration
