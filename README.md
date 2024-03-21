# TUD TAS Backend

The TUD TAS Backend is a software component that acts as an adapter between the TUD Assistance Backbone and the Learning
Management System ILIAS. Together with the TUD Assistance Backbone and the assistance components integrated in the LMS,
it forms the Tutorial Assistance System (TAS).

## Requirements

To be able to run this project locally, the following requirements have to be fulfilled:

* JDK 17 or higher
* Maven 3 or higher
* Docker
* Docker Compose
* A running MongoDB, which can be started be executing

```bash
docker compose up -d mongo && docker compose up -d mongo_init
```

## Build Project

Package application by running:

```bash
mvn package
```

Build Docker image by running:

```bash
mvn clean install -P docker -DskipTests
```

By default, the current Maven project version is used as the Docker tag. To build the Docker image with a custom
tag `<TAG>` run:

```bash
mvn clean install -Pdocker -DdockerImageTag=<TAG> -DskipTests
```

## Run Project

### Environment Variables

The application must be configured using environment variables before start:

| Name                              | Description                                                                                                                                                | Default value |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| CORS_ALLOWED_ORIGINS              | A list of origins for which cross-origin requests are allowed from a browser, separated with comma. Example: `http://localhost:3000,http://localhost:3001` |               |
| JWT_SECRET_KEY                    | Any secure key used to sign/verify the JWT tokens. Must be at least 32 characters long. Example: `4bA712yCNHaPFpGrI7148v1X0DjCeOF9`                        |               |
| LL_PASSWORD                       | The password to authenticate against the Learning Locker to send statements to.                                                                            |               |
| LL_URL                            | The Learning Locker URL for sending statements.                                                                                                            |               |
| LL_USERNAME                       | The username to authenticate against the Learning Locker to send statements to.                                                                            |               |
| LOG_PATH                          | The path were to store the learning record log file.                                                                                                       |               |
| MONGODB_BACKEND_DATABASE          | The name of the database to use.                                                                                                                           |               |
| MONGODB_BACKEND_HOST              | Host of the MongoDB instance to use.                                                                                                                       |               |
| MONGODB_BACKEND_PORT              | Port of the MongoDB instance to use.                                                                                                                       | 27017         |
| STATEMENT_SENDER_USERNAME         | Any username that must be used in the Authorization header for POST requests to `/statements`.                                                             |               |
| STATEMENT_SENDER_PASSWORD         | Any password that must be used in the Authorization header for POST requests to `/statements`.                                                             |               |
| STOMP_INCOMING_HEARTBEAT_INTERVAL | Desired interval for heartbeats to receive in milliseconds.                                                                                                | 0             |
| STOMP_OUTGOING_HEARTBEAT_INTERVAL | Intended interval for STOMP heartbeats to send in milliseconds.                                                                                            | 0             |
| SWAGGER_SERVER_URL                | The URL to set in the swagger documentation.                                                                                                               |               |
| TUD_ASSISTANCE_BACKBONE_URL       | URL of the TUD Assistance Backbone to which requests for assistance, feedback and suggestions are sent.                                                    |               |

When developing locally, it may be helpful to copy the `example.env` file, rename it to `ttb.env` and adjust the values
of the environment variables in it. When starting the application, the environment variables from the `ttb.env` file are
automatically taken into account. Using IntelliJ or any other IDE to execute the `BackendApplication` the `LOG_PATH`
variable can not be loaded from the `ttb.env` file but has to be added as an environment variable manually.

### Run Project using Maven

Run application by executing:

```bash
mvn spring-boot:run
```

### Run Project using Docker

Run Docker container after Docker image was built using Docker by executing:

```bash
docker run --rm --name tas-backend -p 8080:8080 registry-1.docker.io/tud/tas-backend:<TAG>
```

### Run Project using Docker Compose

To run the Docker-Compose-setup the following variables have to be defined:

* `BACKEND_DOCKER_TAG` - Specifies the tag of the Docker image which should be used, e.g., `latest`

These variables can also be defined in a `.env` file located in the root directory, afterward the Docker-Compose-setup
can be started by executing:

```bash
docker-compose up # -d
```

## Usage

After the application was started locally it is accessible under `http://localhost:8080`.

### OpenAPI

The OpenAPI 3.0 specification of the REST API is automatically generated by the application based on the current code
and can be accessed after starting the application under the following paths: `/v3/api-docs` (JSON)
and `/v3/api-docs.yaml` (YAML).

Additionally, this auto-generated Open API 3.0 specification can be visualized using Swagger
UI: `/swagger-ui/index.html`.

### WebSocket example

The STOMP messaging protocol is used to extend WebSockets communication. Client libraries exist for the implementation
in frontends. However, since not all functionalities are important and not the entire range of features is used,
communication can also be implemented directly with the WebSocket API in a simpler way:

```javascript
// in production, the encrypted protocol wss:// should be used
const webSocket = new WebSocket('ws://localhost:8080/api/v1/websocket');

webSocket.onopen = (event) => {
    webSocket.send("CONNECT\ntoken:" + jwtToken + "\naccept-version:1.2\n\n\0");
    // the client should always be subscribed to: /user/queue/chat/0
    // and other queues depending on current page
    webSocket.send("SUBSCRIBE\nid:sub-0\ndestination:/user/queue/chat/0\n\n\0");
};

webSocket.onmessage = (event) => {
    console.log(event);
    // extract content between \n\n and \0
    const body = event.data.substring(event.data.indexOf('\n\n') + 2, event.data.lastIndexOf("\0"));
    // send JSON data in body of STOMP messages that can be deserialized
    console.log(JSON.parse(body));
};
```

The websocket path used for subscribing and sending messages contains the contextId (e.g the ID of the current course). ContextId `0` and the respective path `/user/queue/chat/0` is considered a general message queue the client should always be subscribed to. It is used for messages that do not belong to any specific context.

A message exchange could look like this, for example:

* client → backend
  ```
  CONNECT
  token:eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzZWxmIiwic3ViIjoiOWNjZDcxNmUtYjEwZi00MDMxLWJiZjQtZTI3NDFkNGZlOGIzIiwiZXhwIjoxNjcwNjc0MDAxLCJpYXQiOjE2NzA2NzA0MDEsInJvbGVzIjpbIlNUVURFTlQiXX0.e8p4gafk1oqmsJGMa41QC1dQUDKQaXOSgyB5er_OruU
  accept-version:1.2


  ```
* client → backend
  ```
  SUBSCRIBE
  id:sub-0
  destination:/user/queue/chat/0


  ```
* client → backend
  ```
  SEND
  destination:/user/queue/chat/0
  content-length:77

  {"aId":"","parameters":[{"key":"message_response","value":"Anybody there?"}]}

  ```
* backend → client
  ```
  CONNECTED
  version:1.2
  heart-beat:0,0
  user-name:9ccd716e-b10f-4031-bbf4-e2741d4fe8b3


  ```
* backend → client
  ```
  MESSAGE
  destination:/user/queue/chat/0
  content-type:application/json
  subscription:sub-0
  message-id:33db0a0a-7420-360e-7fda-0ac1ac691016-0
  content-length:69

  {"order":"some string","msg":"This is sample message 1 to a client."}
  ```
* backend → client
  ```
  MESSAGE
  destination:/user/queue/chat/23
  content-type:application/json
  subscription:sub-0
  message-id:33db0a0a-7420-360e-7fda-0ac1ac691016-1
  content-length:69

  {"order":"some string","msg":"This is sample message 2 to a client on contextId 23."}
  ```

## License

This plugin is licensed under the GPL v3 License (for further information, see [LICENSE](LICENSE)).

## Libraries used

The libraries used in this project are listed in the following table. This information can also be requested by:

```bash
mvn license:aggregate-third-party-report
```

After that the list of libraries used can be found in `target/site/aggregate-third-party-report.html`.

| GroupId:ArtifactId:Version                                                                                                                              | Scope    | Type   | License(s)                                                |
|---------------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------|-----------------------------------------------------------|
| [com.fasterxml:classmate:1.5.1](#com.fasterxml:classmate:1.5.1)                                                                                         | compile  | bundle | Apache License, Version 2.0                               |
| [com.fasterxml.jackson.core:jackson-annotations:2.13.4](#com.fasterxml.jackson.core:jackson-annotations:2.13.4)                                         | compile  | bundle | The Apache Software License, Version 2.0                  |
| [com.fasterxml.jackson.core:jackson-core:2.13.4](#com.fasterxml.jackson.core:jackson-core:2.13.4)                                                       | compile  | bundle | The Apache Software License, Version 2.0                  |
| [com.fasterxml.jackson.core:jackson-databind:2.13.4.2](#com.fasterxml.jackson.core:jackson-databind:2.13.4.2)                                           | compile  | bundle | The Apache Software License, Version 2.0                  |
| [com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.4](#com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.4)                     | compile  | bundle | The Apache Software License, Version 2.0                  |
| [com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.4](#com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.4)                             | compile  | bundle | The Apache Software License, Version 2.0                  |
| [com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2](#com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2)                         | compile  | bundle | The Apache Software License, Version 2.0                  |
| [com.fasterxml.jackson.module:jackson-module-parameter-names:2.13.4](#com.fasterxml.jackson.module:jackson-module-parameter-names:2.13.4)               | compile  | bundle | The Apache Software License, Version 2.0                  |
| [com.github.stephenc.jcip:jcip-annotations:1.0-1](#com.github.stephenc.jcip:jcip-annotations:1.0-1)                                                     | compile  | jar    | Apache License, Version 2.0                               |
| [com.google.code.findbugs:jsr305:3.0.2](#com.google.code.findbugs:jsr305:3.0.2)                                                                         | compile  | jar    | The Apache Software License, Version 2.0                  |
| [com.google.code.gson:gson:2.9.1](#com.google.code.gson:gson:2.9.1)                                                                                     | compile  | jar    | Apache-2.0                                                |
| [com.jayway.jsonpath:json-path:2.7.0](#com.jayway.jsonpath:json-path:2.7.0)                                                                             | test     | jar    | The Apache Software License, Version 2.0                  |
| [com.nimbusds:nimbus-jose-jwt:9.22](#com.nimbusds:nimbus-jose-jwt:9.22)                                                                                 | compile  | jar    | The Apache Software License, Version 2.0                  |
| [com.savoirtech.logging:slf4j-json-logger:2.0.2](#com.savoirtech.logging:slf4j-json-logger:2.0.2)                                                       | compile  | bundle | Apache 2                                                  |
| [com.vaadin.external.google:android-json:0.0.20131108.vaadin1](#com.vaadin.external.google:android-json:0.0.20131108.vaadin1)                           | test     | jar    | Apache License 2.0                                        |
| [de.tu.dresden.verdatas:learning-locker-api-spec:1.0.0](#de.tu.dresden.verdatas:learning-locker-api-spec:1.0.0)                                         | compile  | jar    | Apache License, Version 2.0                               |
| [de.tu.dresden.verdatas:tud-assistance-backbone-api-spec:1.0.0](#de.tu.dresden.verdatas:tud-assistance-backbone-api-spec:1.0.0)                         | compile  | jar    | Apache License, Version 2.0                               |
| [io.github.classgraph:classgraph:4.8.149](#io.github.classgraph:classgraph:4.8.149)                                                                     | compile  | jar    | The MIT License (MIT)                                     |
| [io.micrometer:micrometer-core:1.9.6](#io.micrometer:micrometer-core:1.9.6)                                                                             | compile  | jar    | The Apache Software License, Version 2.0                  |
| [io.netty:netty-buffer:4.1.85.Final](#io.netty:netty-buffer:4.1.85.Final)                                                                               | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-codec:4.1.85.Final](#io.netty:netty-codec:4.1.85.Final)                                                                                 | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-codec-dns:4.1.85.Final](#io.netty:netty-codec-dns:4.1.85.Final)                                                                         | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-codec-http:4.1.85.Final](#io.netty:netty-codec-http:4.1.85.Final)                                                                       | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-codec-http2:4.1.85.Final](#io.netty:netty-codec-http2:4.1.85.Final)                                                                     | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-codec-socks:4.1.85.Final](#io.netty:netty-codec-socks:4.1.85.Final)                                                                     | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-common:4.1.85.Final](#io.netty:netty-common:4.1.85.Final)                                                                               | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-handler:4.1.85.Final](#io.netty:netty-handler:4.1.85.Final)                                                                             | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-handler-proxy:4.1.85.Final](#io.netty:netty-handler-proxy:4.1.85.Final)                                                                 | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-resolver:4.1.85.Final](#io.netty:netty-resolver:4.1.85.Final)                                                                           | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-resolver-dns:4.1.85.Final](#io.netty:netty-resolver-dns:4.1.85.Final)                                                                   | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-resolver-dns-classes-macos:4.1.85.Final](#io.netty:netty-resolver-dns-classes-macos:4.1.85.Final)                                       | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-resolver-dns-native-macos:4.1.85.Final](#io.netty:netty-resolver-dns-native-macos:4.1.85.Final)                                         | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-transport:4.1.85.Final](#io.netty:netty-transport:4.1.85.Final)                                                                         | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-transport-classes-epoll:4.1.85.Final](#io.netty:netty-transport-classes-epoll:4.1.85.Final)                                             | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-transport-native-epoll:4.1.85.Final](#io.netty:netty-transport-native-epoll:4.1.85.Final)                                               | compile  | jar    | Apache License, Version 2.0                               |
| [io.netty:netty-transport-native-unix-common:4.1.85.Final](#io.netty:netty-transport-native-unix-common:4.1.85.Final)                                   | compile  | jar    | Apache License, Version 2.0                               |
| [io.projectreactor:reactor-core:3.4.25](#io.projectreactor:reactor-core:3.4.25)                                                                         | compile  | jar    | Apache License, Version 2.0                               |
| [io.projectreactor.netty:reactor-netty-core:1.0.25](#io.projectreactor.netty:reactor-netty-core:1.0.25)                                                 | compile  | jar    | The Apache Software License, Version 2.0                  |
| [io.projectreactor.netty:reactor-netty-http:1.0.25](#io.projectreactor.netty:reactor-netty-http:1.0.25)                                                 | compile  | jar    | The Apache Software License, Version 2.0                  |
| [io.swagger.core.v3:swagger-annotations:2.2.7](#io.swagger.core.v3:swagger-annotations:2.2.7)                                                           | compile  | jar    | Apache License 2.0                                        |
| [io.swagger.core.v3:swagger-core:2.2.7](#io.swagger.core.v3:swagger-core:2.2.7)                                                                         | compile  | jar    | Apache License 2.0                                        |
| [io.swagger.core.v3:swagger-models:2.2.7](#io.swagger.core.v3:swagger-models:2.2.7)                                                                     | compile  | jar    | Apache License 2.0                                        |
| [jakarta.activation:jakarta.activation-api:1.2.2](#jakarta.activation:jakarta.activation-api:1.2.2)                                                     | compile  | jar    | EDL 1.0                                                   |
| [jakarta.annotation:jakarta.annotation-api:1.3.5](#jakarta.annotation:jakarta.annotation-api:1.3.5)                                                     | compile  | jar    | EPL 2.0  <br>GPL2 w/ CPE                                  |
| [jakarta.validation:jakarta.validation-api:2.0.2](#jakarta.validation:jakarta.validation-api:2.0.2)                                                     | compile  | jar    | Apache License 2.0                                        |
| [jakarta.xml.bind:jakarta.xml.bind-api:2.3.3](#jakarta.xml.bind:jakarta.xml.bind-api:2.3.3)                                                             | compile  | jar    | Eclipse Distribution License - v 1.0                      |
| [junit:junit:4.13.2](#junit:junit:4.13.2)                                                                                                               | test     | jar    | Eclipse Public License 1.0                                |
| [net.bytebuddy:byte-buddy:1.12.19](#net.bytebuddy:byte-buddy:1.12.19)                                                                                   | test     | jar    | Apache License, Version 2.0                               |
| [net.bytebuddy:byte-buddy-agent:1.12.19](#net.bytebuddy:byte-buddy-agent:1.12.19)                                                                       | test     | jar    | Apache License, Version 2.0                               |
| [net.minidev:accessors-smart:2.4.8](#net.minidev:accessors-smart:2.4.8)                                                                                 | test     | bundle | The Apache Software License, Version 2.0                  |
| [net.minidev:json-smart:2.4.8](#net.minidev:json-smart:2.4.8)                                                                                           | test     | bundle | The Apache Software License, Version 2.0                  |
| [org.apache.commons:commons-lang3:3.12.0](#org.apache.commons:commons-lang3:3.12.0)                                                                     | compile  | jar    | Apache License, Version 2.0                               |
| [org.apache.logging.log4j:log4j-api:2.17.2](#org.apache.logging.log4j:log4j-api:2.17.2)                                                                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.apache.logging.log4j:log4j-core:2.17.2](#org.apache.logging.log4j:log4j-core:2.17.2)                                                               | compile  | jar    | Apache License, Version 2.0                               |
| [org.apache.logging.log4j:log4j-jul:2.17.2](#org.apache.logging.log4j:log4j-jul:2.17.2)                                                                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.apache.logging.log4j:log4j-slf4j-impl:2.17.2](#org.apache.logging.log4j:log4j-slf4j-impl:2.17.2)                                                   | compile  | jar    | Apache License, Version 2.0                               |
| [org.apache.tomcat.embed:tomcat-embed-core:9.0.69](#org.apache.tomcat.embed:tomcat-embed-core:9.0.69)                                                   | compile  | jar    | Apache License, Version 2.0                               |
| [org.apache.tomcat.embed:tomcat-embed-el:9.0.69](#org.apache.tomcat.embed:tomcat-embed-el:9.0.69)                                                       | compile  | jar    | Apache License, Version 2.0                               |
| [org.apache.tomcat.embed:tomcat-embed-websocket:9.0.69](#org.apache.tomcat.embed:tomcat-embed-websocket:9.0.69)                                         | compile  | jar    | Apache License, Version 2.0                               |
| [org.apiguardian:apiguardian-api:1.1.2](#org.apiguardian:apiguardian-api:1.1.2)                                                                         | test     | jar    | The Apache License, Version 2.0                           |
| [org.assertj:assertj-core:3.22.0](#org.assertj:assertj-core:3.22.0)                                                                                     | test     | jar    | Apache License, Version 2.0                               |
| [org.bouncycastle:bcpkix-jdk15on:1.70](#org.bouncycastle:bcpkix-jdk15on:1.70)                                                                           | compile  | jar    | Bouncy Castle Licence                                     |
| [org.bouncycastle:bcprov-jdk15on:1.70](#org.bouncycastle:bcprov-jdk15on:1.70)                                                                           | compile  | jar    | Bouncy Castle Licence                                     |
| [org.bouncycastle:bcutil-jdk15on:1.70](#org.bouncycastle:bcutil-jdk15on:1.70)                                                                           | compile  | jar    | Bouncy Castle Licence                                     |
| [org.hamcrest:hamcrest:2.2](#org.hamcrest:hamcrest:2.2)                                                                                                 | test     | jar    | BSD License 3                                             |
| [org.hamcrest:hamcrest-core:2.2](#org.hamcrest:hamcrest-core:2.2)                                                                                       | test     | jar    | BSD License 3                                             |
| [org.hdrhistogram:HdrHistogram:2.1.12](#org.hdrhistogram:HdrHistogram:2.1.12)                                                                           | compile  | bundle | BSD-2-Clause  <br>Public Domain, per Creative Commons CC0 |
| [org.hibernate.validator:hibernate-validator:6.2.5.Final](#org.hibernate.validator:hibernate-validator:6.2.5.Final)                                     | compile  | jar    | Apache License 2.0                                        |
| [org.jboss.logging:jboss-logging:3.4.3.Final](#org.jboss.logging:jboss-logging:3.4.3.Final)                                                             | compile  | jar    | Apache License, version 2.0                               |
| [org.junit.jupiter:junit-jupiter:5.8.2](#org.junit.jupiter:junit-jupiter:5.8.2)                                                                         | test     | jar    | Eclipse Public License v2.0                               |
| [org.junit.jupiter:junit-jupiter-api:5.8.2](#org.junit.jupiter:junit-jupiter-api:5.8.2)                                                                 | test     | jar    | Eclipse Public License v2.0                               |
| [org.junit.jupiter:junit-jupiter-engine:5.8.2](#org.junit.jupiter:junit-jupiter-engine:5.8.2)                                                           | test     | jar    | Eclipse Public License v2.0                               |
| [org.junit.jupiter:junit-jupiter-params:5.8.2](#org.junit.jupiter:junit-jupiter-params:5.8.2)                                                           | test     | jar    | Eclipse Public License v2.0                               |
| [org.junit.platform:junit-platform-commons:1.8.2](#org.junit.platform:junit-platform-commons:1.8.2)                                                     | test     | jar    | Eclipse Public License v2.0                               |
| [org.junit.platform:junit-platform-engine:1.8.2](#org.junit.platform:junit-platform-engine:1.8.2)                                                       | test     | jar    | Eclipse Public License v2.0                               |
| [org.latencyutils:LatencyUtils:2.0.3](#org.latencyutils:LatencyUtils:2.0.3)                                                                             | runtime  | jar    | Public Domain, per Creative Commons CC0                   |
| [org.mapstruct:mapstruct:1.5.3.Final](#org.mapstruct:mapstruct:1.5.3.Final)                                                                             | compile  | jar    | The Apache Software License, Version 2.0                  |
| [org.mockito:mockito-core:4.5.1](#org.mockito:mockito-core:4.5.1)                                                                                       | test     | jar    | The MIT License                                           |
| [org.mockito:mockito-junit-jupiter:4.5.1](#org.mockito:mockito-junit-jupiter:4.5.1)                                                                     | test     | jar    | The MIT License                                           |
| [org.mongodb:bson:4.6.1](#org.mongodb:bson:4.6.1)                                                                                                       | compile  | jar    | The Apache License, Version 2.0                           |
| [org.mongodb:bson-record-codec:4.6.1](#org.mongodb:bson-record-codec:4.6.1)                                                                             | runtime  | jar    | The Apache License, Version 2.0                           |
| [org.mongodb:mongodb-driver-core:4.6.1](#org.mongodb:mongodb-driver-core:4.6.1)                                                                         | compile  | jar    | The Apache License, Version 2.0                           |
| [org.mongodb:mongodb-driver-sync:4.6.1](#org.mongodb:mongodb-driver-sync:4.6.1)                                                                         | compile  | jar    | The Apache License, Version 2.0                           |
| [org.objenesis:objenesis:3.2](#org.objenesis:objenesis:3.2)                                                                                             | test     | jar    | Apache License, Version 2.0                               |
| [org.openapitools:jackson-databind-nullable:0.2.6](#org.openapitools:jackson-databind-nullable:0.2.6)                                                   | compile  | jar    | Apache License 2.0                                        |
| [org.opentest4j:opentest4j:1.2.0](#org.opentest4j:opentest4j:1.2.0)                                                                                     | test     | jar    | The Apache License, Version 2.0                           |
| [org.ow2.asm:asm:9.1](#org.ow2.asm:asm:9.1)                                                                                                             | test     | jar    | BSD-3-Clause                                              |
| [org.projectlombok:lombok:1.18.24](#org.projectlombok:lombok:1.18.24)                                                                                   | provided | jar    | The MIT License                                           |
| [org.reactivestreams:reactive-streams:1.0.4](#org.reactivestreams:reactive-streams:1.0.4)                                                               | compile  | jar    | MIT-0                                                     |
| [org.skyscreamer:jsonassert:1.5.1](#org.skyscreamer:jsonassert:1.5.1)                                                                                   | test     | jar    | The Apache Software License, Version 2.0                  |
| [org.slf4j:jul-to-slf4j:1.7.36](#org.slf4j:jul-to-slf4j:1.7.36)                                                                                         | compile  | jar    | MIT License                                               |
| [org.slf4j:slf4j-api:1.7.36](#org.slf4j:slf4j-api:1.7.36)                                                                                               | compile  | jar    | MIT License                                               |
| [org.springdoc:springdoc-openapi-common:1.6.13](#org.springdoc:springdoc-openapi-common:1.6.13)                                                         | compile  | jar    | The Apache License, Version 2.0                           |
| [org.springdoc:springdoc-openapi-ui:1.6.13](#org.springdoc:springdoc-openapi-ui:1.6.13)                                                                 | compile  | jar    | The Apache License, Version 2.0                           |
| [org.springdoc:springdoc-openapi-webmvc-core:1.6.13](#org.springdoc:springdoc-openapi-webmvc-core:1.6.13)                                               | compile  | jar    | The Apache License, Version 2.0                           |
| [org.springframework:spring-aop:5.3.24](#org.springframework:spring-aop:5.3.24)                                                                         | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-beans:5.3.24](#org.springframework:spring-beans:5.3.24)                                                                     | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-context:5.3.24](#org.springframework:spring-context:5.3.24)                                                                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-core:5.3.24](#org.springframework:spring-core:5.3.24)                                                                       | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-expression:5.3.24](#org.springframework:spring-expression:5.3.24)                                                           | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-jcl:5.3.24](#org.springframework:spring-jcl:5.3.24)                                                                         | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-messaging:5.3.24](#org.springframework:spring-messaging:5.3.24)                                                             | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-test:5.3.24](#org.springframework:spring-test:5.3.24)                                                                       | test     | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-tx:5.3.24](#org.springframework:spring-tx:5.3.24)                                                                           | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-web:5.3.24](#org.springframework:spring-web:5.3.24)                                                                         | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-webflux:5.3.24](#org.springframework:spring-webflux:5.3.24)                                                                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-webmvc:5.3.24](#org.springframework:spring-webmvc:5.3.24)                                                                   | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework:spring-websocket:5.3.24](#org.springframework:spring-websocket:5.3.24)                                                             | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot:2.7.6](#org.springframework.boot:spring-boot:2.7.6)                                                               | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-actuator:2.7.6](#org.springframework.boot:spring-boot-actuator:2.7.6)                                             | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.6](#org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.6)                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-autoconfigure:2.7.6](#org.springframework.boot:spring-boot-autoconfigure:2.7.6)                                   | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter:2.7.6](#org.springframework.boot:spring-boot-starter:2.7.6)                                               | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-actuator:2.7.6](#org.springframework.boot:spring-boot-starter-actuator:2.7.6)                             | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-data-mongodb:2.7.6](#org.springframework.boot:spring-boot-starter-data-mongodb:2.7.6)                     | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-json:2.7.6](#org.springframework.boot:spring-boot-starter-json:2.7.6)                                     | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-log4j2:2.7.6](#org.springframework.boot:spring-boot-starter-log4j2:2.7.6)                                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.7.6](#org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.7.6) | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-reactor-netty:2.7.6](#org.springframework.boot:spring-boot-starter-reactor-netty:2.7.6)                   | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-security:2.7.6](#org.springframework.boot:spring-boot-starter-security:2.7.6)                             | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-test:2.7.6](#org.springframework.boot:spring-boot-starter-test:2.7.6)                                     | test     | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-tomcat:2.7.6](#org.springframework.boot:spring-boot-starter-tomcat:2.7.6)                                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-validation:2.7.6](#org.springframework.boot:spring-boot-starter-validation:2.7.6)                         | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-web:2.7.6](#org.springframework.boot:spring-boot-starter-web:2.7.6)                                       | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-webflux:2.7.6](#org.springframework.boot:spring-boot-starter-webflux:2.7.6)                               | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-starter-websocket:2.7.6](#org.springframework.boot:spring-boot-starter-websocket:2.7.6)                           | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-test:2.7.6](#org.springframework.boot:spring-boot-test:2.7.6)                                                     | test     | jar    | Apache License, Version 2.0                               |
| [org.springframework.boot:spring-boot-test-autoconfigure:2.7.6](#org.springframework.boot:spring-boot-test-autoconfigure:2.7.6)                         | test     | jar    | Apache License, Version 2.0                               |
| [org.springframework.data:spring-data-commons:2.7.6](#org.springframework.data:spring-data-commons:2.7.6)                                               | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.data:spring-data-mongodb:3.4.6](#org.springframework.data:spring-data-mongodb:3.4.6)                                               | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.security:spring-security-config:5.7.5](#org.springframework.security:spring-security-config:5.7.5)                                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.security:spring-security-core:5.7.5](#org.springframework.security:spring-security-core:5.7.5)                                     | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.security:spring-security-crypto:5.7.5](#org.springframework.security:spring-security-crypto:5.7.5)                                 | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.security:spring-security-oauth2-core:5.7.5](#org.springframework.security:spring-security-oauth2-core:5.7.5)                       | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.security:spring-security-oauth2-jose:5.7.5](#org.springframework.security:spring-security-oauth2-jose:5.7.5)                       | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.security:spring-security-oauth2-resource-server:5.7.5](#org.springframework.security:spring-security-oauth2-resource-server:5.7.5) | compile  | jar    | Apache License, Version 2.0                               |
| [org.springframework.security:spring-security-test:5.7.5](#org.springframework.security:spring-security-test:5.7.5)                                     | test     | jar    | Apache License, Version 2.0                               |
| [org.springframework.security:spring-security-web:5.7.5](#org.springframework.security:spring-security-web:5.7.5)                                       | compile  | jar    | Apache License, Version 2.0                               |
| [org.webjars:swagger-ui:4.15.5](#org.webjars:swagger-ui:4.15.5)                                                                                         | compile  | jar    | Apache 2.0                                                |
| [org.webjars:webjars-locator-core:0.50](#org.webjars:webjars-locator-core:0.50)                                                                         | compile  | jar    | MIT                                                       |
| [org.xmlunit:xmlunit-core:2.9.0](#org.xmlunit:xmlunit-core:2.9.0)                                                                                       | test     | jar    | The Apache Software License, Version 2.0                  |
| [org.yaml:snakeyaml:1.30](#org.yaml:snakeyaml:1.30)                                                                                                     | compile  | bundle | Apache License, Version 2.0                               |
