#!/bin/bash
# Slate Project Initialization Script
# This script initializes all necessary dependencies and builds the project

set -e

echo "🚀 Slate Project Initialization"
echo "=================================="
echo

# Check prerequisites
echo "✓ Checking prerequisites..."
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is required but not installed."
    exit 1
fi
if ! command -v npm &> /dev/null; then
    echo "❌ npm is required but not installed."
    exit 1
fi
echo "✓ Node.js $(node --version) detected"
echo "✓ npm $(npm --version) detected"
echo

# Install dependencies
echo "📦 Installing npm dependencies..."
npm install --loglevel=warn
echo "✓ Dependencies installed"
echo

# Build configuration
echo "⚙️  Building production assets..."
npm run build
echo "✓ Production build complete"
echo

# Summary
echo "✅ Initialization Complete!"
echo
echo "Next steps:"
echo "1. Development: npm run watch"
echo "2. Start server: npm start"
echo "3. Deploy K8s: kubectl apply -f k8s/"
echo
echo "📚 Documentation:"
echo "   - README.md - Full project guide"
echo "   - PROJECT_MANIFEST.md - Project structure"
echo "   - TEMPLATE.md - Template usage guide"
echo
echo "🌐 Local development:"
echo "   - Visit http://localhost:3000"
echo
echo "💻 API Integration:"
echo "   - Tunarr: http://localhost:8000"
echo "   - Pseudovision: http://localhost:9000"
echo
