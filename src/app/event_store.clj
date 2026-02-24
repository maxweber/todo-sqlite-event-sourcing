(ns app.event-store
  "Event store using SQLite via next.jdbc.

   Events are stored as EDN blobs. The table only has sequence_num (ordering),
   id (dedup), and data (the full event as EDN). Any queryable indexes on
   event fields would be separate read-model projections."
  (:require [clojure.edn :as edn]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn store-event!
  "Store an event. The connectable can be a datasource or transaction."
  [connectable event]
  (jdbc/execute! connectable
    ["INSERT INTO events (id, data) VALUES (?, ?)"
     (str (:event/id event))
     (pr-str event)]))

(defn- parse-event
  "Parse a row into an event map."
  [row]
  (let [event (edn/read-string (:data row))]
    (assoc event :event/sequence-num (:sequence_num row))))

(defn get-all-events
  "Get all events in order."
  [connectable]
  (mapv parse-event
        (jdbc/execute! connectable
          ["SELECT * FROM events ORDER BY sequence_num"]
          {:builder-fn rs/as-unqualified-maps})))

(defn drop-todos-table!
  "Drop the todos table for replay. Used when rebuilding the read-model."
  [connectable]
  (jdbc/execute! connectable ["DROP TABLE IF EXISTS todos"]))
