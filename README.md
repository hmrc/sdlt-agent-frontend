
# sdlt-agent-frontend

This is the new sdlt-agent-frontend repository to maintain (create / update / delete) SDLT purchaser agents

For more information please refer to the [documentation](https://confluence.tools.tax.service.gov.uk/spaces/RBD/pages/1081606211/3.+Stamp+Duty+Land+Tax+-+SDLT).

## Running the service
Before starting, you will need to have  [service-manager](https://github.com/hmrc/service-manager) installed/configured

### Dependencies
All dependencies can be found in [AppDependencies.scala](https://github.com/hmrc/sdlt-agent-frontend/blob/main/project/AppDependencies.scala)

### Running locally:
Service Manager:
- Start dependent services `sm2 --start SDLT_ALL`
- Stop this service `sm2 --stop  SDLT-AGENT-FRONTEND`
- Start the server locally on `port 10911` with `sbt run`
- 
### Testing:
- Run unit tests: `sbt test`
- Run integration tests: `sbt it/test`
- To run all tests and coverage: `sbt clean compile coverage test it/test coverageOff coverageReport`
- To run the service in test-only mode: `sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`. This allows access to the following test routes:
    ```
    /stamp-duty-land-tax-agent/manage-agents/test-only/session/set
    /stamp-duty-land-tax-agent/manage-agents/test-only/session/clear
    ```

## Adding New Pages

### Folder Structure
The project uses domain-based organisation. Each new page should be placed in the appropriate domain folder:

```
app/
├── controllers/[domain]/               # e.g. controllers/manageAgents
├── models/[domain]/                    # e.g. models/manageAgents
├── views/[domain]/                     # e.g. views/manageAgents
├── forms/[domain]/                     # e.g. forms/manageAgents
├── pages/[domain]/                     # e.g. pages/manageAgents
└── viewmodels/checkAnswers/[domain]/   # e.g. viewmodels/checkAnswers/manageAgents
```

```
test/
├── controllers/[domain]/   # e.g. controllers/manageAgents
├── models/[domain]/        # e.g. models/manageAgents
├── forms/[domain]/         # e.g. forms/manageAgents
└── views/[domain]/         # e.g. views/manageAgents
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").