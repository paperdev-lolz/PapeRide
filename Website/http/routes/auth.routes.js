'use strict';

/**
 * TBRide :: auth + public bootstrap routes.
 */

/** Simple in-memory rate limiter for auth endpoints. */
const _buckets = new Map();
function rateLimit(windowMs, max) {
  return (req, res, next) => {
    const key = (req.ip || req.connection.remoteAddress || '') + ':' + (req.path || '');
    const now = Date.now();
    let bucket = _buckets.get(key);
    if (!bucket || now - bucket.start > windowMs) {
      bucket = { start: now, count: 0 };
      _buckets.set(key, bucket);
    }
    bucket.count++;
    if (bucket.count > max) {
      return res.status(429).json({ error: { code: 'RATE_LIMITED', message: 'Too many requests' } });
    }
    return next();
  };
}


const express = require('express');
const authService = require('../../services/auth.service');
const driversRepo = require('../../repositories/drivers.repository');
const ridesRepo = require('../../repositories/rides.repository');
const systemLogs = require('../../repositories/logs.repository');
const { authenticate, requireRole } = require('../middleware/auth.middleware');
const { ValidationError } = require('../../utils/errors');
const {
  validateRidePayload,
  assertUuid,
  assertOptionalLabel,
  assertOptionalPositiveInt,
  assertLatitude,
  assertLongitude,
} = require('../../utils/validation');
const config = require('../../config');
const io = require('../../realtime/io');

const router = express.Router();

/** POST /api/auth/login  { phone_number, password } -> { token, user } */
router.post('/login', rateLimit(60000, 20), async (req, res, next) => {
  try {
    if (!req.body || typeof req.body.phone_number !== 'string' || typeof req.body.password !== 'string') {
      throw new ValidationError('phone_number and password are required');
    }
    const session = await authService.loginWithPassword(req.body.phone_number, req.body.password);
    res.json(session);
  } catch (err) {
    next(err);
  }
});

/** POST /api/auth/register { full_name, phone_number, password } -> { token, user } */
router.post('/register', rateLimit(60000, 10), async (req, res, next) => {
  try {
    const body = req.body || {};
    const session = await authService.registerRider(body.full_name, body.phone_number, body.password);
    res.status(201).json(session);
  } catch (err) {
    next(err);
  }
});

/** GET /api/auth/me */
router.get('/me', authenticate, async (req, res, next) => {
  try {
    res.json({ user: await authService.me(req.identity) });
  } catch (err) {
    next(err);
  }
});

/** GET /api/health — used by the sandbox healthcheck. */
router.get('/health', async (_req, res) => {
  const payload = {
    ok: true,
    service: config.app.name,
    env: config.env,
    uptimeSeconds: Math.round(process.uptime()),
    sockets: { connected: io.connectedSessionCount(), byRole: io.sessionsByRole() },
    memory: { rssMb: Math.round(process.memoryUsage().rss / 1024 / 1024), heapMb: Math.round(process.memoryUsage().heapUsed / 1024 / 1024) },
    timestamp: new Date().toISOString(),
  };
  try {
    const info = await require('../../db').verifyConnection();
    payload.database = { ok: true, database: info.db, serverTime: info.now };
  } catch (err) {
    payload.ok = false;
    payload.database = { ok: false, error: 'unavailable' };
    return res.status(500).json(payload);
  }
  return res.json(payload);
});

/** GET /api/health/detailed — extended health with dependency status. */
router.get('/health/detailed', async (_req, res, next) => {
  const checks = {
    service: { ok: true, name: config.app.name, uptimeSeconds: Math.round(process.uptime()) },
    database: { ok: false },
    memory: {
      rssMb: Math.round(process.memoryUsage().rss / 1024 / 1024),
      heapMb: Math.round(process.memoryUsage().heapUsed / 1024 / 1024),
    },
  };
  let overallOk = true;
  try {
    const info = await require('../../db').verifyConnection();
    checks.database = { ok: true, database: info.db, serverTime: info.now };
  } catch (err) {
    checks.database = { ok: false, error: err.message };
    overallOk = false;
  }
  const status = overallOk ? 200 : 503;
  res.status(status).json({ ok: overallOk, checks, timestamp: new Date().toISOString() });
});

/** GET /api/overview — authenticated role-aware snapshot (REST fallback). */
router.get('/overview', authenticate, async (req, res, next) => {
  try {
    const { buildSnapshot } = require('../../realtime/snapshots');
    const snapshot = await buildSnapshot(req.identity);
    res.json(snapshot ? snapshot.payload : {});
  } catch (err) {
    next(err);
  }
});

/** GET /api/drivers — operators and admins only. */
router.get('/drivers', authenticate, requireRole('operator', 'admin'), async (_req, res, next) => {
  try {
    const drivers = await driversRepo.listAll();
    res.json({
      drivers: drivers.map((d) => ({
        driverId: d.driver_id || d.id,
        fullName: d.full_name,
        phone: d.phone_number,
        vehicleModel: d.vehicle_model,
        licensePlate: d.license_plate,
        status: d.status,
        location:
          d.current_lat !== null && d.current_lat !== undefined
            ? { lat: Number(d.current_lat), lng: Number(d.current_lng) }
            : null,
        updatedAt: d.updated_at,
      })),
    });
  } catch (err) {
    next(err);
  }
});

/** GET /api/rides — operators and admins only. */
router.get('/rides', authenticate, requireRole('operator', 'admin'), async (req, res, next) => {
  try {
    const status = req.query.status;
    const statuses = status
      ? String(status).split(',').map((s) => s.trim()).filter(Boolean)
      : ['pending', 'assigned', 'in_progress'];
    const rides = await ridesRepo.listByStatuses(statuses, { limit: req.query.limit });
    const { serialiseRide } = require('../../realtime/snapshots');
    res.json({ rides: rides.map(serialiseRide) });
  } catch (err) {
    next(err);
  }
});

/** GET /api/rides/history — a rider's own ride history. */
router.get('/rides/history', authenticate, async (req, res, next) => {
  try {
    if (req.identity.role === 'driver') {
      const rides = await ridesRepo.listByDriver(req.identity.id);
      const { serialiseRide } = require('../../realtime/snapshots');
      return res.json({ rides: rides.map(serialiseRide) });
    }
    if (req.identity.role !== 'user') {
      throw new ValidationError('Role has no ride history');
    }
    const rides = await ridesRepo.listByUser(req.identity.id);
    const { serialiseRide } = require('../../realtime/snapshots');
    return res.json({ rides: rides.map(serialiseRide) });
  } catch (err) {
    return next(err);
  }
});

/** GET /api/logs — admins only. */
router.get('/logs', authenticate, requireRole('admin'), async (req, res, next) => {
  try {
    res.json({ logs: await systemLogs.list(req.query) });
  } catch (err) {
    next(err);
  }
});

/** GET /api/rides/estimate — fare estimate for a route (authenticated). */
router.get('/rides/estimate', authenticate, async (req, res, next) => {
  try {
    const { quote } = require('../../services/rides.service');
    const est = quote(
      { pickup_lat: req.query.pickup_lat, pickup_lng: req.query.pickup_lng },
      { dropoff_lat: req.query.dropoff_lat, dropoff_lng: req.query.dropoff_lng },
    );
    res.json({ estimate: est });
  } catch (err) {
    next(err);
  }
});

/** GET /api/rides/search — filtered ride listing (operators/admins). */
router.get('/rides/search', authenticate, requireRole('operator', 'admin'), async (req, res, next) => {
  try {
    const status = req.query.status;
    const statuses = status
      ? String(status).split(',').map((s) => s.trim()).filter(Boolean)
      : undefined;
    const limit = assertOptionalPositiveInt(req.query.limit, 'limit', 50);
    const rides = await ridesRepo.listByStatuses(statuses || ['pending', 'assigned', 'in_progress', 'completed', 'cancelled'], { limit });
    const { serialiseRide } = require('../../realtime/snapshots');
    res.json({ rides: rides.map(serialiseRide), limit });
  } catch (err) {
    next(err);
  }
});

/** PATCH /api/rides/:id/notes — operator note on a ride. */
router.patch('/rides/:id/notes', authenticate, requireRole('operator', 'admin'), async (req, res, next) => {
  try {
    const rideId = assertUuid(req.params.id, 'ride id');
    const { note } = req.body || {};
    const ride = await ridesRepo.findById(rideId);
    if (!ride) throw new NotFoundError('Ride not found');
    await systemLogs.log({
      actorId: req.identity.id,
      actorRole: req.identity.role,
      event: 'ride.note_added',
      entityId: rideId,
      level: 'info',
      details: { note: String(note || '').slice(0, 500) },
    });
    const io_ = require('../../realtime/io');
    io_.emitToRoom(`ride:${rideId}`, 'ride:note', { rideId, note, by: req.identity.id });
    res.json({ ok: true, rideId, note });
  } catch (err) {
    next(err);
  }
});

/** POST /api/rides/:id/reassign — reassign a ride to another driver (operators/admins). */
router.post('/rides/:id/reassign', authenticate, requireRole('operator', 'admin'), async (req, res, next) => {
  try {
    const rideId = assertUuid(req.params.id, 'ride id');
    const { driverId } = req.body || {};
    const targetDriver = assertUuid(driverId, 'driver id');
    const { assignRideToDriver } = require('../../services/dispatch.service');
    const result = await assignRideToDriver({ id: req.identity.id, role: req.identity.role }, rideId, targetDriver);
    res.json({ ok: true, ride: result.ride });
  } catch (err) {
    next(err);
  }
});

/** POST /api/rides/:id/rate — rate a completed ride (rider or driver). */
router.post('/rides/:id/rate', authenticate, async (req, res, next) => {
  try {
    const rideId = assertUuid(req.params.id, 'ride id');
    const { rating, comment } = req.body || {};
    const numRating = Number(rating);
    if (!Number.isInteger(numRating) || numRating < 1 || numRating > 5) {
      throw new ValidationError('Rating must be an integer between 1 and 5', { field: 'rating' });
    }
    const ride = await ridesRepo.findById(rideId);
    if (!ride) throw new NotFoundError('Ride not found');
    if (req.identity.role === 'user' && ride.user_id !== req.identity.id) {
      throw new ValidationError('Riders can only rate their own rides');
    }
    if (req.identity.role === 'driver' && ride.assigned_driver_id !== req.identity.id) {
      throw new ValidationError('Drivers can only rate rides they served');
    }
    await systemLogs.log({
      actorId: req.identity.id,
      actorRole: req.identity.role,
      event: 'ride.rated',
      entityId: rideId,
      level: 'info',
      details: { rating: numRating, comment: String(comment || '').slice(0, 500) },
    });
    // Persist rating if the ride_ratings table (migration 003) exists.
    try {
      const { query } = require('../db');
      const ratedId = req.identity.role === 'user' ? ride.assigned_driver_id : ride.user_id;
      if (ratedId) {
        await query(
          `INSERT INTO ride_ratings (ride_id, rater_id, rated_id, rating, comment)
           VALUES ($1, $2, $3, $4, $5)
           ON CONFLICT (ride_id, rater_id) DO UPDATE SET rating = EXCLUDED.rating, comment = EXCLUDED.comment, created_at = now()`,
          [rideId, req.identity.id, ratedId, numRating, String(comment || '').slice(0, 500)]
        );
      }
    } catch (ratingErr) {
      // Table may not exist yet — non-fatal.
      console.warn('ride_ratings table not available:', ratingErr.message);
    }
    res.json({ ok: true, rideId, rating: numRating });
  } catch (err) {
    next(err);
  }
});

/** GET /api/rides/:id/ratings — public rating summary for a ride. */
router.get('/rides/:id/ratings', async (req, res, next) => {
  try {
    const rideId = assertUuid(req.params.id, 'ride id');
    const ride = await ridesRepo.findById(rideId);
    if (!ride) throw new NotFoundError('Ride not found');
    res.json({
      rideId,
      hasRatings: false,
      message: 'Rating summaries require the ride_ratings migration (003).',
    });
  } catch (err) {
    next(err);
  }
});

/** GET /api/admin/stats — platform statistics (admins only). */
router.get('/admin/stats', authenticate, requireRole('admin'), async (_req, res, next) => {
  try {
    const statusCounts = await ridesRepo.countByStatus();
    const drivers = await driversRepo.listAll();
    const onlineDrivers = drivers.filter((d) => d.status === 'online').length;
    res.json({
      rides_by_status: Object.fromEntries(statusCounts.map((s) => [s.status, s.total])),
      drivers_total: drivers.length,
      drivers_online: onlineDrivers,
      uptime_seconds: Math.round(process.uptime()),
      timestamp: new Date().toISOString(),
    });
  } catch (err) {
    next(err);
  }
});

/** GET /api/drivers/nearby — drivers near a point (operators/admins). */
router.get('/drivers/nearby', authenticate, requireRole('operator', 'admin'), async (req, res, next) => {
  try {
    const lat = assertLatitude(req.query.lat, 'lat');
    const lng = assertLongitude(req.query.lng, 'lng');
    const radiusKm = Number(req.query.radius_km || 10);
    const drivers = await driversRepo.listAll();
    const { haversineKm } = require('../../utils/validation');
    const nearby = drivers
      .filter((d) => d.current_lat !== null && d.current_lng !== null)
      .map((d) => ({
        driverId: d.driver_id || d.id,
        fullName: d.full_name,
        phone: d.phone_number,
        vehicleModel: d.vehicle_model,
        licensePlate: d.license_plate,
        status: d.status,
        distanceKm: Math.round(haversineKm(lat, lng, Number(d.current_lat), Number(d.current_lng)) * 100) / 100,
      }))
      .filter((d) => d.distanceKm <= radiusKm)
      .sort((a, b) => a.distanceKm - b.distanceKm);
    res.json({ drivers: nearby, center: { lat, lng }, radiusKm });
  } catch (err) {
    next(err);
  }
});

/** GET /api/rides/:id — get a single ride (authenticated, owner or staff). */
router.get('/rides/:id', authenticate, async (req, res, next) => {
  try {
    const rideId = assertUuid(req.params.id, 'ride id');
    const ride = await ridesRepo.findById(rideId);
    if (!ride) throw new NotFoundError('Ride not found');
    if (req.identity.role === 'user' && ride.user_id !== req.identity.id) {
      throw new ValidationError('Riders can only read their own rides');
    }
    const { serialiseRide } = require('../../realtime/snapshots');
    res.json({ ride: serialiseRide(ride) });
  } catch (err) {
    next(err);
  }
});

/** PATCH /api/drivers/status — driver sets online/offline/active. */
router.patch('/drivers/status', authenticate, requireRole('driver'), async (req, res, next) => {
  try {
    const { status } = req.body || {};
    if (!['offline', 'online', 'active'].includes(status)) {
      throw new ValidationError('Invalid status', { field: 'status' });
    }
    const driversService = require('../../services/drivers.service');
    await driversService.changeStatus({ id: req.identity.id, role: 'driver' }, status);
    res.json({ ok: true, status });
  } catch (err) {
    next(err);
  }
});

/** POST /api/drivers/location — driver updates current location. */
router.post('/drivers/location', authenticate, requireRole('driver'), async (req, res, next) => {
  try {
    const { lat, lng } = req.body || {};
    const driversService = require('../../services/drivers.service');
    await driversService.updateLocation({ id: req.identity.id, role: 'driver' }, { lat, lng });
    res.json({ ok: true, lat, lng });
  } catch (err) {
    next(err);
  }
});

/** GET /api/drivers/:id/ratings — rating summary for a driver (public). */
router.get('/drivers/:id/ratings', async (req, res, next) => {
  try {
    const driverId = assertUuid(req.params.id, 'driver id');
    try {
      const { query } = require('../db');
      const rows = await query(
        `SELECT COUNT(*)::int AS total, COALESCE(AVG(rating), 0)::numeric(3,2) AS average
         FROM ride_ratings WHERE rated_id = $1`,
        [driverId]
      );
      const row = rows.rows[0];
      res.json({
        driverId,
        totalRatings: row.total,
        averageRating: Number(row.average),
      });
    } catch (dbErr) {
      res.json({ driverId, totalRatings: 0, averageRating: null, note: 'ride_ratings table not available' });
    }
  } catch (err) {
    next(err);
  }
});

/** GET /api/drivers/:id — single driver profile (operators/admins). */
router.get('/drivers/:id', authenticate, requireRole('operator', 'admin'), async (req, res, next) => {
  try {
    const driverId = assertUuid(req.params.id, 'driver id');
    const drivers = await driversRepo.listAll();
    const driver = drivers.find((d) => (d.driver_id || d.id) === driverId);
    if (!driver) throw new NotFoundError('Driver not found');
    res.json({
      driverId: driver.driver_id || driver.id,
      fullName: driver.full_name,
      phone: driver.phone_number,
      vehicleModel: driver.vehicle_model,
      licensePlate: driver.license_plate,
      status: driver.status,
      location:
        driver.current_lat !== null && driver.current_lat !== undefined
          ? { lat: Number(driver.current_lat), lng: Number(driver.current_lng) }
          : null,
      updatedAt: driver.updated_at,
    });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
