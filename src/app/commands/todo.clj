(ns app.commands.todo
  "Todo commands - pure functions that validate and return events."
  (:require [app.events.todo :as events]
            [clojure.string :as str]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn- find-todo
  "Find a todo by id."
  [ds id]
  (jdbc/execute-one! ds
    ["SELECT * FROM todos WHERE id = ?" (str id)]
    {:builder-fn rs/as-unqualified-maps}))

(defn add-todo
  "Command to create a new todo.
   Pure function: reads from w, returns {:ok events} or {:error msg}."
  [w]
  (let [text (get-in w [:command :command/data :text])]
    (cond
      (or (nil? text) (str/blank? text))
      {:error "Text cannot be empty"}

      (> (count text) 500)
      {:error "Text cannot exceed 500 characters"}

      :else
      (let [id (random-uuid)]
        {:ok [(events/todo-created id (str/trim text))]
         :aggregate-id id}))))

(defn toggle-todo
  "Command to toggle a todo's completion status."
  [w]
  (let [id (get-in w [:command :command/data :id])
        ds (:db/ds w)
        todo (find-todo ds id)]
    (cond
      (nil? todo)
      {:error "Todo not found"}

      (= 1 (:completed todo))
      {:ok [(events/todo-uncompleted id)]
       :aggregate-id id}

      :else
      {:ok [(events/todo-completed id)]
       :aggregate-id id})))

(defn delete-todo
  "Command to delete a todo."
  [w]
  (let [id (get-in w [:command :command/data :id])
        ds (:db/ds w)
        todo (find-todo ds id)]
    (cond
      (nil? todo)
      {:error "Todo not found"}

      :else
      {:ok [(events/todo-deleted id)]
       :aggregate-id id})))
