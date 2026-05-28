# jwe-keycloak

Localhost demo of a Spring Boot 4 backend integrating with Keycloak for two OAuth flows:

1. **Machine-to-machine (M2M)** — obtain a Keycloak `client_credentials` token and return it encrypted as a **JWE**, then validate that JWE.
2. **User login** — authenticate a user (username/password) against Keycloak.

No secret is hardcoded in Java. All sensitive values come from environment variables.

## Components

- **Keycloak** runs in Docker on `http://localhost:8080`, backed by **PostgreSQL** and built from a custom
  optimized image (`keycloak/Dockerfile`). The `demo` realm is auto-imported.
- **PostgreSQL** (`localhost:5432`) persists Keycloak data; **pgAdmin** (`localhost:5050`) is an optional DB UI.
- **The Spring app** runs on `http://localhost:8081` (so it does not clash with Keycloak's `8080`).

## Endpoints

| Method  | Path               | Purpose                                                                                                              |
|---------|--------------------|----------------------------------------------------------------------------------------------------------------------|
| `POST`  | `/oauth/m2m/token` | Issue a Keycloak M2M access token encrypted as a JWE (RSA-OAEP-256 + A256GCM).                                       |
| `POST`  | `/oauth/m2m/login` | Receive a JWE, decrypt it, validate the inner Keycloak token. Prints `Token is ok` on the server console when valid. |
| `POST`  | `/oauth/login`     | Authenticate a user (password grant); return the access and refresh tokens encrypted as JWEs.                        |

## Environment variables

| Variable                       | Default                 | Notes                                                                |
|--------------------------------|-------------------------|----------------------------------------------------------------------|
| `SERVER_PORT`                  | `8081`                  | App HTTP port.                                                       |
| `KEYCLOAK_BASE_URL`            | `http://localhost:8080` | Keycloak base URL.                                                   |
| `KEYCLOAK_REALM`               | `demo`                  | Realm imported by docker-compose.                                    |
| `KEYCLOAK_M2M_CLIENT_ID`       | `m2m-client`            | Confidential client for `client_credentials`.                        |
| `KEYCLOAK_M2M_CLIENT_SECRET`   | _(empty)_               | **Required.** Local demo value: `<your_client_secret>`.              |
| `KEYCLOAK_LOGIN_CLIENT_ID`     | `login-client`          | Confidential client for the password grant.                          |
| `KEYCLOAK_LOGIN_CLIENT_SECRET` | _(empty)_               | **Required.** Local demo value: `<your_client_secret>`.              |

The two client secrets are local-only demo values defined in `keycloak/realm-export.json`. They live in
configuration/infra, never in Java source. Replace them with a real secret store outside of local demos.

## Run

### 1. Start Keycloak + PostgreSQL

```bash
docker compose up -d --build
```

First run builds the custom Keycloak image and starts three containers: `postgres` (`:5432`),
`keycloak` (`:8080`), and `pgadmin` (`:5050`). Compose reads `.env` if present (see `.env.example`).

The `demo` realm (clients `m2m-client`, `login-client`, and user `demo`/`demo`) is imported automatically.
Admin console: `http://localhost:8080` (bootstrap admin `admin` / `admin`, local only).

To configure or inspect Keycloak by hand instead of relying on the import, see
[`keycloak/SETUP.md`](keycloak/SETUP.md) — it walks through the realm, both clients, the demo user, and how to
verify each flow.

### 2. Export the demo secrets (same values as the imported realm)

```bash
export KEYCLOAK_M2M_CLIENT_SECRET=<your_client_secret>
export KEYCLOAK_LOGIN_CLIENT_SECRET=<your_client_secret>
```

### 3. Run the app

```bash
./gradlew bootRun
```

## Exercise the endpoints

```bash
# 1) Issue an encrypted (JWE) M2M token
JWE=$(curl -s -X POST http://localhost:8081/oauth/m2m/token | jq -r .access_token)
echo "$JWE"

# 2) Validate it — the server console prints: Token is ok
curl -s -X POST http://localhost:8081/oauth/m2m/login \
  -H 'Content-Type: application/json' \
  -d "{\"token\":\"$JWE\"}"

# 3) User login (password grant)
curl -s -X POST http://localhost:8081/oauth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"demo"}'
```

## Tests

```bash
./gradlew test
```

## Stop Keycloak

```bash
docker compose down       # stop containers, keep Postgres data
docker compose down -v    # also wipe the Postgres volume (realm re-imports on next start)
```

## Notes / limitations (demo only)

- The JWE RSA key pair is **ephemeral** (generated at startup). A JWE issued before a restart cannot be
  decrypted afterward, and the app is single-instance.
- User login uses the **Resource Owner Password Credentials** grant (deprecated for production); it is enabled
  only on the demo realm's login client. Use Authorization Code + PKCE for real frontends.
- Demo secrets and the bootstrap admin password are local-only values; do not reuse them.
