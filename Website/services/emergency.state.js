'use strict';

/**
 * TBRide :: Active Emergency SOS State.
 * Tracks emergency alerts triggered by riders (e.g. from the Telegram bot).
 */

const activeEmergencies = new Map(); // rideId -> { rideId, userId, telegramUserId, riderName, assignedDriverId, location, note, triggeredAt }

function setEmergency(rideId, details) {
  if (!rideId) return;
  activeEmergencies.set(String(rideId), {
    rideId: String(rideId),
    ...details,
    triggeredAt: details.triggeredAt || new Date().toISOString(),
  });
}

function getEmergency(rideId) {
  return activeEmergencies.get(String(rideId)) || null;
}

function hasEmergency(rideId) {
  return activeEmergencies.has(String(rideId));
}

function clearEmergency(rideId) {
  return activeEmergencies.delete(String(rideId));
}

function listActiveEmergencies() {
  return Array.from(activeEmergencies.values());
}

module.exports = {
  setEmergency,
  getEmergency,
  hasEmergency,
  clearEmergency,
  listActiveEmergencies,
};
