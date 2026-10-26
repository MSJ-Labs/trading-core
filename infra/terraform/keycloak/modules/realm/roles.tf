resource "keycloak_role" "user" {
  realm_id    = keycloak_realm.trading.id
  name        = "USER"
  description = "Standard trader"
}

resource "keycloak_role" "admin" {
  realm_id    = keycloak_realm.trading.id
  name        = "ADMIN"
  description = "Platform administrator"
}

# Authoritative: replaces the whole default-roles list, so the two roles Keycloak
# grants by default (offline_access, uma_authorization) are kept explicitly.
resource "keycloak_default_roles" "trading" {
  realm_id      = keycloak_realm.trading.id
  default_roles = ["offline_access", "uma_authorization", keycloak_role.user.name]
}