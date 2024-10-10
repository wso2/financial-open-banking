/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.identity.claims;

import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.model.HttpRequestHeader;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.DefaultOIDCClaimsCallbackHandler;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This call back handler adds ob specific additional claims to self contained JWT access token.
 */
public class OBDefaultOIDCClaimsCallbackHandler extends DefaultOIDCClaimsCallbackHandler {

    private static Log log = LogFactory.getLog(OBDefaultOIDCClaimsCallbackHandler.class);
    Map<String, Object> identityConfigurations = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();


    @Override
    public JWTClaimsSet handleCustomClaims(JWTClaimsSet.Builder jwtClaimsSetBuilder, OAuthTokenReqMessageContext
            tokenReqMessageContext) throws IdentityOAuth2Exception {

        /*  accessToken property check is done to omit the following claims getting bound to id_token
             The access token property is added to the ID token message context before this method is invoked. */
        try {
            if (IdentityCommonUtil.getRegulatoryFromSPMetaData(tokenReqMessageContext.getOauth2AccessTokenReqDTO()
                    .getClientId()) && (tokenReqMessageContext.getProperty("accessToken") == null)) {

                Map<String, Object> userClaimsInOIDCDialect = new HashMap<>();
                JWTClaimsSet jwtClaimsSet = getJwtClaimsFromSuperClass(jwtClaimsSetBuilder, tokenReqMessageContext);
                if (jwtClaimsSet != null) {
                    for (Map.Entry<String, Object> claimEntry : jwtClaimsSet.getClaims().entrySet()) {
                        userClaimsInOIDCDialect.put(claimEntry.getKey(), claimEntry.getValue());
                    }
                }
                addCnfClaimToOIDCDialect(tokenReqMessageContext, userClaimsInOIDCDialect);
                addConsentIDClaimToOIDCDialect(tokenReqMessageContext, userClaimsInOIDCDialect);
                updateSubClaim(tokenReqMessageContext, userClaimsInOIDCDialect);

                for (Map.Entry<String, Object> claimEntry : userClaimsInOIDCDialect.entrySet()) {
                    if (IdentityCommonConstants.SCOPE.equals(claimEntry.getKey())) {
                        String[] nonInternalScopes = IdentityCommonUtil
                                .removeInternalScopes(claimEntry.getValue().toString()
                                        .split(IdentityCommonConstants.SPACE_SEPARATOR));
                        jwtClaimsSetBuilder.claim(IdentityCommonConstants.SCOPE, StringUtils.join(nonInternalScopes,
                                IdentityCommonConstants.SPACE_SEPARATOR));
                    } else {
                        jwtClaimsSetBuilder.claim(claimEntry.getKey(), claimEntry.getValue());
                    }
                }
                return jwtClaimsSetBuilder.build();
            }
        } catch (OpenBankingException e) {
            throw new IdentityOAuth2Exception(e.getMessage(), e);
        }
        return super.handleCustomClaims(jwtClaimsSetBuilder, tokenReqMessageContext);
    }

    @Override
    public JWTClaimsSet handleCustomClaims(JWTClaimsSet.Builder jwtClaimsSet, OAuthAuthzReqMessageContext
            authzReqMessageContext) throws IdentityOAuth2Exception {
        try {
            if (IdentityCommonUtil.getRegulatoryFromSPMetaData(
                    authzReqMessageContext.getAuthorizationReqDTO().getConsumerKey())) {
                Map<String, Object> claims = new HashMap<>();
                JWTClaimsSet claimsSet = getJwtClaimsFromSuperClass(jwtClaimsSet, authzReqMessageContext);
                if (claimsSet != null) {
                    for (Map.Entry<String, Object> claimEntry : claimsSet.getClaims().entrySet()) {
                        claims.put(claimEntry.getKey(), claimEntry.getValue());
                    }
                }
                updateSubClaim(authzReqMessageContext, claims);
                for (Map.Entry<String, Object> claimEntry : claims.entrySet()) {
                    jwtClaimsSet.claim(claimEntry.getKey(), claimEntry.getValue());
                }
                return jwtClaimsSet.build();
            }
        } catch (OpenBankingException e) {
            throw new IdentityOAuth2Exception(e.getMessage(), e);
        }
        return getJwtClaimsFromSuperClass(jwtClaimsSet, authzReqMessageContext);
    }

    /**
     * Update the subject claim of the JWT claims set base on below configurations.
     * 1. open_banking.identity.token.remove_tenant_domain_from_subject
     * 2. open_banking.identity.token.remove_user_store_domain_from_subject
     *
     * @param authzReqMessageContext token request message context
     * @param claims                 user claims in OIDC dialect as a map
     */
    private void updateSubClaim(OAuthAuthzReqMessageContext authzReqMessageContext, Map<String, Object> claims) {

        Object removeTenantDomainConfig =
                identityConfigurations.get(IdentityCommonConstants.REMOVE_TENANT_DOMAIN_FROM_SUBJECT);
        boolean removeTenantDomain = removeTenantDomainConfig != null
                && Boolean.parseBoolean(removeTenantDomainConfig.toString());

        Object removeUserStoreDomainConfig =
                identityConfigurations.get(IdentityCommonConstants.REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT);
        boolean removeUserStoreDomain = removeUserStoreDomainConfig != null
                && Boolean.parseBoolean(removeUserStoreDomainConfig.toString());

        String subClaim = authzReqMessageContext.getAuthorizationReqDTO().getUser()
                .getUsernameAsSubjectIdentifier(removeUserStoreDomain, removeTenantDomain);
        claims.put("sub", subClaim);
    }

    @Generated(message = "Excluding from code coverage since it makes is used to return claims from the super class")
    public JWTClaimsSet getJwtClaimsFromSuperClass(JWTClaimsSet.Builder jwtClaimsSetBuilder,
                                                   OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext)
            throws IdentityOAuth2Exception {

        return super.handleCustomClaims(jwtClaimsSetBuilder, oAuthAuthzReqMessageContext);
    }

    @Generated(message = "Excluding from code coverage since it makes is used to return claims from the super class")
    public JWTClaimsSet getJwtClaimsFromSuperClass(JWTClaimsSet.Builder jwtClaimsSetBuilder,
                                                   OAuthTokenReqMessageContext tokenReqMessageContext)
            throws IdentityOAuth2Exception {

        return super.handleCustomClaims(jwtClaimsSetBuilder, tokenReqMessageContext);
    }

    private void addCnfClaimToOIDCDialect(OAuthTokenReqMessageContext tokenReqMessageContext,
                                          Map<String, Object> userClaimsInOIDCDialect) {
        Base64URL certThumbprint;
        X509Certificate certificate;
        String headerName = IdentityCommonUtil.getMTLSAuthHeader();

        HttpRequestHeader[] requestHeaders = tokenReqMessageContext.getOauth2AccessTokenReqDTO()
                .getHttpRequestHeaders();
        Optional<HttpRequestHeader> certHeader =
                Arrays.stream(requestHeaders).filter(h -> headerName.equals(h.getName())).findFirst();
        if (certHeader.isPresent()) {
            try {
                certificate = CertificateUtils.parseCertificate(certHeader.get().getValue()[0]);
                certThumbprint = X509CertUtils.computeSHA256Thumbprint(certificate);
                userClaimsInOIDCDialect.put("cnf", Collections.singletonMap("x5t#S256", certThumbprint));
            } catch (OpenBankingException e) {
                log.error("Error while extracting the certificate", e);
            }
        }
    }

    private void addConsentIDClaimToOIDCDialect(OAuthTokenReqMessageContext tokenReqMessageContext,
                                                Map<String, Object> userClaimsInOIDCDialect) {

        String consentIdClaimName =
                identityConfigurations.get(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();
        String consentID = Arrays.stream(tokenReqMessageContext.getScope())
                .filter(scope -> scope.contains(IdentityCommonConstants.OB_PREFIX)).findFirst().orElse(null);
        if (StringUtils.isEmpty(consentID)) {
            consentID = Arrays.stream(tokenReqMessageContext.getScope())
                    .filter(scope -> scope.contains(consentIdClaimName))
                    .findFirst().orElse(StringUtils.EMPTY)
                    .replaceAll(consentIdClaimName, StringUtils.EMPTY);
        } else {
            consentID = consentID.replace(IdentityCommonConstants.OB_PREFIX, StringUtils.EMPTY);
        }

        if (StringUtils.isNotEmpty(consentID)) {
            userClaimsInOIDCDialect.put(consentIdClaimName, consentID);
        }
    }

    /**
     * Update the subject claim of the JWT claims set if any of the following configurations are true
     *  1. Remove tenant domain from subject (open_banking.identity.token.remove_tenant_domain_from_subject)
     *  2. Remove user store domain from subject (open_banking.identity.token.remove_user_store_domain_from_subject)
     * @param tokenReqMessageContext token request message context
     * @param userClaimsInOIDCDialect user claims in OIDC dialect as a map
     */
    private void updateSubClaim(OAuthTokenReqMessageContext tokenReqMessageContext,
                                Map<String, Object> userClaimsInOIDCDialect) {

        Object removeTenantDomainConfig =
                identityConfigurations.get(IdentityCommonConstants.REMOVE_TENANT_DOMAIN_FROM_SUBJECT);
        Boolean removeTenantDomain = removeTenantDomainConfig != null
                && Boolean.parseBoolean(removeTenantDomainConfig.toString());

        Object removeUserStoreDomainConfig =
                identityConfigurations.get(IdentityCommonConstants.REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT);
        Boolean removeUserStoreDomain = removeUserStoreDomainConfig != null
                && Boolean.parseBoolean(removeUserStoreDomainConfig.toString());

        if (removeTenantDomain || removeUserStoreDomain) {
            String subClaim = tokenReqMessageContext.getAuthorizedUser()
                    .getUsernameAsSubjectIdentifier(!removeUserStoreDomain, !removeTenantDomain);
            userClaimsInOIDCDialect.put("sub", subClaim);
        }
    }
}
