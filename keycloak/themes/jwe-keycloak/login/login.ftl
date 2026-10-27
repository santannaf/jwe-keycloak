<!DOCTYPE html>
<html lang="${lang}"<#if realm.internationalizationEnabled> dir="${(locale.rtl)?then('rtl','ltr')}"</#if>>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="robots" content="noindex, nofollow">
    <meta name="color-scheme" content="light dark">
    <title>${msg("loginTitle", (realm.displayName!''))}</title>
    <link rel="icon" type="image/svg+xml" href="${url.resourcesPath}/img/favicon.svg">
    <link rel="preload" href="${url.resourcesPath}/fonts/jetbrains-mono-latin-wght-normal.woff2" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="${url.resourcesPath}/fonts/instrument-serif-latin-400-normal.woff2" as="font" type="font/woff2" crossorigin>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
    <script type="module" src="${url.resourcesPath}/js/login.js"></script>
</head>
<body>
<div class="jwe-shell">
    <main class="jwe-panel">
        <div class="jwe-panel__inner">
            <p class="jwe-realm"><span class="jwe-realm__dot" aria-hidden="true"></span>realm <strong>${realm.name}</strong></p>
            <h1 class="jwe-title">${msg("jweTitle")}</h1>
            <p class="jwe-subtitle">${msg("jweSubtitle")}</p>

            <#assign hasCredentialError = messagesPerField.existsError('username','password')>
            <#if hasCredentialError>
                <p class="jwe-alert jwe-alert--error" role="alert">${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}</p>
            <#elseif message?has_content>
                <p class="jwe-alert jwe-alert--${message.type}" role="alert">${kcSanitize(message.summary)?no_esc}</p>
            </#if>

            <#if realm.password>
            <form id="kc-form-login" class="jwe-form" action="${url.loginAction}" method="post" data-login-form>
                <#if !usernameHidden??>
                <div class="jwe-field">
                    <label class="jwe-field__label" for="username"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
                    <input class="jwe-field__input" id="username" name="username" type="text" value="${(login.username)!''}"
                           autocomplete="username" autocapitalize="none" spellcheck="false" autofocus required
                           aria-invalid="${hasCredentialError?c}">
                </div>
                </#if>

                <div class="jwe-field">
                    <div class="jwe-field__head">
                        <label class="jwe-field__label" for="password">${msg("password")}</label>
                        <#if realm.resetPasswordAllowed>
                        <a class="jwe-link jwe-link--small" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a>
                        </#if>
                    </div>
                    <div class="jwe-password">
                        <input class="jwe-field__input" id="password" name="password" type="password"
                               autocomplete="current-password" required<#if usernameHidden??> autofocus</#if>
                               aria-invalid="${hasCredentialError?c}" aria-describedby="jwe-caps-lock">
                        <button class="jwe-password__toggle" type="button" aria-controls="password" data-password-toggle
                                data-label-show="${msg("jweShowPassword")}" data-label-hide="${msg("jweHidePassword")}">${msg("jweShowPassword")}</button>
                    </div>
                    <p class="jwe-hint" id="jwe-caps-lock" data-caps-lock-hint hidden>${msg("jweCapsLockOn")}</p>
                </div>

                <#if realm.rememberMe && !usernameHidden??>
                <label class="jwe-check">
                    <input class="jwe-check__input" id="rememberMe" name="rememberMe" type="checkbox"<#if login.rememberMe??> checked</#if>>
                    <span>${msg("rememberMe")}</span>
                </label>
                </#if>

                <input type="hidden" id="id-hidden-input" name="credentialId"<#if auth.selectedCredential?has_content> value="${auth.selectedCredential}"</#if>>
                <button class="jwe-submit" id="kc-login" name="login" type="submit" data-submit data-label-busy="${msg("jweSigningIn")}">
                    <span class="jwe-submit__label" data-submit-label>${msg("doLogIn")}</span>
                    <span class="jwe-submit__arrow" aria-hidden="true">→</span>
                </button>
            </form>
            </#if>

            <#if realm.password && social.providers?? && social.providers?has_content>
            <div class="jwe-social">
                <p class="jwe-divider"><span>${msg("identity-provider-login-label")}</span></p>
                <ul class="jwe-social__list">
                    <#list social.providers as p>
                    <li><a class="jwe-social__link" id="social-${p.alias}" href="${p.loginUrl}">${p.displayName!}</a></li>
                    </#list>
                </ul>
            </div>
            </#if>

            <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <p class="jwe-register">${msg("noAccount")} <a class="jwe-link" href="${url.registrationUrl}">${msg("doRegister")}</a></p>
            </#if>

            <footer class="jwe-footer">
                <p>${msg("jweFooter")}</p>
                <#if realm.internationalizationEnabled && locale.supported?size gt 1>
                <nav class="jwe-locales" aria-label="${msg("languages")}">
                    <#list locale.supported?sort_by("label") as l>
                    <a class="jwe-locales__link" href="${l.url}" lang="${l.languageTag}" hreflang="${l.languageTag}" title="${l.label}"<#if l.languageTag == locale.currentLanguageTag> aria-current="true"</#if>>${l.languageTag}</a>
                    </#list>
                </nav>
                </#if>
            </footer>
        </div>
    </main>

    <aside class="jwe-cipher">
        <p class="jwe-brand"><span class="jwe-brand__mark" aria-hidden="true"></span>jwe · keycloak</p>

        <div class="jwe-cipher__body">
            <p class="jwe-headline"><span>${msg("jweHeadline")}</span> <em>${msg("jweHeadlineAccent")}</em></p>

            <figure class="jwe-token-figure" aria-hidden="true">
                <#-- base64url of {"enc":"A256GCM","alg":"RSA-OAEP-256"}: the protected header travels unencrypted and is identical in every JWE the backend issues, so it never scrambles. -->
                <p class="jwe-token" data-jwe-token><span class="jwe-token__part jwe-token__part--header">eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0</span><span class="jwe-token__dot">.</span><span class="jwe-token__part jwe-token__part--key" data-cipher-length="48">ZfpNsFQGcXtyNQ1LG5FL6mFNMcF-1ZPzUJBVm7v4bE5y_Kjs</span><span class="jwe-token__ellipsis">…</span><span class="jwe-token__dot">.</span><span class="jwe-token__part jwe-token__part--iv" data-cipher-length="22">YlQFqg7RbFF6pZNe38ySk2</span><span class="jwe-token__dot">.</span><span class="jwe-token__part jwe-token__part--ciphertext" data-cipher-length="96">ooy5zOl6R1-j7Wjh1kCHm9snhBDcB7IZIxaGyuQkwiPi6M9ji7di71Z1ErdokcXSfW94IyyWnvZxLf1WZAhyCHfN5HmWHrf2</span><span class="jwe-token__ellipsis">…</span><span class="jwe-token__dot">.</span><span class="jwe-token__part jwe-token__part--tag" data-cipher-length="22">S7Wi6T-lcVONnDXN1wtR_V</span></p>
                <figcaption>
                    <ol class="jwe-legend">
                        <li class="jwe-legend__item jwe-legend__item--header">${msg("jweLegendHeader")}</li>
                        <li class="jwe-legend__item jwe-legend__item--key">${msg("jweLegendKey")}</li>
                        <li class="jwe-legend__item jwe-legend__item--iv">${msg("jweLegendIv")}</li>
                        <li class="jwe-legend__item jwe-legend__item--ciphertext">${msg("jweLegendCiphertext")}</li>
                        <li class="jwe-legend__item jwe-legend__item--tag">${msg("jweLegendTag")}</li>
                    </ol>
                </figcaption>
            </figure>
        </div>

        <div class="jwe-cipher__foot">
            <p class="jwe-alg">RSA-OAEP-256 · A256GCM</p>
            <p class="jwe-caption">${msg("jweTokenCaption")}</p>
        </div>
    </aside>
</div>
</body>
</html>
