# 🛡️ PanggaGuard

> A reactive API Gateway with built-in Authentication service, built on **Spring Reactive Cloud Gateway**, **Spring Security** and **Spring Oauth2 Client**.

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud_Gateway-2025.1.2-6DB33F?logo=spring)](https://spring.io/projects/spring-cloud-gateway)
[![Spring Security](https://img.shields.io/badge/Spring_Security-7.1.0-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![Spring OAuth2 Client](https://img.shields.io/badge/Spring_OAuth2_Client-7.1.0-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

---

## 📖 Overview

**PanggaGuard** is a service build by using Spring Reactive Cloud Gateway, Spring Security, and Spring Oauth2 Client.
It acts as the single entry point for your applications, handling request routing, JWT-based authentication, and OAuth2 social login — all in a non-blocking, reactive manner using Project Reactor and Spring WebFlux as the core.

I started it as it so tedious to always write the security layer in every project which has the similar problem and complexity. By using this service, you could put your business logic application behind this service safely without the need to setup from scratch.

---

## ✨ Features

- **🔀 Dynamic API Gateway** — Routes incoming requests to downstream services based on database-driven route configuration and custom headers.
- **🔐 JWT Authentication** — Issues and validates JSON Web Tokens (JWTs) for stateless, scalable authentication.
- **🌐 OAuth2 / Social Login** — Out-of-the-box Google OAuth2 login integration via Spring Security.
- **🔒 Token Invalidation** — Tracks active sessions via `UserActivity` records, enabling server-side token revocation on logout.
- **⚡ Reactive & Non-blocking** — Built entirely on Spring WebFlux and Project Reactor for high concurrency and low latency.
- **🗄️ Multi-database Support** — Ships with drivers for PostgreSQL, Oracle, SQLite, and H2 (for development/testing).
- **📊 Distributed Tracing** — Integrated with Micrometer Tracing (Brave) for end-to-end observability.

---

## 🏗️ Architecture

```mermaid
flowchart TD
    Client(["🖥️ Client"])
    OAuth2Provider(["🌐 Google OAuth2"])

    subgraph PanggaGuard ["🛡️ PanggaGuard"]
        direction TB
        JWTFilter["⚙️ JwtReactiveFilter\n@Order(-100)"]
        SecurityChain["🔐 Spring Security\nFilter Chain"]
        AuthCtrl["📋 Auth Controller\n/auth/sign-in\n/auth/sign-up\n/auth/sign-out"]
        Gateway["🔀 Spring Cloud Gateway\nRoute Locator"]
    end

    DB[("🗄️ PostgreSQL")]
    ServiceA["📦 Downstream\nService A"]
    ServiceB["📦 Downstream\nService B"]

    Client -->|"HTTP Request\n+ Bearer Token"| JWTFilter
    JWTFilter -->|"Valid JWT\n→ inject SecurityContext"| SecurityChain
    JWTFilter -->|"❌ Invalid / Revoked"| Reject["401 Unauthorized"]

    SecurityChain -->|"/auth/**, /oauth2/**\n/ping/public → permit"| AuthCtrl
    SecurityChain -->|"Authenticated request"| Gateway

    AuthCtrl -->|"sign-in / sign-up\n/ sign-out"| DB
    AuthCtrl <-->|"OAuth2 Authorization\nCode Flow"| OAuth2Provider

    Gateway -->|"Route match\n(Path / x-target-domain)"| ServiceA
    Gateway -->|"Route match\n(Path / x-target-domain)"| ServiceB
    Gateway -->|"Load routes"| DB
    JWTFilter -->|"Validate jti"| DB
```

### Request Flow

1. **Incoming Request** → hits `JwtReactiveFilter` (ordered at `-100`, runs before Spring Security).
2. **JWT Validation** → token is decoded, and its `jti` (JWT ID) is checked against the `USER_ACTIVITIES` table. Invalid or revoked tokens are rejected with `401 Unauthorized`.
3. **Route Matching** → Spring Cloud Gateway matches the request to a configured route (database-driven or YAML-defined) and proxies it to the downstream service.
4. **Auth Endpoints** → unauthenticated paths (`/auth/**`, `/oauth2/**`, `/ping/public`) bypass JWT validation.

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
| --- | --- |
| Java (JDK) | 25+ |
| Gradle | 8+ (wrapper included) |
| PostgreSQL | 13+ |

### 1. Clone the Repository

```bash
git clone https://github.com/sajiwo/panggaguard.git
cd panggaguard
```

### 2. Configure the Database

Create a PostgreSQL schema and update `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres?currentSchema=panggaguard
    username: your_db_user
    password: your_db_password
  jpa:
    hibernate:
      ddl-auto: update   # set to 'validate' in production
```

PanggaGuard will automatically generate the required tables (`USERS`, `ROUTES`, `USER_ACTIVITIES`) on first run.

### 3. Configure the JWT Secret

> ⚠️ **Security Notice:** The default secret key in `JwtService.java` is a placeholder. **Always override it** with a strong, environment-specific secret before deploying.

Set your secret via an environment variable or update the configuration:

```yaml
# application.yaml (recommended approach)
panggaguard:
  jwt:
    secret: your-very-long-and-secure-secret-key-at-least-256-bits
```

### 4. (Optional) Configure OAuth2 / Google Login

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            provider: google
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope: openid, email, profile
```

You can obtain credentials from the [Google Cloud Console](https://console.cloud.google.com/).

### 5. Build & Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The service will start on `http://localhost:8080` by default.

---

## 📡 API Endpoints

### Authentication

| Method | Path | Description | Auth Required |
| -------- | ------ | ------------- | :---: |
| `GET` | `/auth/sign-in/method` | Returns available sign-in methods | ❌ |
| `POST` | `/auth/sign-in` | Sign in with email & password, returns JWT | ❌ |
| `POST` | `/auth/sign-up` | Register a new user account | ❌ |
| `POST` | `/auth/sign-out` | Invalidate the current JWT session | ✅ |

### OAuth2

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/oauth2/authorization/google` | Redirect to Google OAuth2 login |

### Health

| Method | Path | Description | Auth Required |
|--------|------|-------------|:---:|
| `GET` | `/ping/public` | Public health check | ❌ |

### Sign In Request

```json
POST /auth/sign-in
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "yourpassword"
}
```

**Response:**

```json
{
  "data": {
    "jti": "550e8400-e29b-41d4-a716-446655440000",
    "bearerToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

Use the `bearerToken` in subsequent requests:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## ⚙️ Route Configuration

PanggaGuard supports two ways to configure routes:

### 1. YAML-based (Static)

Defined in `application.yaml`, suitable for simple or fixed routes:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: my-service
              uri: http://localhost:8081
              predicates:
                - Path=/my-service-api/**
              filters:
                - StripPrefix=1
```

### 2. Database-driven (Dynamic)

Stored in the `ROUTES` table. Routes are loaded at startup and matched based on the `x-target-domain` request header. To add a new route, insert a record:

```sql
INSERT INTO panggaguard.ROUTES (ROUTE_ID, DOMAIN, CREATED_BY, CREATED_DATE)
VALUES (gen_random_uuid(), 'my-downstream-domain', 'admin', NOW());
```

---

## 🔐 Spring Security

PanggaGuard uses **Spring Security for WebFlux** (`@EnableWebFluxSecurity`) with reactive method-level security (`@EnableReactiveMethodSecurity`). The entire security posture is defined in [`SecurityConfig`](src/main/java/dev/sajiwo/panggaguard/configuration/SecurityConfig.java) and enforced by a custom pre-filter.

### Security Filter Chain

The `SecurityWebFilterChain` bean configures the following rules:

| Rule | Detail |
| --- | --- |
| **Public paths** | `/auth/**`, `/oauth2/**`, `/ping/public` — no authentication required |
| **All other paths** | Must be authenticated (valid JWT or active OAuth2 session) |
| **Authentication entry point** | Returns `401 Unauthorized` (no redirect to login page for API clients) |
| **CSRF** | Disabled — suitable for stateless JWT / API gateway usage |
| **OAuth2 login page** | Custom login redirect to `/auth/sign-in/method` |
| **Logout URL** | `POST /logout` — triggers `LogoutHandler` to invalidate the JWT |

```java
http.authorizeExchange(authz ->
    authz.pathMatchers("/auth/**", "/oauth2/**", "/ping/public").permitAll()
         .anyExchange().authenticated()
);
```

### JWT Pre-Filter (`JwtReactiveFilter`)

A custom [`JwtReactiveFilter`](src/main/java/dev/sajiwo/panggaguard/components/JwtReactiveFilter.java) runs at order `-100` — **before** Spring Security's own filter — to establish the `SecurityContext` from a Bearer token:

```
Request
  │
  ▼
JwtReactiveFilter (@Order -100)
  ├─ No Authorization header?  ──────────────────────────▶ pass through
  ├─ Token present → decode JWT
  ├─ Lookup jti in USER_ACTIVITIES table
  ├─ Activity not found or isValid = false? ──────────────▶ 401 Unauthorized
  └─ Valid token → inject UsernamePasswordAuthenticationToken into ReactiveSecurityContext
            │
            ▼
  Spring Security filter chain continues
```

The `ReactiveSecurityContextHolder` is populated with the authenticated principal so that downstream filters and controllers can access it via `@AuthenticationPrincipal` or `ReactiveSecurityContextHolder.getContext()`.

### Password Encoding

Passwords are encoded with **BCrypt** via the `PasswordEncoder` bean in [`GlobalConfiguration`](src/main/java/dev/sajiwo/panggaguard/configuration/GlobalConfiguration.java):

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

This is used on registration (`UserServiceImpl`) and verified on sign-in (`JwtAuthenticationService`).

### Logout & Token Revocation

On `POST /logout`, the [`LogoutHandler`](src/main/java/dev/sajiwo/panggaguard/components/LogoutHandler.java) calls `UserActivityRepository.invalidatedTokenId(jti)`, flipping `IS_VALID = false` for the session row. Any subsequent request carrying the same JWT will be rejected by `JwtReactiveFilter`.

---

## 🌐 Spring OAuth2 Client

PanggaGuard integrates Spring Security's **OAuth2 Client** support to provide social login (currently Google). The flow follows the standard **Authorization Code Grant** with PKCE.

### OAuth2 Login Flow

```
Browser / Client
  │
  │  GET /oauth2/authorization/google
  ▼
PanggaGuard (Spring Security OAuth2 Client)
  │
  │  Redirect to Google Authorization Server
  ▼
Google Login Page
  │
  │  User authenticates & grants consent
  ▼
Google redirects back with ?code=...
  │
  ▼
PanggaGuard
  ├─ Exchanges authorization code for access token
  ├─ Fetches user info (name, email, profile picture)
  └─ Creates authenticated OAuth2 session
```

### Configuration

OAuth2 providers are registered in `application.yaml` under `spring.security.oauth2.client.registration`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:                          # registration ID (used in URL)
            provider: google
            client-id: YOUR_CLIENT_ID
            client-secret: YOUR_CLIENT_SECRET
            scope:
              - openid
              - email
              - profile
```

> [!NOTE]
> The `registration` key becomes part of the redirect URL: `/oauth2/authorization/{registrationId}`.  
> Using a custom registration ID (e.g., `google-myapp`) will change the URL accordingly.

### Endpoints

| Path | Description |
| --- | --- |
| `GET /auth/sign-in/method` | Returns the OAuth2 redirect path (e.g., `/google`) for the client to follow |
| `GET /oauth2/authorization/google` | Initiates the Google OAuth2 Authorization Code flow |
| `GET /login/oauth2/code/google` | Spring Security callback — handles the authorization code exchange (internal) |

### JWT Issuance after OAuth2 Login

After a successful OAuth2 login, PanggaGuard's `JwtService` can generate a JWT from the `OAuth2User` principal:

```java
public String generateToken(OAuth2User oAuth2User) {
    // Embeds: name, subject (OAuth2 principal name)
    // Expiry: 24 hours
    // Signed with: HMAC-SHA256
}
```

This allows the client to switch from the OAuth2 session cookie to a stateless Bearer token for subsequent API calls.

### Supported Providers

| Provider | Registration ID | Scopes |
|---|---|---|
| Google | `google` | `openid`, `email`, `profile` |

> Additional providers (GitHub, Facebook, etc.) can be added by registering them under `spring.security.oauth2.client.registration` following the same pattern.

---

## 🛠️ Tech Stack

| Technology | Purpose |
| --- | --- |
| [Spring Boot 4.1.0](https://spring.io/projects/spring-boot) | Application framework |
| [Spring Cloud Gateway (WebFlux)](https://spring.io/projects/spring-cloud-gateway) | Reactive API gateway |
| [Spring Security (OAuth2 Client)](https://spring.io/projects/spring-security) | Security & OAuth2 |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | Database ORM |
| [jjwt 0.13.0](https://github.com/jwtk/jjwt) | JWT generation & parsing |
| [Project Reactor](https://projectreactor.io/) | Reactive streams |
| [Micrometer Tracing (Brave)](https://micrometer.io/docs/tracing) | Distributed tracing |
| [Lombok](https://projectlombok.org/) | Boilerplate reduction |
| [MapStruct](https://mapstruct.org/) | DTO mapping |
| [Spotless + ktlint](https://github.com/diffplug/spotless) | Code formatting |
| PostgreSQL / Oracle / SQLite / H2 | Database drivers |
| Java 25 | Language runtime |

---

## 🤝 Contributing

Contributions are welcome! Here's how to get involved:

1. **Fork** the repository.
2. **Create** a feature branch: `git checkout -b feature/your-feature-name`
3. **Commit** your changes: `git commit -m 'feat: add some feature'`
4. **Push** to the branch: `git push origin feature/your-feature-name`
5. **Open** a Pull Request.

### Code Style

This project uses **Spotless** with **ktlint** for Gradle files. Before opening a PR, please run:

```bash
./gradlew spotlessApply
```

### Commit Convention

Please follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` — New features
- `fix:` — Bug fixes
- `docs:` — Documentation changes
- `refactor:` — Code refactoring
- `test:` — Adding or updating tests
- `chore:` — Maintenance tasks

---

## 🔒 Security

> [!IMPORTANT]
> Before deploying PanggaGuard to production, please review these security checklist items:

- [ ] Replace the hardcoded JWT secret in `JwtService.java` with an externalized, environment-specific secret (e.g., via environment variables or a secrets manager).
- [ ] Remove or secure the OAuth2 client credentials from `application.yaml` — never commit real secrets to version control.
- [ ] Set `spring.jpa.hibernate.ddl-auto` to `validate` or `none` in production.
- [ ] Enable HTTPS / TLS termination at the load balancer or proxy level.
- [ ] Review and tighten the `authorizeExchange` rules in `SecurityConfig` for your use case.

If you discover a security vulnerability, please **do not open a public issue**. Instead, contact the maintainer directly.

---

## 📄 License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.

---

## 🌟 Acknowledgements

- [Spring Team](https://spring.io/) for the incredible ecosystem.
- [JJWT](https://github.com/jwtk/jjwt) for the lightweight JWT library.
- The open-source community for continuous inspiration.
