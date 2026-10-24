-- ============================================================================
-- TBRide :: 003_add_ride_ratings.sql
-- Rider/driver ratings (1-5) on completed rides, plus optional comment.
-- ============================================================================

CREATE TABLE IF NOT EXISTS ride_ratings (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ride_id         UUID NOT NULL REFERENCES ride_requests(id) ON DELETE CASCADE,
  rater_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  rated_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  rating          INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
  comment         VARCHAR(500),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT ride_ratings_one_per_rater_ride UNIQUE (ride_id, rater_id)
);

CREATE INDEX IF NOT EXISTS idx_ride_ratings_ride_id ON ride_ratings(ride_id);
CREATE INDEX IF NOT EXISTS idx_ride_ratings_rated_id ON ride_ratings(rated_id);