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

package com.wso2.openbanking.scp.webapp.util;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.model.SCPError;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utils.
 * <p>
 * Contains utility methods used by self-care portal web application.
 */
public class Utils {
    private static final Log LOG = LogFactory.getLog(Utils.class);

    private Utils() {
        // hiding constructor
    }

    public static JSONObject sendRequest(HttpUriRequest request)
            throws TokenGenerationException {

        LOG.debug("Sending request to " + request.getURI());
        String responseStr = null;
        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                responseStr = EntityUtils.toString(responseEntity);
            }
            if ((response.getStatusLine().getStatusCode() / 100) != 2) {
                if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    LOG.debug("Received unauthorized(401) response. body: " + responseStr);
                    throw new TokenGenerationException("Received unauthorized Response: " + responseStr);
                } else if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                    JSONObject responseJson = new JSONObject(response.getLastHeader("Location"));
                    return responseJson;
                }
            } else {
                // received success (200 range) response
                JSONObject responseJson;
                if (StringUtils.isNotEmpty(responseStr)) {
                    responseJson = new JSONObject(responseStr);
                } else {
                    responseJson = new JSONObject();
                }
                responseJson.put("res_status_code", response.getStatusLine().getStatusCode());
                return responseJson;
            }

        } catch (IOException e) {
            LOG.error("Exception occurred while forwarding request. Caused by, ", e);
        } catch (OpenBankingException e) {
            LOG.error("Exception occurred while generating http client. Caused by, ", e);
        }
        throw new TokenGenerationException("Unexpected response received for the request. path: " +
                request.getURI() + " response:" + responseStr);
    }

    public static JSONObject sendTokenRequest(HttpPost tokenReq) throws TokenGenerationException {
        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpResponse response = client.execute(tokenReq);
            String responseStr = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // received success response
                return new JSONObject(responseStr);
            }
            LOG.error("Invalid response received for token request. Error: " + responseStr);
        } catch (IOException e) {
            LOG.error("Exception occurred while sending token request. Caused by, ", e);
        } catch (JSONException e) {
            LOG.error("Exception occurred while processing token response. Caused by, ", e);
        } catch (OpenBankingException e) {
            LOG.error("Exception occurred while generating http client. Caused by, ", e);
        }
        throw new TokenGenerationException("Invalid response received for token request");
    }

    public static HttpUriRequest getHttpUriRequest(String apimBaseUrl, String requestMethod,
                                                   String queryParams) {
        if (HttpDelete.METHOD_NAME.equals(requestMethod)) {
            // Generate HTTP DELETE request for consent delete endpoint
            return commonHttpUriRequest(apimBaseUrl, Constants.PATH_APIM_CONSENT_REVOKE_V1, requestMethod,
                    queryParams, null, null);
        }
        return commonHttpUriRequest(apimBaseUrl, Constants.PATH_APIM_CONSENT_SEARCH_V1, requestMethod, queryParams,
                null, null);
    }

    public static HttpUriRequest commonHttpUriRequest(String baseUrl, String path, String requestMethod,
                                                      String queryParams, String pathParams, HttpEntity data) {
        switch (requestMethod) {
            case HttpDelete.METHOD_NAME:
                    return new HttpDelete(generateURL(baseUrl, path, pathParams, queryParams));
            case HttpPost.METHOD_NAME:
                    HttpPatch patchRequest = new HttpPatch(generateURL(baseUrl, path, pathParams, queryParams));
                    patchRequest.setEntity(data);
                    return patchRequest;
            default:
                    return new HttpGet(generateURL(baseUrl, path, pathParams,
                            queryParams));
        }
    }

    private static String generateURL(final String baseUrl, final String path, final String pathParams,
                                      final String queryParams) {
        String url = baseUrl + path;
        if (pathParams != null) {
            url = url + "/" + pathParams;
        }
        if (queryParams != null) {
            url = url + "?" + queryParams;
        }
        return url;
    }

    public static Optional<Cookie> getCookieFromRequest(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst();
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static void returnResponse(HttpServletResponse resp, int statusCode, JSONObject payload) {
        try {
            LOG.debug("Returning response to frontend. payload: " + payload.toString());
            // returning  response
            resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            resp.setStatus(statusCode);
            PrintWriter out = resp.getWriter();
            out.print(payload);
            out.flush();
        } catch (IOException e) {
            LOG.error("Exception occurred while returning response. Caused by, ", e);
        }
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static void sendErrorToFrontend(SCPError error, String errRedirectUrlFormat, HttpServletResponse resp) {
        LOG.debug("Sending error to frontend.");
        try {
            final String errorMsg = Base64.getUrlEncoder()
                    .encodeToString(error.getMessage().getBytes(StandardCharsets.UTF_8));
            final String errorDesc = Base64.getUrlEncoder()
                    .encodeToString((error.getDescription()).getBytes(StandardCharsets.UTF_8));

            final String errorUrl = String.format(errRedirectUrlFormat, errorMsg, errorDesc);
            resp.sendRedirect(errorUrl);
        } catch (IOException ex) {
            LOG.error("Exception occurred while redirecting to frontend. Caused by, ", ex);
        }
    }

    /**
     * Retrieve consent management portal related configurations from webapp properties or OB configs.
     */
    public static String getParameter(String requiredParameter) {
        String parameter;
        try {
            InputStream configurations = Utils.class.getClassLoader()
                    .getResourceAsStream(Constants.CONFIG_FILE_NAME);
            Properties configurationProperties = new Properties();
            configurationProperties.load(configurations);
            Boolean isConfiguredInWebapp = Boolean.parseBoolean(
                    configurationProperties.getProperty(Constants.LOCATION_OF_CREDENTIALS));
            if (!isConfiguredInWebapp) {
                parameter = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                        .get(requiredParameter);
            } else {
                parameter = configurationProperties.getProperty(requiredParameter);
            }
        } catch (IOException | NullPointerException e) {
            LOG.error("Error occurred while reading the webapp properties file. Therefore using OB configurations.");
            parameter = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                    .get(requiredParameter);
        }
        return parameter;
    }

    /**
     * Create basic authorization header.
     */
    public static String getConsentApiCredentials () {
        final String userName = getParameter(Constants.CONFIGURED_USERNAME);
        final String password = getParameter(Constants.CONFIGURED_PASSWORD);
        String authorizationHeader = Base64.getEncoder().encodeToString((userName + ":" + password)
                .getBytes(StandardCharsets.UTF_8));

        return authorizationHeader;
    }

    /**
     * Extract the payload from the request body.
     */
    public static String getPayload(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder payload = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            payload.append(line);
        }
        return payload.toString();
    }
}
