(ns app.events.todo
  "Todo domain events - immutable facts about what happened."
  (:require [app.events.core :as events]))

(defn todo-created
  "Create a todo-created event."
  [id text]
  (events/make-event
   {:event/kind :todo/created
    :event/data {:id id
                 :text text}}))

(defn todo-completed
  "Create a todo-completed event."
  [id]
  (events/make-event
   {:event/kind :todo/completed
    :event/data {:id id}}))

(defn todo-uncompleted
  "Create a todo-uncompleted event."
  [id]
  (events/make-event
   {:event/kind :todo/uncompleted
    :event/data {:id id}}))

(defn todo-deleted
  "Create a todo-deleted event."
  [id]
  (events/make-event
   {:event/kind :todo/deleted
    :event/data {:id id}}))
