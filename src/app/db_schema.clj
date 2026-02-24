(ns app.db-schema
  (:require [next.jdbc :as jdbc]))

(defn ensure-schema!
  "Creates the events and todos tables if they don't exist."
  [ds]
  ;; Minimal schema: only ordering, dedup, and the EDN blob.
  ;; All other event fields live inside `data`, so this table
  ;; never needs a migration — new event fields are just EDN.
  (jdbc/execute! ds ["
    CREATE TABLE IF NOT EXISTS events (
      sequence_num INTEGER PRIMARY KEY AUTOINCREMENT,
      id TEXT NOT NULL UNIQUE,
      data TEXT NOT NULL
    )"])
  (jdbc/execute! ds ["
    CREATE TABLE IF NOT EXISTS todos (
      id TEXT PRIMARY KEY,
      text TEXT NOT NULL,
      completed INTEGER NOT NULL DEFAULT 0
    )"]))
