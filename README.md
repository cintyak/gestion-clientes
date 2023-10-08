# Gestion clientes Microservice

## Development

To start your application in the dev profile, run:

```
./gradlew
```

## Building for production

### Packaging as jar

To build the final jar and optimize the spring application for production, run:

```
./gradlew -Pprod clean bootJar
```

To ensure everything worked, run:

```
java -jar build/libs/*.jar
```

### Packaging as war

To package your application as a war in order to deploy it to an application server, run:

```
./gradlew -Pprod -Pwar clean bootWar
```

To generate two package war

```
./gradlew clean build -Pprod -Pwar
```

## Testing

To launch your application's tests, run:

```
./gradlew test integrationTest
```

To run a test file:

```
./gradlew test integrationTest --tests "bo.com.bisa.gpgw.msaccount.web.rest.UserJWTControllerIT"
```

## Upgrade Gradle

```
./gradlew wrapper --gradle-version=7.6.1
```

# Docker

Build docker image:

```
./gradlew bootJar jibDockerBuild
```

#### Upload image to **registry.gitlab.com**:

Login

```
docker login registry.gitlab.com
```

Logout

```
docker logout registry.gitlab.com
```

Build docker image and upload (use persisted credentials).

```
./gradlew jib -Pprod
```

Build docker image and upload (using credentials as parameter).

```
./gradlew jib -Pprod -Djib.to.auth.username=${GL_USERNAME} -Djib.to.auth.password=${GL_PASSWORD}
```

### Registry production (Use these commands responsibly)

Build docker image and upload to **registry production** (use persisted credentials).

```
./gradlew jib -Pprod -Pregistry-prod
```

Build docker image and upload to **registry production** (using credentials as parameter).

```
./gradlew jib -Pprod -Djib.to.auth.username=${GL_USERNAME} -Djib.to.auth.password=${GL_PASSWORD}
```
