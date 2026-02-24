(ns app.projections.todo
  "Projections for todo events - transform events to honeysql statements.")

(defmulti project
  "Project an event to a honeysql statement."
  (fn [event] (:event/type event)))

(defmethod project :todo/created
  [event]
  (let [{:keys [id text]} (:event/data event)]
    [{:insert-into :todos
      :values [{:id (str id) :text text :completed 0}]}]))

(defmethod project :todo/completed
  [event]
  (let [{:keys [id]} (:event/data event)]
    [{:update :todos
      :set {:completed 1}
      :where [:= :id (str id)]}]))

(defmethod project :todo/uncompleted
  [event]
  (let [{:keys [id]} (:event/data event)]
    [{:update :todos
      :set {:completed 0}
      :where [:= :id (str id)]}]))

(defmethod project :todo/deleted
  [event]
  (let [{:keys [id]} (:event/data event)]
    [{:delete-from :todos
      :where [:= :id (str id)]}]))

(defmethod project :default
  [event]
  (println "Unknown event type for todo projection:" (:event/type event))
  [])
