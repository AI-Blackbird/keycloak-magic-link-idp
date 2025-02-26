<#ftl output_format="plainText">
<#if is_new_user?? && is_new_user == "true">
${msg("magicLinkBodyNewUser", realmName, magicLink)}
<#else>
${msg("magicLinkBody", realmName, magicLink)}
</#if>
