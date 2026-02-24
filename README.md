# Todo app with SQLite event sourcing

A basic todo app that combines event sourcing with SQLite. Events are the source
of truth; the todos table is a read-model rebuilt from projections.

Mostly written by Claude Code.

## Why

Relational databases mix essential state with derived state. Imagine an Excel
spreadsheet where cells containing formulas do not update automatically, and
worse, they store the computed result instead of the formula itself. It's
immediately obvious that this is something you want to avoid.

Event sourcing separates the two. Events are immutable values, stored forever.
The read-model (todos table) is derived state. If you later discover that a
projection was wrong, you can delete the read-model and replay all events to
rebuild it, atomically, in a single SQLite transaction.

Events also force you to assign a meaning to what happened. Transactions in
relational databases can be fairly arbitrary. External event streams, such as
those from [a billing
provider](https://developer.paddle.com/api-reference/events/list-events), make
this especially clear: you can build your own read-model and keep it up to date
simply by applying new events as they arrive.

## Architecture

### Event store

A single SQLite table stores all events as EDN blobs. The schema is minimal
(`sequence_num`, `id`, `data`) and never needs migration — new event fields are
just EDN.

### Data-driven registration

Queries, commands, and projections are all registered as data maps in a single
register:

```clojure
;; in app.todo
(def register
  [{:query/kind     :query/todos      :query/fn #'query-todos}
   {:command/kind   :command/add-todo  :command/fn ...}
   {:projection/event-kind :todo/created  :projection/fn #'proj/todo-created}
   ...])
```

### Processing flow

1. Browser sends a command via `/command`
2. Command handler (pure function) returns `{:ok [events]}` or `{:error msg}`
3. In a single SQLite transaction: events are stored, then projections update the
   read-model
4. Queries read from the projected todos table

### Replay

Rebuild the read-model from scratch at the REPL:

```clojure
(app.event-processor/replay-all-events! (app.db/get-ds))
```

This drops the todos table and replays every event through the projections,
all in one transaction.

## Development

    bin/dev-start

Starts the JVM backend (nREPL on port 4000) and shadow-cljs (browser on port
3001).

## Licence

MIT
