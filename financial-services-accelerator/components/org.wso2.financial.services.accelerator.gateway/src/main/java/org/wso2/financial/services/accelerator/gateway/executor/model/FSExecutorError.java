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

import java.util.Map;

/**
 * Error model for Financial Services executors.
 */
public class FSExecutorError {

    private String code;
    private String title;
    private String message;
    private String httpStatusCode;
    private Map<String, String> links;

    public FSExecutorError() {}

    public FSExecutorError(String errorCode) {
        this.code = errorCode;
    }


    public FSExecutorError(String code, String title, String message, String httpStatusCode) {
        this.code = code;
        this.title = title;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    public FSExecutorError(String code, String title, String message, String httpStatusCode,
                           Map<String, String> links) {
        this.code = code;
        this.title = title;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
        this.links = links;
    }

    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getHttpStatusCode() {

        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {

        this.httpStatusCode = httpStatusCode;
    }

    public Map<String, String> getLinks() {

        return links;
    }

    public void setLinks(Map<String, String> links) {

        this.links = links;
    }
}
