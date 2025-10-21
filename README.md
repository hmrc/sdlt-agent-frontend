
# sdlt-agent-frontend

This is the new sdlt-agent-frontend repository

## Running the service

Service Manager: `sm2 --start SDLT_ALL`

To run all tests and coverage: `sbt clean compile coverage test coverageOff coverageReport`

To start the server locally on `port 10911`: `sbt run`

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