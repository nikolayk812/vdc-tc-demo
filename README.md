# TestContainers demo

There are 3 services in this demo:

- User service
- Item service
- Eureka

*User service* uses *Postgres* and *Item service* uses *Redis* respectively and has dependency on User service. Each service has a SpringBootTest which uses non-mocked DB started by *TestContainers* library.  

*Eureka* is a service registry for *User* and *Item* services to register at start up.

*test-runner* module is an example of a framework to start all 3 services and their dependencies  together in order to perform end-to-end tests.

## Stack

- Spring Boot
- Spring Cloud Netflix Eureka
- Docker
- Postgres, Redis
- TestContainers
- JUnit 5

## Setup

To build all services in Docker:
```
sh build-all.sh
```

To run end-to-end tests:
```
cd test-runner
mvn test
```


## Usage examples

Http calls examples could be found in ./requests.http