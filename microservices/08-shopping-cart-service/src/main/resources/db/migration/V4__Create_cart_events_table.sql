-- V4__Create_cart_events_table.sql
CREATE TABLE cart_events (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    cart_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer_id UUID,
    session_id VARCHAR(255)
);

-- Create indexes
CREATE INDEX idx_cart_events_cart_id ON cart_events(cart_id);
CREATE INDEX idx_cart_events_event_type ON cart_events(event_type);
CREATE INDEX idx_cart_events_occurred_at ON cart_events(occurred_at);
CREATE INDEX idx_cart_events_customer_id ON cart_events(customer_id);

-- JSON index for PostgreSQL
CREATE INDEX idx_cart_events_event_data_gin ON cart_events USING GIN (event_data);
