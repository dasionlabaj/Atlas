CREATE TABLE IF NOT EXISTS observations (
 id TEXT PRIMARY KEY,
 source TEXT NOT NULL,
 kind TEXT NOT NULL,
 payload TEXT NOT NULL,
 created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now'))
);
CREATE INDEX IF NOT EXISTS idx_observations_kind_created ON observations (kind, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_observations_created ON observations (created_at DESC);
