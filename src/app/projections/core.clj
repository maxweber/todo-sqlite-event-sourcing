(ns app.projections.core
  "Core projection utilities - routes events to appropriate projections.")

(defn build-projection-lookup
  "Takes a register (seq of maps), returns {event-type -> projection-fn}."
  [register]
  (->> register
       (filter :projection/event-type)
       (reduce (fn [m entry]
                 (assoc m (:projection/event-type entry) (:projection/fn entry)))
               {})))

(defn apply-event
  "Route event to appropriate projection. Returns honeysql maps."
  [projection-lookup event]
  (if-let [f (get projection-lookup (:event/type event))]
    (f event)
    (do
      (println "Unknown event type for projection:" (:event/type event))
      [])))
