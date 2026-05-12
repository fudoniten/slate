const express = require('express');
const compression = require('compression');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(compression());
app.use(express.json());

// Static files
app.use(express.static(path.join(__dirname, 'resources', 'public')));

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Readiness check for Kubernetes
app.get('/ready', (req, res) => {
  res.status(200).json({ ready: true, timestamp: new Date().toISOString() });
});

// API proxy routes (for future integration)
app.use('/api/tunarr', (req, res) => {
  res.status(503).json({ error: 'Tunarr API integration not yet configured' });
});

app.use('/api/pseudovision', (req, res) => {
  res.status(503).json({ error: 'Pseudovision API integration not yet configured' });
});

// SPA fallback - serve index.html for all unmatched routes
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'resources', 'public', 'index.html'));
});

// Error handling
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({ error: 'Internal Server Error', message: err.message });
});

const server = app.listen(PORT, '0.0.0.0', () => {
  console.log(`Slate UI service listening on port ${PORT}`);
  console.log(`Health check: http://localhost:${PORT}/health`);
  console.log(`Readiness check: http://localhost:${PORT}/ready`);
});

process.on('SIGTERM', () => {
  console.log('SIGTERM signal received: closing HTTP server');
  server.close(() => {
    console.log('HTTP server closed');
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  console.log('SIGINT signal received: closing HTTP server');
  server.close(() => {
    console.log('HTTP server closed');
    process.exit(0);
  });
});
