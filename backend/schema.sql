CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE merchants (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE devices (
    id TEXT PRIMARY KEY,
    merchant_id TEXT NOT NULL REFERENCES merchants(id),
    api_token TEXT UNIQUE NOT NULL DEFAULT encode(gen_random_bytes(32), 'hex'),
    platform TEXT NOT NULL DEFAULT 'android',
    app_version TEXT,
    last_seen_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE suggestion_events (
    id BIGSERIAL PRIMARY KEY,
    device_id TEXT NOT NULL REFERENCES devices(id),
    merchant_id TEXT NOT NULL REFERENCES merchants(id),
    event_type TEXT NOT NULL CHECK (event_type IN ('shown', 'accepted', 'dismissed')),
    trigger_items TEXT[] NOT NULL,
    suggested_item TEXT NOT NULL,
    suggested_item_id TEXT,
    price_cents BIGINT DEFAULT 0,
    ai_model TEXT,
    response_time_ms INT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_events_merchant ON suggestion_events(merchant_id, created_at);
CREATE INDEX idx_events_device ON suggestion_events(device_id, created_at);

CREATE TABLE merchant_config (
    merchant_id TEXT PRIMARY KEY REFERENCES merchants(id),
    ai_model TEXT NOT NULL DEFAULT 'glm-4.7-flash',
    ai_base_url TEXT NOT NULL DEFAULT 'https://api.z.ai/api/paas/v4',
    ai_api_key TEXT,
    feature_flags JSONB NOT NULL DEFAULT '{}',
    system_prompt_override TEXT,
    max_requests_per_minute INT NOT NULL DEFAULT 30,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
