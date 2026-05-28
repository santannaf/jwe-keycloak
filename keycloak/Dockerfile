# Custom Keycloak image backed by PostgreSQL, built in "optimized" mode.
# Pattern adapted from https://github.com/ozbillwang/keycloak-compose
# Docs: https://www.keycloak.org/server/containers
ARG KEYCLOAK_VERSION=26.1

# --- Build stage: bake build-time options so the runtime can use --optimized ---
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder

ENV KC_DB=postgres

WORKDIR /opt/keycloak
RUN /opt/keycloak/bin/kc.sh build \
    --db=postgres \
    --health-enabled=true \
    --metrics-enabled=true

# --- Runtime stage ---
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION}
ARG KEYCLOAK_VERSION
LABEL image.version=${KEYCLOAK_VERSION}

COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
