
#### General

Application is a simple web server providing RESTful API to create bank accounts, deposit money and transfer money between accounts.

To build the app, run `mvn clean package`. This will run all the tests and package application into a single jar.
To run it after the build, run `java -jar mt-webapp/target/money-transfer-webapp-1.0-SNAPSHOT.jar`

You could use the following `curl` commands to play with the app, `<id>` has to correspond to uuid of an account:
- create account `curl -X POST http://localhost:8080/account`
- deposit money `curl -X POST --data 'amount=200' http://localhost:8080/account/<id>`
- transfer money `curl -X POST --data 'amount=100' --data 'to=<id>' http://localhost:8080/account`
- get all accounts `curl -X GET http://localhost:8080/account`



#### Architecture

Web server is based on JettyServer.

The application is written as a multimodule maven project, it consists of three modules:
- core - business functionality with objects like Bank, Account, etc.
- persistence - engine based on SQL database with objects like SqlStorage, TransactionalOperation, etc.
- webapp - packaging capabilities of everything into the application

Persistence is based on H2, where everything is kept in memory.
Flyway is used to simplify database initialization and possible evolution.
