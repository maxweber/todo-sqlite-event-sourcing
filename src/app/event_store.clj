(ns app.event-store
  "Event store using SQLite via next.jdbc."
  (:require [clojure.edn :as edn]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn store-event!
  "Store an event. The connectable can be a datasource or transaction."
  [connectable event]
  (jdbc/execute! connectable
    ["INSERT INTO events (id, event_type, aggregate_type, aggregate_id, data, timestamp, tx_id, user_id, version)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
     (str (:event/id event))
     (pr-str (:event/type event))
     (pr-str (:event/aggregate event))
     (str (:event/aggregate-id event))
     (pr-str (:event/data event))
     (str (:event/timestamp event))
     (str (:event/tx-id event))
     (some-> (:event/user-id event) str)
     (or (:event/version event) 1)]))

(defn- parse-event
  "Parse a row map into an event map."
  [row]
  {:event/id (parse-uuid (:id row))
   :event/type (edn/read-string (:event_type row))
   :event/aggregate (edn/read-string (:aggregate_type row))
   :event/aggregate-id (parse-uuid (:aggregate_id row))
   :event/data (edn/read-string (:data row))
   :event/timestamp (java.time.Instant/parse (:timestamp row))
   :event/tx-id (parse-uuid (:tx_id row))
   :event/user-id (some-> (:user_id row) parse-uuid)
   :event/version (:version row)
   :event/sequence-num (:sequence_num row)})

(defn get-all-events
  "Get all events in order."
  [connectable]
  (mapv parse-event
        (jdbc/execute! connectable
          ["SELECT * FROM events ORDER BY sequence_num"]
          {:builder-fn rs/as-unqualified-maps})))

(defn get-events-for-aggregate
  "Get all events for a specific aggregate."
  [connectable aggregate-type aggregate-id]
  (mapv parse-event
        (jdbc/execute! connectable
          ["SELECT * FROM events WHERE aggregate_type = ? AND aggregate_id = ? ORDER BY sequence_num"
           (pr-str aggregate-type)
           (str aggregate-id)]
          {:builder-fn rs/as-unqualified-maps})))

(defn drop-todos-table!
  "Drop the todos table for replay. Used when rebuilding the read-model."
  [connectable]
  (jdbc/execute! connectable ["DROP TABLE IF EXISTS todos"]))
