# Keycloak Magic Link + Okta Integration Guide

This guide explains how to set up and configure the Magic Link authenticator with Okta domain-based routing.

## Overview

This integration allows you to:

1. Use Magic Link authentication for non-Okta managed domains
2. Automatically redirect to Okta SSO for Okta-managed domains

## Setup Steps

### 1. Build and Deploy

1. Build the extension:

   ```bash
   mvn clean install
   ```

2. Deploy to Keycloak:
   - Copy `target/keycloak-magic-link-idp-{version}.jar` to Keycloak's `providers` directory
   - Restart Keycloak

### 2. Keycloak Configuration

#### A. Create Authentication Flow

1. Open Keycloak Admin Console
2. Navigate to Authentication > Flows
3. Click "Create Flow"
   - Name: "Magic Link + Okta Flow"
   - Description: "Authentication with Magic Link and Okta SSO"

4. Add Execution:
   - Click "Add execution"
   - Select "Magic Link"
   - Set Requirement to "REQUIRED"

#### B. Configure Magic Link Authenticator

1. Click the gear icon (⚙️) next to Magic Link
2. Configure required settings:

   ```bash
   Okta Managed Domains: your-company.com,subsidiary.com
   Okta URL: https://your-org.okta.com
   ```

3. Optional settings to consider:
   - Force create user: Enable if you want to auto-create users
   - Update profile on create: Enable if new users should complete their profile
   - Token lifespan: Set magic link expiration time (e.g., 3600 for 1 hour)

#### C. Configure Email

1. Go to Realm Settings > Email
2. Configure SMTP settings:

   ```md
   Host: your-smtp-server
   Port: 587 (or your port)
   From: noreply@your-domain.com
   Enable Authentication: Yes (if required)
   Username/Password: Your SMTP credentials
   ```

### 3. Okta Configuration

#### Configure Identity Provider in Keycloak

1. Go to Identity Providers in Keycloak
2. Add provider > OpenID Connect
3. Configure:

   ```md
   Alias: okta
   Display Name: Okta SSO
   Service Provider entity ID: [Entity ID used to identify the SAML SP]
   Identity Provider entity ID: [The Entity ID used to validate the Issuer for received SAML assertions. If empty, no Issuer validation is performed.]
   Single Sign-On service URL: [The URL of the Okta Identity Provider's SAML endpoint]
   ```

### 4. Testing the Setup

1. Test Okta-managed domain:
   - Enter email: <user@your-company.com>
   - Should redirect to Okta login

2. Test non-Okta domain:
   - Enter email: <user@external-domain.com>
   - Should send magic link

## Troubleshooting

### Common Issues

1. Okta Redirect Not Working
   - Verify Okta domains configuration
   - Check Okta URL format
   - Verify redirect URIs in Okta application

2. Magic Link Not Received
   - Check SMTP configuration
   - Verify email template
   - Check spam folder

3. User Creation Issues
   - Verify "Force create user" setting
   - Check user profile requirements

### Debug Logging

Add to `standalone.xml`:

```xml
<logger category="io.phasetwo.keycloak.magic">
    <level name="DEBUG"/>
</logger>
```

## Security Best Practices

1. Always use HTTPS
2. Set appropriate token lifespans
3. Regularly audit Okta-managed domains
4. Use secure SMTP settings
5. Monitor authentication attempts
6. Implement rate limiting if needed

## Customization

### Email Templates

1. Location: Realm Settings > Email > Templates
2. Customizable elements:
   - Magic link email template
   - Email verification template
   - Subject lines
   - HTML/text content

### Domain Management

To update Okta-managed domains:

1. Go to Authentication > Flows
2. Find your flow
3. Click gear icon next to Magic Link
4. Update domains list

## Maintenance

Regular tasks:

1. Update domain list as needed
2. Monitor token lifespans
3. Review security settings
4. Check email delivery rates
5. Update Okta application settings as needed
