# Custom Token Mapper for Keycloak

This project provides a custom token mapper provider for Keycloak. The mapper reads the `custom_value` parameter from incoming HTTP requests and adds it as a claim to the access or ID token.

## Features

- Custom Protocol Mapper compatible with Keycloak 22.0.1
- Dynamically adds values to tokens based on request parameters
- Easy setup with Docker and Docker Compose

## Setup

### 1. Build the Project

```sh
docker-compose build
```

Or manually:

```sh
mvn clean install -DskipTests
```

### 2. Start Keycloak and Postgres

```sh
docker-compose up
```

Access the Keycloak admin console at [http://localhost:8080](http://localhost:8080):

- Username: `admin`
- Password: `admin`

## Using the Mapper

1. Log in to the Keycloak admin console.
2. Select your realm and client.
3. Go to the "Client Scopes" or "Protocol Mappers" section.
4. Click "Add Mapper" and select "Custom Value Token Mapper".
5. Specify the claim name you want to add to the token.

Now, when you request a token and include the `custom_value` parameter, its value will be added to the token as a claim.

## Developer Notes

- Mapper code: [`CustomTokenMapper`](src/main/java/com/example/mapper/CustomTokenMapper.java)
- Provider registration: [`org.keycloak.protocol.ProtocolMapper`](src/main/resources/META-INF/services/org.keycloak.protocol.ProtocolMapper)
- Dockerfile: [Dockerfile](Dockerfile)
- Maven configuration: [pom.xml](pom.xml)

## License

MIT