<#import "template.ftl" as layout>
<@layout.emailLayout>
<#if is_new_user?? && is_new_user == "true">
${kcSanitize(msg("magicLinkBodyHtmlNewUser", realmName, magicLink))?no_esc}
<#else>
${kcSanitize(msg("magicLinkBodyHtml", realmName, magicLink))?no_esc}
</#if>
</@layout.emailLayout>
