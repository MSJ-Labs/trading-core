keycloak_url        = "http://localhost:8180"
ssl_required        = "external"
deletion_protection = false
enable_dev_tools    = true

dev_tools_redirect_uris = [
  "http://localhost:8080/swagger-ui/oauth2-redirect.html",
  "http://localhost:9999/callback",
]