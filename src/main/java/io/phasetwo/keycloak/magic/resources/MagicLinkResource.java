package io.phasetwo.keycloak.magic.resources;

import io.phasetwo.keycloak.magic.MagicLink;
import io.phasetwo.keycloak.magic.auth.token.MagicLinkActionToken;
import io.phasetwo.keycloak.magic.representation.MagicLinkRequest;
import io.phasetwo.keycloak.magic.representation.MagicLinkResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.OptionalInt;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;

import static io.phasetwo.keycloak.magic.MagicLink.MAGIC_LINK;

@JBossLog
@Path("/magic-link")
public class MagicLinkResource extends AbstractAdminResource {

  public MagicLinkResource(KeycloakSession session) {
    super(session);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MagicLinkResponse createMagicLink(final MagicLinkRequest rep) {
    if (!permissions.users().canManage())
      throw new ForbiddenException("magic link requires manage-users");

    ClientModel client = session.clients().getClientByClientId(realm, rep.getClientId());
    if (client == null)
      throw new NotFoundException(String.format("Client with ID %s not found.", rep.getClientId()));
    if (!MagicLink.validateRedirectUri(session, rep.getRedirectUri(), client))
      throw new BadRequestException(
          String.format("redirectUri %s disallowed by client.", rep.getRedirectUri()));

    String emailOrUsername = rep.getEmail();
    boolean forceCreate = rep.isForceCreate();
    boolean updateProfile = rep.isUpdateProfile();
    boolean updatePassword = rep.isUpdatePassword();
    boolean sendEmail = rep.isSendEmail();

    if (rep.getUsername() != null) {
      emailOrUsername = rep.getUsername();
      forceCreate = false;
      sendEmail = false;
    }

    UserModel user = MagicLink.getOrCreate(
        session,
        realm,
        emailOrUsername,
        forceCreate,
        updateProfile,
        updatePassword,
        MagicLink.registerEvent(event, MAGIC_LINK));
    if (user == null)
      throw new NotFoundException(
          String.format(
              "User with email/username %s not found, and forceCreate is off.", emailOrUsername));

    MagicLinkActionToken token = MagicLink.createActionToken(
        user,
        rep.getClientId(),
        rep.getRedirectUri(),
        OptionalInt.of(rep.getExpirationSeconds()),
        rep.getScope(),
        rep.getNonce(),
        rep.getState(),
        rep.getRememberMe(),
        rep.getActionTokenPersistent(),
        rep.getIsNewUser());
    String link = MagicLink.linkFromActionToken(session, realm, token);
    boolean sent = false;
    if (sendEmail) {
      sent = MagicLink.sendMagicLinkEmail(session, user, link);
      log.debugf("sent email to %s? %b. Link? %s", rep.getEmail(), sent, link);
    }

    MagicLinkResponse resp = new MagicLinkResponse();
    resp.setUserId(user.getId());
    resp.setLink(link);
    resp.setSent(sent);

    return resp;
  }

  @POST
  @Path("/send/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response sendMagicLink(@PathParam("userId") String userId,
      @QueryParam("client_id") String clientId,
      @QueryParam("redirect_uri") String redirectUri,
      @QueryParam("is_new_user") @DefaultValue("false") boolean isNewUser) {
    if (!permissions.users().canManage())
      throw new ForbiddenException("magic link requires manage-users");

    RealmModel tokenRealm = realm;

    try {
      session.getContext().setRealm(tokenRealm);

      ClientModel client = session.clients().getClientByClientId(tokenRealm, clientId);
      if (client == null) {
        return Response.status(404)
            .type(MediaType.APPLICATION_JSON)
            .entity("{\"error\":\"Client not found\"}")
            .build();
      }
      if (!MagicLink.validateRedirectUri(session, redirectUri, client)) {
        return Response.status(400)
            .type(MediaType.APPLICATION_JSON)
            .entity("{\"error\":\"Invalid redirect URI\"}")
            .build();
      }

      UserModel user = session.users().getUserById(tokenRealm, userId);
      if (user == null) {
        return Response.status(404)
            .type(MediaType.APPLICATION_JSON)
            .entity("{\"error\":\"User not found\"}")
            .build();
      }

      MagicLinkActionToken token = MagicLink.createActionToken(
          user,
          clientId,
          redirectUri,
          OptionalInt.of(43200), // 12 hours
          null,
          null,
          null,
          false,
          false,
          isNewUser);

      String link = MagicLink.linkFromActionToken(session, tokenRealm, token);
      boolean sent = MagicLink.sendMagicLinkEmail(session, user, link);

      return sent ? Response.ok("{\"success\":true}")
          .type(MediaType.APPLICATION_JSON)
          .build()
          : Response.serverError()
              .type(MediaType.APPLICATION_JSON)
              .entity("{\"error\":\"Failed to send email\"}")
              .build();
    } finally {
      session.getContext().setRealm(tokenRealm);
    }
  }
}
