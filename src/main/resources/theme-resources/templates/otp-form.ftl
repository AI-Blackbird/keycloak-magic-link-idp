<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        <div class="logo">
            <img src="${url.resourcesPath}/img/logo.svg" alt="${msg("emailLogoAlt")}" width="150" height="auto">
        </div>
    <#elseif section = "form">
        <div class="container">
            <div class="content">
                <h2>${msg("loginOtpTitle")}</h2>
                <form id="kc-otp-login-form" action="${url.loginAction}" method="post">
                    <div class="form-group">
                        <label for="otp" class="form-label">${msg("loginOtpOneTime")}</label>
                        <input id="otp" name="otp" autocomplete="off" type="text" class="form-control" autofocus
                            aria-invalid="<#if messagesPerField.existsError('totp')>true</#if>"/>
                        <#if messagesPerField.existsError('totp')>
                            <span id="input-error-otp-code" class="error-message" aria-live="polite">
                                ${kcSanitize(messagesPerField.get('totp'))?no_esc}
                            </span>
                        </#if>
                    </div>

                    <div id="kc-form-buttons" class="form-group">
                        <input class="button" name="submit" id="kc-submit" type="submit" value="${msg("doSubmit")}" />
                        <input class="button button-secondary" name="resend" id="kc-resend" type="submit" value="${msg("doResend")}" />
                    </div>
                </form>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>
