-- V4: TV device module schema
CREATE SCHEMA IF NOT EXISTS tv;

CREATE TABLE tv.tv_users (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE tv.wifi_networks (
    id BIGSERIAL PRIMARY KEY,
    ssid VARCHAR(255) NOT NULL,
    bssid VARCHAR(255),
    user_id BIGINT NOT NULL REFERENCES tv.tv_users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE tv.connected_tvs (
    id BIGSERIAL PRIMARY KEY,
    device_name VARCHAR(255) NOT NULL,
    mac_address VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(255),
    wifi_network_id BIGINT NOT NULL REFERENCES tv.wifi_networks(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
