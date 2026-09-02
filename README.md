
# sdlt-management-frontend

This is the landing point of the SDLT service providing access to service functionality

For more information please refer to the [documentation](https://confluence.tools.tax.service.gov.uk/spaces/RBD/pages/1081606211/3.+Stamp+Duty+Land+Tax+-+SDLT).

## Running the service
Before starting, you will need to have  [service-manager](https://github.com/hmrc/service-manager) installed/configured

### Dependencies
All dependencies can be found in [AppDependencies.scala](https://github.com/hmrc/sdlt-management-frontend/blob/main/project/AppDependencies.scala)
Service Manager: `sm2 --start SDLT_ALL`

### Running locally:
Service Manager:
- Start dependent services `sm2 --start SDLT_ALL`
- Stop this service `sm2 --stop SDLT-MANAGEMENT-FRONTEND`
- Start the server locally on `port 10912` with `sbt run`

### Testing:
- Run unit tests: `sbt test`
- Run integration tests: `sbt it/test`
- To run all tests and coverage: `sbt clean compile coverage test it/test coverageOff coverageReport`
- To run the service in test-only mode: `sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`

## Adding New Pages

### Folder Structure
The project uses domain-based organisation. Each new page should be placed in the appropriate domain folder:

```
app/
├── controllers/[domain]/               # e.g. controllers/manage
├── models/[domain]/                    # e.g. models/manage
├── views/[domain]/                     # e.g. views/manage
├── forms/[domain]/                     # e.g. forms/manage
├── pages/[domain]/                     # e.g. pages/manage
└── viewmodels/checkAnswers/[domain]/   # e.g. viewmodels/checkAnswers/manage
```

```
test/
├── controllers/[domain]/   # e.g. controllers/manage
├── models/[domain]/        # e.g. models/manage
├── forms/[domain]/         # e.g. forms/manage
└── views/[domain]/         # e.g. views/manage
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").