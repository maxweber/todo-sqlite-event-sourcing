(ns app.events.core
  "Core event utilities - creating and enriching events.

   Events have no aggregate identity. Following Dynamic Consistency
   Boundaries (DCB), consistency boundaries are defined by command
   handlers at decision time, not baked into events. Entity identity
   lives in :event/data as domain data.")
(defn make-event
  "Create an event with required metadata.
   Takes a map with :event/kind and :event/data."
  [{:keys [event/kind event/data]}]
  {:event/id (random-uuid)
   :event/kind kind
   :event/data data
   :event/timestamp (java.util.Date.)
   :event/version 1})
