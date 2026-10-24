'use strict';

/**
 * TBRide :: Express application factory.
 * Plain Node.js + Express. No frontend framework, no build step: the files in
 * /public are served exactly as authored.
 */

const path = require('path');
const express = require('express');
const config = require('../config');
const logger = require('../utils/logger');
const { toPublicError, NotFoundError } = require('../utils/errors');

const authRoutes = require('./routes/auth.routes');
const adminRoutes = require('./routes/admin.routes');
const telegramRoutes = require('./routes/telegram.routes');
const driverApplicationRoutes = require('./routes/driver-applications.routes');

function createApp() {
  const app = express();

  app.disable('x-powered-by');
  app.set('trust proxy', true);

  app.use((_req, res, next) => {
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('Referrer-Policy', 'no-referrer');
    res.setHeader('X-Frame-Options', 'DENY');
    res.setHeader('X-XSS-Protection', '0');
    res.setHeader(
      'Content-Security-Policy',
      "default-src 'self'; script-src 'self' https://cdn.socket.io 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' ws: wss:; frame-ancestors 'none';"
    );
    next();
  });

  // CORS (configurable, "*" by default for local development).
  app.use((req, res, next) => {
    const origin = req.headers.origin;
    if (!origin) return next();
    if (config.cors.origins.includes('*') || config.cors.origins.includes(origin)) {
      res.setHeader('Access-Control-Allow-Origin', origin);
      res.setHeader('Vary', 'Origin');
      res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
      res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, DELETE, OPTIONS');
    }
    return next();
  });

  app.options(/.*/, (_req, res) => res.sendStatus(204));

  app.use(express.json({ limit: '256kb' }));
  app.use(express.urlencoded({ extended: false }));

  /** Request timeout: 30s for all HTTP routes. */
  app.use((req, res, next) => {
    const timer = setTimeout(() => {
      if (!res.headersSent) {
        res.status(504).json({ error: { code: 'REQUEST_TIMEOUT', message: 'Request timed out' } });
      }
    }, 30000);
    res.on('finish', () => clearTimeout(timer));
    res.on('close', () => clearTimeout(timer));
    next();
  });

  // Request log (skip the health probe noise).
  app.use((req, _res, next) => {
    if (req.path !== '/api/health') logger.debug('HTTP request', { method: req.method, path: req.path });
    next();
  });

  app.use((_req, res, next) => {
    const id = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;
    res.setHeader('X-Request-Id', id);
    next();
  });

  app.get('/api/version', (_req, res) => {
    res.json({ name: config.app.name, version: '1.0.0', node: process.version });
  });

  /** GET /api/docs — lightweight endpoint reference. */
  app.get('/api/docs', (_req, res) => {
    res.json({
      name: 'TBRide API',
      version: '1.0.0',
      endpoints: [
        { method: 'GET', path: '/api/health', desc: 'Health check (DB + sockets)' },
        { method: 'GET', path: '/api/version', desc: 'Service version info' },
        { method: 'GET', path: '/api/docs', desc: 'This endpoint reference' },
        { method: 'GET', path: '/api/track/:rideId', desc: 'Public ride tracking (no auth)' },
        { method: 'POST', path: '/api/auth/login', desc: 'Rider/driver login' },
        { method: 'POST', path: '/api/auth/register', desc: 'Rider registration' },
        { method: 'GET', path: '/api/auth/me', desc: 'Current user profile' },
        { method: 'GET', path: '/api/overview', desc: 'Role-aware snapshot' },
        { method: 'GET', path: '/api/drivers', desc: 'List all drivers (operator/admin)' },
        { method: 'GET', path: '/api/drivers/nearby', desc: 'Drivers near a point (operator/admin)' },
        { method: 'PATCH', path: '/api/drivers/status', desc: 'Set driver status (driver)' },
        { method: 'POST', path: '/api/drivers/location', desc: 'Update driver GPS (driver)' },
        { method: 'GET', path: '/api/rides', desc: 'Live rides (operator/admin)' },
        { method: 'GET', path: '/api/rides/:id', desc: 'Single ride (authenticated)' },
        { method: 'GET', path: '/api/rides/history', desc: 'User ride history' },
        { method: 'GET', path: '/api/rides/search', desc: 'Filtered rides (operator/admin)' },
        { method: 'GET', path: '/api/rides/estimate', desc: 'Fare estimate (authenticated)' },
        { method: 'PATCH', path: '/api/rides/:id/notes', desc: 'Operator note (operator/admin)' },
        { method: 'POST', path: '/api/rides/:id/reassign', desc: 'Reassign ride (operator/admin)' },
        { method: 'POST', path: '/api/rides/:id/rate', desc: 'Rate a ride (user/driver)' },
        { method: 'GET', path: '/api/admin/stats', desc: 'Platform stats (admin)' },
        { method: 'GET', path: '/api/logs', desc: 'System logs (admin)' },
        { method: 'POST', path: '/api/telegram/rides', desc: 'Bot: book a ride' },
        { method: 'GET', path: '/api/telegram/rides', desc: 'Bot: ride history' },
        { method: 'POST', path: '/api/telegram/rides/:id/cancel', desc: 'Bot: cancel a ride' },
      ],
    });
  });

  app.get('/healthz', (_req, res) => {
    res.status(200).end();
  });

  // The static frontend MUST be plain HTML/CSS/JS served from /public.
  app.use(express.static(path.join(__dirname, '../public'), { extensions: ['html'], index: 'index.html' }));

  app.use('/api/auth', authRoutes);
  app.use('/api/admin', adminRoutes);
  app.use('/api/telegram', telegramRoutes);
  app.use('/api/driver-applications', driverApplicationRoutes);

  /** GET /api/track/:rideId — public ride tracking (no auth, limited fields). */
  app.get('/api/track/:rideId', async (req, res, next) => {
    try {
      const { assertUuid } = require('../utils/validation');
      const ridesRepo = require('../repositories/rides.repository');
      const { serialiseRide } = require('../realtime/snapshots');
      const rideId = assertUuid(req.params.rideId, 'ride id');
      const ride = await ridesRepo.findById(rideId);
      if (!ride) throw new NotFoundError('Ride not found');
      const s = serialiseRide(ride);
      res.json({
        rideId: s.id,
        status: s.status,
        pickup: s.pickup,
        dropoff: s.dropoff,
        driverName: s.driverName,
        driverPhone: s.driverPhone,
        vehicleModel: s.vehicleModel,
        licensePlate: s.licensePlate,
        fare: s.quote?.fare,
        createdAt: s.createdAt,
      });
    } catch (err) {
      next(err);
    }
  });

  app.use('/api', authRoutes); // /api/health, /api/drivers, /api/rides, ...

  // Unknown API paths stay JSON 404s; other GET paths use the frontend shell.
  app.use((req, res, next) => {
    if (req.path.startsWith('/api/')) {
      return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Unknown endpoint' } });
    }
    return next();
  });

  app.get('*', (_req, res) => {
    res.sendFile(path.join(__dirname, '../public/index.html'));
  });

  // Central error handler: internal details never reach the client.
  // eslint-disable-next-line no-unused-vars
  app.use((err, req, res, _next) => {
    const publicError = toPublicError(err);
    if (publicError.statusCode >= 500) {
      logger.error('Unhandled HTTP error', { error: err && err.message, stack: err && err.stack });
    } else {
      logger.warn('HTTP error', { code: publicError.code, message: publicError.message, path: req.path });
    }
    res.status(publicError.statusCode).json({
      error: {
        code: publicError.code,
        message: publicError.message,
        details: publicError.details || undefined,
      },
    });
  });

  return app;
}

module.exports = { createApp };
