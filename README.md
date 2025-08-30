# Custom Token Mapper for Keycloak

This project provides a custom token mapper provider for Keycloak.

It makes an http request to an external API passing in the username and stores a value from the JSON response as a claim

## Features

- Custom Protocol Mapper compatible with Keycloak 22.0.1
- Dynamically adds values to tokens based on request parameters
- Easy setup with Docker and Docker Compose
- Lightweight sample API application that returns an example response

## Setup

### 1. Build the Project

```sh
docker compose build
```

### 2. Start Services

```sh
docker compose up
```

Access the Keycloak admin console at [http://localhost:8081](http://localhost:8081):

- Username: `admin`
- Password: `admin`

## Using the Mapper

1. Log in to the Keycloak admin console.
2. Select your realm and client.
3. Click the "Client scopes" tab and "Add client scope", then add a mapper "By configuration" and select "Custom Value Token Mapper"
4. Specify the API Endpoint URL with templated username using `${username}` and JSON field to store result as a claim name

Now, when you request a token, its value will be added to the token as a claim.

## Evaluate generated token
1. Go to the client and select "Client scopes" tab.
2. Select the "Evaluate" sub tab
3. Select a user
4. Select "Generated access token" on right sidepanel

```json
{
  "exp": 1756518483,
  "iat": 1756518423,
  "jti": "94fc5b68-1b0e-475c-921b-2bd2ab5857f4",
  "iss": "http://localhost:8081/realms/master",
  "sub": "a95993a3-8331-4ab1-a71d-ba7a8f2d5d60",
  "typ": "Bearer",
  "azp": "admin-cli",
  "session_state": "2c0fdffc-0ccf-4bc6-9c8f-993849c05902",
  "acr": "1",
  "scope": "openid email auth-api profile",
  "sid": "2c0fdffc-0ccf-4bc6-9c8f-993849c05902",
  "email_verified": false,
  "sources": [
    "source_a",
    "source_b"
  ],
  "preferred_username": "admin"
}
```

## Developer Notes

- Mapper code: [`CustomTokenMapper`](src/main/java/com/example/mapper/CustomTokenMapper.java)
- Provider registration: [`org.keycloak.protocol.ProtocolMapper`](src/main/resources/META-INF/services/org.keycloak.protocol.ProtocolMapper)
- Dockerfile: [Dockerfile](Dockerfile)
- Maven configuration: [pom.xml](pom.xml)

## License

MIT