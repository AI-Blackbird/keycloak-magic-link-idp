package io.phasetwo.keycloak.magic.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MagicLinkRequest {
  @JsonProperty("username")
  private String username;

  @JsonProperty("email")
  private String email;

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("redirect_uri")
  private String redirectUri;

  @JsonProperty("expiration_seconds")
  private int expirationSeconds = 60 * 60 * 24;

  @JsonProperty("force_create")
  private boolean forceCreate = false;

  @JsonProperty("update_profile")
  private boolean updateProfile = false;

  @JsonProperty("update_password")
  private boolean updatePassword = false;

  @JsonProperty("send_email")
  private boolean sendEmail = false;

  @JsonProperty("scope")
  private String scope = null;

  @JsonProperty("nonce")
  private String nonce = null;

  @JsonProperty("state")
  private String state = null;

  @JsonProperty("remember_me")
  private Boolean rememberMe = false;

  @JsonProperty("reusable")
  private Boolean actionTokenPersistent = true;

  @JsonProperty("is_new_user")
  private Boolean isNewUser = false;
}
