# Api Platform Events With Mongodb

[ ![Download](https://api.bintray.com/packages/hmrc/releases/api-platform-events/images/download.svg) ](https://bintray.com/hmrc/releases/api-platform-events/_latestVersion)

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the app locally

    sm --start AGENTS_STUBS New_Shiny_Service -f
    sm --stop New_Shiny_Service
    sbt run

It should then be listening on port 6700

    browse http://localhost:6700/api-platform-events

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
