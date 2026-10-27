# Dev only: client used by Swagger UI / manual tests to get a token with the same flow as
# production (Authorization Code + PKCE). No client secret: a public client cannot keep one.
resource "keycloak_openid_client" "dev_tools" {
  count = var.enable_dev_tools ? 1 : 0

  realm_id  = keycloak_realm.trading.id
  client_id = "trading-dev-tools"
  name      = "Trading dev tools (dev only)"
  enabled   = true

  access_type = "PUBLIC"

  standard_flow_enabled        = true
  implicit_flow_enabled        = false
  direct_access_grants_enabled = false
  service_accounts_enabled     = false

  pkce_code_challenge_method = "S256"

  valid_redirect_uris = var.dev_tools_redirect_uris
  # "+" = allow the origins of the valid redirect URIs (CORS for the token request).
  web_origins = ["+"]
}

# Adds aud=<api_audience> to the access token. Additive: it does not touch the client's scopes.
resource "keycloak_openid_audience_protocol_mapper" "dev_tools_api_audience" {
  count = var.enable_dev_tools ? 1 : 0

  realm_id  = keycloak_realm.trading.id
  client_id = keycloak_openid_client.dev_tools[0].id
  name      = "api-audience"

  included_custom_audience = var.api_audience
  add_to_access_token      = true
  add_to_id_token          = false
}