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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;

/**
 * Consent retrieval step interface.
 */
public interface ConsentRetrievalStep {

    /**
     * Method to be implemented as a step for consent retrieval. Once implemented add the step to the configuration.
     *
     * @param consentData Includes all the data that is received to the consent page.
     * @param jsonObject  Passed on through each step where the response body sent to the page is built.
     * @throws ConsentException Exception thrown in case of failure.
     */
    void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException;
}
