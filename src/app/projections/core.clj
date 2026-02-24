(ns app.projections.core
  "Core projection utilities - routes events to appropriate projections.")

(defn build-projection-lookup
  "Takes a register (seq of maps), returns {event-kind -> projection-fn}."
  [register]
  (->> register
       (filter :projection/event-kind)
       (reduce (fn [m entry]
                 (assoc m (:projection/event-kind entry) (:projection/fn entry)))
               {})))

(defn apply-event
  "Route event to appropriate projection. Returns honeysql maps."
  [projection-lookup event]
  (if-let [f (get projection-lookup (:event/kind event))]
    (f event)
    (do
      (println "Unknown event kind for projection:" (:event/kind event))
      [])))
