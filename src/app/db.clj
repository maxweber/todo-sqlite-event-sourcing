(ns app.db
  "Simple database connection management."
  (:require [clojure.java.io :as io]
            [next.jdbc :as jdbc]))

(defonce datasource
  (atom nil))

(def db-file "data/db.db")

(defn get-ds
  "Gets or creates the datasource."
  []
  (if-let [ds @datasource]
    ds
    (let [_ (io/make-parents db-file)
          ds (jdbc/get-datasource (str "jdbc:sqlite:" db-file))]
      (reset! datasource ds)
      ds)))
