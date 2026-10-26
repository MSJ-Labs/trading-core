provider "keycloak" {
  url           = var.keycloak_url
  realm         = "master"
  client_id     = "terraform"
  client_secret = var.terraform_client_secret
  base_path     = ""
}

module "realm" {
  source = "../../modules/realm"

  ssl_required        = var.ssl_required
  deletion_protection = var.deletion_protection
  enable_dev_tools    = var.enable_dev_tools

  dev_tools_redirect_uris = var.dev_tools_redirect_uris
}

output "trading_realm_id" {
  value = module.realm.realm_id
}