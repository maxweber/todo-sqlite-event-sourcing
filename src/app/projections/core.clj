(ns app.projections.core
  "Core projection utilities - routes events to appropriate projections."
  (:require [app.projections.todo :as todo-proj]))

(defn apply-event
  "Route event to appropriate projection. Returns honeysql maps."
  [event]
  (case (namespace (:event/type event))
    "todo" (todo-proj/project event)
    (do
      (println "Unknown event type namespace:" (:event/type event))
      [])))
