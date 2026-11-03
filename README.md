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
- **Login theme** `jwe-keycloak` (`keycloak/themes/jwe-keycloak/login`) restyles Keycloak's sign-in page — see
  [Login theme](#login-theme).

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

## Roles in tokens

Both clients have *Full scope allowed* turned off, with no role scope mappings: Keycloak 26.8 deprecated the
switch and logs a warning on every token issued while it is on. Their tokens therefore carry no roles
(`realm_access`, `resource_access`) and no `aud`. Nothing here reads them: the app validates the signature, `iss`
and `exp`, and the Postman tests check `azp` and `preferred_username`. To give a client a role, map it under
*Clients → client → Client scopes → `<client>-dedicated` → Scope*.

For a fresh import, set `"fullScopeAllowed": false` on both clients in `keycloak/realm-export.json`. The import
skips a realm that already exists; there, turn the switch off with:

```bash
kcadm() { docker exec jwe-keycloak /opt/keycloak/bin/kcadm.sh "$@"; }
kcadm config credentials --server http://localhost:8080 --realm master --user admin --password admin
for c in m2m-client login-client; do
  id=$(kcadm get clients -r demo -q clientId=$c --fields id --format csv --noquotes)
  kcadm update "clients/$id" -r demo -s fullScopeAllowed=false
done
```

## Login theme

`keycloak/themes/jwe-keycloak/login` is a custom Keycloak login theme. It extends the built-in `keycloak.v2` theme
and replaces only the username/password page (`login.ftl`): a split layout with an animated, color-coded JWE
(`RSA-OAEP-256 · A256GCM`) next to the sign-in form. Every other page (password reset, required actions, errors)
keeps the `keycloak.v2` look.

| Path                             | Purpose                                                                                             |
|----------------------------------|-----------------------------------------------------------------------------------------------------|
| `login.ftl`                      | FreeMarker template of the sign-in page.                                                            |
| `resources/css/login.css`        | Styles: light and dark (`prefers-color-scheme`), responsive down to phone width.                    |
| `resources/js/login.js`          | Show/hide password, Caps Lock hint, busy submit button, token animation (off under reduced motion). |
| `messages/messages_*.properties` | Theme texts in `pt-BR` and `en`; Keycloak built-in messages cover the rest.                         |
| `resources/fonts/`               | Instrument Serif and JetBrains Mono (OFL, licenses included), served by Keycloak itself.            |

The fonts are self-hosted, so the sign-in page makes no third-party requests.

### Wiring

`docker-compose.yml` mounts the theme read-only into `/opt/keycloak/themes/jwe-keycloak` and turns theme caching
off (`KC_SPI_THEME__*`), so edits show up on a browser reload without restarting Keycloak.

### Enabling it on a realm

A theme applies only to the realms that select it. For a fresh import, set these top-level fields in
`keycloak/realm-export.json` (the last three make `pt-BR` the default language):

```json
"loginTheme": "jwe-keycloak",
"internationalizationEnabled": true,
"supportedLocales": ["pt-BR", "en"],
"defaultLocale": "pt-BR"
```

The import runs only when the realm does not exist yet. On a running realm, pick the theme under
*Realm settings → Themes → Login theme* in the admin console, or:

```bash
docker exec jwe-keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 --realm master --user admin --password admin
docker exec jwe-keycloak /opt/keycloak/bin/kcadm.sh update realms/demo -s loginTheme=jwe-keycloak
```

### Seeing it

- Open `http://localhost:8080/realms/demo/account` and sign in with `demo` / `demo`. With an existing session
  Keycloak skips the page: sign out or use a private window.
- The page follows the browser's language when it is a supported locale, otherwise the realm default. The footer
  links switch languages.
- `/oauth/login` uses the password grant and never renders this page: the theme shows up only in browser flows
  (the account console, or Authorization Code + PKCE).
- The account console requires the realm's default roles. Users imported from `realm-export.json` only get them
  when listed: `"realmRoles": ["default-roles-demo"]`.

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
