CREATE TABLE notification_tasks (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    recipient_type VARCHAR(50) NOT NULL,
    recipient_id BIGINT NOT NULL,
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_status ON notification_tasks(status);
CREATE INDEX idx_trip_id ON notification_tasks(trip_id);