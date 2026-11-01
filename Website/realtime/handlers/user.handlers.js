'use strict';

/**
 * TBRide :: rider socket handlers.
 *
 *   user:request_ride   -> creates a pending ride + notifies the dispatch floor
 *   user:cancel_ride    -> pending/assigned -> cancelled (owner only)
 */

const ridesService = require('../../services/rides.service');
const events = require('../ride.events');
const { ROOMS, EVENTS } = require('../rooms');
const io = require('../io');

module.exports = function registerUserHandlers(socket, { register }) {
  const identity = () => socket.data.identity;

  register(socket, EVENTS.USER_REQUEST_RIDE, async (payload) => {
    const result = await ridesService.createRide(identity(), payload);
    const ride = result.ride;

    // Keep the rider's socket attached to this ride's room.
    socket.join(ROOMS.ride(ride.id));

    events.emitRideCreated(ride);
    events.emitNewRideRequest(ride);
    return { ride: events.serialiseRide(ride), quote: result.quote };
  });

  register(socket, EVENTS.USER_CANCEL_RIDE, async (payload) => {
    const rideId = payload && (payload.ride_id || payload.rideId);
    const reason = payload && payload.reason;
    const result = await ridesService.cancelRide(identity(), rideId, reason);
    const ride = result.ride;

    events.emitRideStatusUpdate({
      ride,
      previousStatus: result.releasedDriverId || ride.status === 'cancelled' ? 'assigned' : 'pending',
      stage: 'cancelled_by_rider',
      actor: { id: identity().id, role: 'user', name: identity().name },
    });
    events.emitRideRemovedFromBoard(ride.id, 'cancelled');
    if (result.releasedDriverId) events.emitRideWithdrawn(ride.id, result.releasedDriverId, 'cancelled_by_rider');
    return { ride: events.serialiseRide(ride) };
  });

  register(socket, EVENTS.USER_EMERGENCY, async (payload) => {
    const rideId = payload && (payload.ride_id || payload.rideId);
    const location = payload && payload.location;
    const note = (payload && payload.note) || 'Emergency SOS triggered by rider from website';
    const emergencyState = require('../../services/emergency.state');
    const ridesRepo = require('../../repositories/rides.repository');
    const systemLogs = require('../../services/system-logs.service');
    const { ValidationError, NotFoundError } = require('../../utils/errors');

    if (!rideId) throw new ValidationError('rideId is required');
    const ride = await ridesRepo.findById(rideId);
    if (!ride) throw new NotFoundError('Ride not found');
    if (ride.user_id !== identity().id) {
      throw new ValidationError('This ride does not belong to the user', { field: 'ride_id' });
    }

    const details = {
      rideId: ride.id,
      userId: identity().id,
      riderName: identity().name || identity().fullName || 'Web Rider',
      riderPhone: identity().phone || null,
      assignedDriverId: ride.assigned_driver_id,
      location,
      note,
      triggeredAt: new Date().toISOString(),
    };

    emergencyState.setEmergency(ride.id, details);

    await systemLogs.log({
      actorId: identity().id,
      actorRole: 'user',
      event: 'rider.emergency_sos',
      entityId: ride.id,
      level: 'warn',
      details,
    });

    events.emitRideEmergency(ride, details);
    return { ok: true, rideId: ride.id, isEmergency: true };
  });

  register(socket, EVENTS.USER_CLEAR_EMERGENCY, async (payload) => {
    const rideId = payload && (payload.ride_id || payload.rideId);
    const emergencyState = require('../../services/emergency.state');
    const ridesRepo = require('../../repositories/rides.repository');
    const ride = await ridesRepo.findById(rideId);
    if (ride && ride.user_id === identity().id) {
      emergencyState.clearEmergency(ride.id);
      events.emitRideStatusUpdate({
        ride,
        stage: 'emergency_cleared',
        actor: { id: identity().id, role: 'user', name: identity().name },
      });
    }
    return { ok: true, rideId };
  });
};
