(ns app.event-processor
  "Processes events: stores in SQLite and applies projections to the todos table.

   Both operations happen in the same SQLite transaction via jdbc/with-transaction."
  (:require [app.event-store :as store]
            [app.projections.core :as proj]
            [app.db-schema :as schema]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]))

(defn process-events!
  "Process events within a single SQLite transaction.

   Takes world-map w with :db/ds (datasource).
   Takes command-result which is {:ok events} or {:error msg}.

   Both event storage and projection happen in the same transaction,
   ensuring atomicity across the event store and read-model."
  [w command-result]
  (if-let [events (:ok command-result)]
    (let [tx-id (random-uuid)
          user-id nil
          events-with-meta (mapv #(assoc %
                                         :event/tx-id tx-id
                                         :event/user-id user-id)
                                 events)
          ds (:db/ds w)]

      (jdbc/with-transaction [tx ds]
        ;; 1. Store events
        (doseq [event events-with-meta]
          (store/store-event! tx event))

        ;; 2. Apply projections
        (let [stmts (vec (mapcat proj/apply-event events-with-meta))]
          (doseq [stmt stmts]
            (jdbc/execute! tx (sql/format stmt)))))

      ;; 3. Return success
      (assoc w
             :command/result {:success? true
                              :aggregate-id (:aggregate-id command-result)}
             :event-processor/events events-with-meta))

    ;; Error case - no events to process
    (assoc w
           :command/result {:success? false
                            :error (:error command-result)})))

(defn replay-all-events!
  "Rebuild todos read-model by replaying all events from SQLite.

   DESTRUCTIVE: Drops the todos table and rebuilds it from events.
   All operations happen in a single SQLite transaction.

   Returns {:replayed count}."
  [ds]
  (jdbc/with-transaction [tx ds]
    ;; Read events first
    (let [events (store/get-all-events tx)]

      ;; Drop and recreate todos table
      (store/drop-todos-table! tx)
      (jdbc/execute! tx ["
        CREATE TABLE IF NOT EXISTS todos (
          id TEXT PRIMARY KEY,
          text TEXT NOT NULL,
          completed INTEGER NOT NULL DEFAULT 0
        )"])

      ;; Replay each event
      (doseq [event events]
        (let [stmts (proj/apply-event event)]
          (doseq [stmt stmts]
            (jdbc/execute! tx (sql/format stmt)))))

      {:replayed (count events)})))
