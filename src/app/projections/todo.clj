(ns app.projections.todo
  "Projections for todo events - transform events to honeysql statements.")

(defn todo-created
  [event]
  (let [{:keys [id text]} (:event/data event)]
    [{:insert-into :todos
      :values [{:id (str id) :text text :completed 0}]}]))

(defn todo-completed
  [event]
  (let [{:keys [id]} (:event/data event)]
    [{:update :todos
      :set {:completed 1}
      :where [:= :id (str id)]}]))

(defn todo-uncompleted
  [event]
  (let [{:keys [id]} (:event/data event)]
    [{:update :todos
      :set {:completed 0}
      :where [:= :id (str id)]}]))

(defn todo-deleted
  [event]
  (let [{:keys [id]} (:event/data event)]
    [{:delete-from :todos
      :where [:= :id (str id)]}]))
