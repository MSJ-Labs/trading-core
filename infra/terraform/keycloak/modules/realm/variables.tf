variable "realm_name" {
  description = "Name of the realm."
  type        = string
  default     = "trading"
}

variable "display_name" {
  description = "Display name shown on the Keycloak login pages."
  type        = string
  default     = "Iris"
}

variable "ssl_required" {
  description = "Which requests must use HTTPS: 'external' (all but localhost), 'all' or 'none'. No default: each environment must choose."
  type        = string

  validation {
    condition     = contains(["external", "all", "none"], var.ssl_required)
    error_message = "ssl_required must be one of: external, all, none."
  }
}

variable "deletion_protection" {
  description = "When true, Terraform refuses to destroy the realm. No default: each environment must choose."
  type        = bool
}

variable "enable_dev_tools" {
  description = "Create the dev-only client 'trading-dev-tools' (password grant, to get test tokens). Must be false in production. No default: each environment must choose."
  type        = bool
}

variable "dev_tools_redirect_uris" {
  description = "Redirect URIs accepted for 'trading-dev-tools'. Only used when enable_dev_tools is true."
  type        = list(string)
  default     = []
}

variable "api_audience" {
  description = "Audience (aud) that tokens must carry to be accepted by the resource server."
  type        = string
  default     = "trading-core"
}

variable "revoke_refresh_token" {
  description = "A refresh token can be used only once. Keep true unless an environment has a documented reason."
  type        = bool
  default     = true
}