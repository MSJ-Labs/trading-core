variable "keycloak_url" {
  description = "Base URL of the Keycloak server, as reachable from where Terraform runs."
  type        = string
}

variable "terraform_client_secret" {
  description = "Secret of the technical client 'terraform'. Supplied through TF_VAR_terraform_client_secret, never written in a file."
  type        = string
  sensitive   = true
}

variable "ssl_required" {
  description = "Which requests must use HTTPS (external, all, none)."
  type        = string
}

variable "deletion_protection" {
  description = "When true, Terraform refuses to destroy the realm."
  type        = bool
}

variable "enable_dev_tools" {
  description = "Create the dev-only client 'trading-dev-tools' (Authorization Code + PKCE)."
  type        = bool
}

variable "dev_tools_redirect_uris" {
  description = "Redirect URIs accepted for 'trading-dev-tools'."
  type        = list(string)
}