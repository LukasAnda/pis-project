# Read Me First
To successfully launch, enter this command in docker console
`docker-compose up --build`

Also, before you run, make sure that application.properties file is pointing the database to db:5432 instead of localhost:5432

Now you can find the app on https://localhost:8080

## Development mode
To run in development mode, open this project in IntelliJ IDEA, and run following command in docker console:
`docker-compose -f ./docker-compose-local.yml up --build` which will only deploy postgres database in container.

Now run wsdl2java gradle task to generate wsdl classes (this needs to be done only once) and then just run from FiitCompanyApplication.kt

To run in this configuration, make sure that application.properties file is pointing to localhost:5432 for database location instead of db:5432