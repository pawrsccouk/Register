# Register

This is a test project to create a registration webserver using Ring and Clojure.

Currently it handles pushing data to and from a table in a MySQL database, with HTTP basic-authentication.
I intend to advance it until it can automatically email registered users (i.e. those on the table) and
invite them to events they have signed up for. If they click on a link in the email then they will be
unregistered for that event.

## Installation

Download from GitHub, set up a MySQL database and set the following environment variables:

```
REGISTER_DB_USER=<your MySQL username>

REGISTER_DB_PASSWORD=<your MySQL password>

REGISTER_DB_INFO=<JDBC info for the current database>
```

The DB info is of the form "//hostname:port/register"

## Usage

Start a repl and run

```clojure
(def srvr (main))
```

This will automatically start a running instance and return control to the REPL.
Stop it with

```clojure
(.stop srvr)
```

## Options

See Installation.

## Bugs

Uses only HTTP basic-authentication, so sends the user's login info as part of the
HTTP session information. I need to encode this as well.


## License

Copyright © 2016 Patrick A Wallace

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
