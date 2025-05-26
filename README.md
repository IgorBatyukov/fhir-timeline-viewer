## Development mode

```shell

docker compose up
```


Point your Clojure REPL to the `dev` module and you're ready to go!

### Database Management

The following commands are available in your REPL:

```clojure
;; Seed database with example FHIR entries
(seed-db)

;; Reset database, including migration history
;; Note: Backend needs to be restarted after this operation
(reset-db)
```

Navigate to `http://localhost:8080` in your browser