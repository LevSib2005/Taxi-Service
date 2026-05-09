CREATE TABLE trips
(
    id           BIGSERIAL PRIMARY KEY,
    passenger_id BIGINT         NOT NULL,
    driver_id    BIGINT,
    status       VARCHAR(20)    NOT NULL DEFAULT 'CREATED',
    origin       VARCHAR(255)   NOT NULL,
    destination  VARCHAR(255)   NOT NULL,
    price        DOUBLE PRECISION,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_trip_status CHECK (
        status IN ('CREATED', 'ACCEPTED', 'STARTED', 'COMPLETED', 'CANCELLED')
    )
);

CREATE INDEX idx_trips_passenger_id ON trips (passenger_id);
CREATE INDEX idx_trips_driver_id ON trips (driver_id);
CREATE INDEX idx_trips_status ON trips (status);