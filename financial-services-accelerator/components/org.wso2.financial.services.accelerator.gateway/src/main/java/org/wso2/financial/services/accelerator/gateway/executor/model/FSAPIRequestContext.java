/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.gateway.executor.model;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesErrorCodes;
import org.wso2.financial.services.accelerator.gateway.cache.GatewayCacheKey;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;
import org.wso2.financial.services.accelerator.gateway.util.GatewayUtils;

import java.io.UnsupportedEncodingException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.security.cert.X509Certificate;

/**
 * Open Banking executor request context.
 */
public class FSAPIRequestContext extends RequestContextDTO {

    private static final Log log = LogFactory.getLog(FSAPIRequestContext.class);
    private final RequestContextDTO requestContextDTO;
    private Map<String, String> contextProps;
    private String modifiedPayload;
    private String requestPayload;
    private Map<String, String> addedHeaders;
    private boolean isError;
    private ArrayList<FSExecutorError> errors;
    private String consentId;
    private Map<String, Object> analyticsData;
    private OpenAPI openAPI;

    public FSAPIRequestContext(RequestContextDTO requestContextDTO,
                               Map<String, String> contextProps, Map<String, Object> analyticsData) {

        this.requestContextDTO = requestContextDTO;
        this.contextProps = contextProps;
        this.addedHeaders = new HashMap<>();
        this.errors = new ArrayList<>();
        this.contextProps = new HashMap<>();
        this.analyticsData = analyticsData;

        Map<String, String> headers = requestContextDTO.getMsgInfo().getHeaders();
        String authHeader = headers.get(GatewayConstants.AUTH_HEADER);
        if (authHeader != null && !authHeader.isEmpty() &&
                GatewayUtils.isValidJWTToken(authHeader.replace(GatewayConstants.BEARER_TAG, ""))) {
            this.consentId = extractConsentID(authHeader);
        }

        String apiId = requestContextDTO.getApiRequestInfo().getApiId();
        Object cacheObject = GatewayDataHolder.getGatewayCache()
                .getFromCache(GatewayCacheKey.of(apiId));
        if (cacheObject == null) {
            String swaggerDefinition = GatewayUtils.getSwaggerDefinition(apiId);
            OpenAPIParser parser = new OpenAPIParser();
            this.openAPI = parser.readContents(swaggerDefinition, null, null).getOpenAPI();
            GatewayDataHolder.getGatewayCache().addToCache(GatewayCacheKey.of(apiId), this.openAPI);
        } else {
            this.openAPI = (OpenAPI) cacheObject;
        }
        if (requestContextDTO.getMsgInfo().getHeaders().get(GatewayConstants.CONTENT_TYPE_TAG) != null) {
            String contentType = requestContextDTO.getMsgInfo().getHeaders().get(GatewayConstants.CONTENT_TYPE_TAG);
            String httpMethod = requestContextDTO.getMsgInfo().getHttpMethod();
            String errorMessage = "Request Content-Type header does not match any allowed types";
            if (contentType.startsWith(GatewayConstants.JWT_CONTENT_TYPE) || contentType.startsWith(GatewayConstants
                    .JOSE_CONTENT_TYPE)) {
                try {
                    this.requestPayload = GatewayUtils.getTextPayload(requestContextDTO.getMsgInfo().getPayloadHandler()
                            .consumeAsString());
                } catch (Exception e) {
                    log.error(String.format("Failed to read the text payload from request. %s",
                            e.getMessage().replaceAll("\n\r", "")));
                    handleContentTypeErrors(FinancialServicesErrorCodes.INVALID_CONTENT_TYPE, errorMessage);
                }
            } else if (GatewayUtils.isEligibleRequest(contentType, httpMethod)) {
                try {
                    this.requestPayload = requestContextDTO.getMsgInfo().getPayloadHandler().consumeAsString();
                } catch (Exception e) {
                    log.error(String.format("Failed to read the payload from request. %s",
                            e.getMessage().replaceAll("\n\r", "")));
                    handleContentTypeErrors(FinancialServicesErrorCodes.INVALID_CONTENT_TYPE, errorMessage);
                }
            } else {
                this.requestPayload = null;
            }
        }
    }

    public String getModifiedPayload() {

        return modifiedPayload;
    }

    public void setModifiedPayload(String modifiedPayload) {

        this.modifiedPayload = modifiedPayload;
    }

    public Map<String, String> getAddedHeaders() {

        return addedHeaders;
    }

    public void setAddedHeaders(Map<String, String> addedHeaders) {

        this.addedHeaders = addedHeaders;
    }

    public Map<String, String> getContextProps() {

        return contextProps;
    }

    public void setContextProps(Map<String, String> contextProps) {

        this.contextProps = contextProps;
    }

    public boolean isError() {

        return isError;
    }

    public void setError(boolean error) {

        isError = error;
    }

    public ArrayList<FSExecutorError> getErrors() {

        return errors;
    }

    public void setErrors(
            ArrayList<FSExecutorError> errors) {

        this.errors = errors;
    }

    public String getConsentId() {

        return consentId;
    }

    public void setConsentId(String consentId) {

        this.consentId = consentId;
    }

    public OpenAPI getOpenAPI() {

        return openAPI;
    }

    public void setOpenAPI(OpenAPI openAPI) {

        this.openAPI = openAPI;
    }

    public Map<String, Object> getAnalyticsData() {

        return analyticsData;
    }

    public void setAnalyticsData(Map<String, Object> analyticsData) {

        this.analyticsData = analyticsData;
    }

    @Override
    public MsgInfoDTO getMsgInfo() {

        return requestContextDTO.getMsgInfo();
    }

    @Override
    public APIRequestInfoDTO getApiRequestInfo() {

        return requestContextDTO.getApiRequestInfo();
    }

    @Override
    public X509Certificate[] getClientCerts() {

        return requestContextDTO.getClientCerts();
    }

    @Override
    public Certificate[] getClientCertsLatest() {
        return requestContextDTO.getClientCertsLatest();
    }

    public String getRequestPayload() {

        return requestPayload;
    }

    private String extractConsentID(String jwtToken) {

        String consentIdClaim = null;
        try {
            if (!jwtToken.contains(GatewayConstants.BASIC_TAG)) {
                jwtToken = jwtToken.replace(GatewayConstants.BEARER_TAG, "");
                JSONObject jwtClaims = GatewayUtils.decodeBase64(GatewayUtils.getPayloadFromJWT(jwtToken));
                String consentIdClaimName =
                        GatewayDataHolder.getInstance().getFinancialServicesConfigurationService().getConfigurations()
                                .get(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME).toString();
                if (!jwtClaims.isNull(consentIdClaimName) &&
                        !jwtClaims.getString(consentIdClaimName).isEmpty()) {
                    consentIdClaim = jwtClaims.getString(consentIdClaimName);
                }
            }
        } catch (UnsupportedEncodingException | JSONException | IllegalArgumentException e) {
            log.error("Failed to retrieve the consent ID from JWT claims. %s", e);
        }
        return consentIdClaim;
    }

    public void addContextProperty(String key, String value) {

        this.contextProps.put(key, value);
    }

    public String getContextProperty(String key) {

        return this.contextProps.get(key);
    }

    private void handleContentTypeErrors(String errorCode, String errorMessage) {
        FSExecutorError error = new FSExecutorError(errorCode, errorMessage, errorMessage,
                FinancialServicesErrorCodes.UNSUPPORTED_MEDIA_TYPE_CODE);

        this.isError = true;
        this.errors.add(error);
    }

}