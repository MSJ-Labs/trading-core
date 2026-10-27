# Keycloak generates the built-in client scopes and roles of the realm itself: never redeclare them here.
resource "keycloak_realm" "trading" {
  realm        = var.realm_name
  display_name = var.display_name
  enabled      = true
  ssl_required = var.ssl_required

  terraform_deletion_protection = var.deletion_protection

  registration_allowed           = true
  registration_email_as_username = false
  login_with_email_allowed       = true
  duplicate_emails_allowed       = false
  reset_password_allowed         = true
  verify_email                   = false
  edit_username_allowed          = false

  password_policy = "length(12) and notUsername and passwordHistory(3)"

  access_token_lifespan    = "5m"
  sso_session_idle_timeout = "30m"
  sso_session_max_lifespan = "10h"
  refresh_token_max_reuse  = 0
  revoke_refresh_token     = var.revoke_refresh_token

  security_defenses {
    brute_force_detection {
      permanent_lockout          = false
      max_login_failures         = 5
      wait_increment_seconds     = 60
      max_failure_wait_seconds   = 1800
      failure_reset_time_seconds = 43200
    }
  }
}

output "realm_id" {
  value = keycloak_realm.trading.id
}