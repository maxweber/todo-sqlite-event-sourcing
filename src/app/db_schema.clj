(ns app.db-schema
  (:require [next.jdbc :as jdbc]))

(defn ensure-schema!
  "Creates the events and todos tables if they don't exist."
  [ds]
  (jdbc/execute! ds ["
    CREATE TABLE IF NOT EXISTS events (
      sequence_num INTEGER PRIMARY KEY AUTOINCREMENT,
      id TEXT NOT NULL UNIQUE,
      event_type TEXT NOT NULL,
      aggregate_type TEXT NOT NULL,
      aggregate_id TEXT NOT NULL,
      data TEXT NOT NULL,
      timestamp TEXT NOT NULL,
      tx_id TEXT NOT NULL,
      user_id TEXT,
      version INTEGER NOT NULL DEFAULT 1
    )"])
  (jdbc/execute! ds ["
    CREATE INDEX IF NOT EXISTS idx_events_aggregate
    ON events(aggregate_type, aggregate_id)"])
  (jdbc/execute! ds ["
    CREATE INDEX IF NOT EXISTS idx_events_timestamp
    ON events(timestamp)"])
  (jdbc/execute! ds ["
    CREATE INDEX IF NOT EXISTS idx_events_tx
    ON events(tx_id)"])
  (jdbc/execute! ds ["
    CREATE TABLE IF NOT EXISTS todos (
      id TEXT PRIMARY KEY,
      text TEXT NOT NULL,
      completed INTEGER NOT NULL DEFAULT 0
    )"]))
