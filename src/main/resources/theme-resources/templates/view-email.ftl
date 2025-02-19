<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        <#if auth?has_content && auth.showUsername()>
            <div id="kc-username" class="${properties.kcFormGroupClass!}">
                <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">
                    <div class="kc-login-tooltip">
                        <i class="${properties.kcResetFlowIcon!}"></i>
                        <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                    </div>
                </a>
            </div>
        </#if>
    <#elseif section = "form">
        ${msg("magicLinkConfirmation")}
    </#if>
</@layout.registrationLayout>
