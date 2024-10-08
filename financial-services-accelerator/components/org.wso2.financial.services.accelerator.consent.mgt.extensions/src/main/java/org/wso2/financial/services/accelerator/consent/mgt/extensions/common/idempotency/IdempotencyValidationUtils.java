/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common.idempotency;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to hold idempotency validation utils.
 */
public class IdempotencyValidationUtils {

    private static final Log log = LogFactory.getLog(IdempotencyValidationUtils.class);
    private static final ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance()
            .getConsentCoreService();

    /**
     * Method to retrieve the consent ids that have the idempotency key name and value as attribute.
     *
     * @param idempotencyKeyName     Idempotency Key Name
     * @param idempotencyKeyValue    Idempotency Key Value
     * @return   List of consent ids if available, else an empty list will be returned
     */
    static List<String> getConsentIdsFromIdempotencyKey(String idempotencyKeyName,
                                                        String idempotencyKeyValue) {
        try {
            return consentCoreService.getConsentIdByConsentAttributeNameAndValue(
                    idempotencyKeyName, idempotencyKeyValue);
        } catch (ConsentManagementException e) {
            log.debug("No consent ids found for the idempotency key value");
            return new ArrayList<>();
        }
    }

    /**
     * Method to retrieve the consent ids and idempotency key value using the idempotency key.
     *
     * @param idempotencyKeyName     Idempotency Key Name
     * @return   Map of consent ids and idempotency key vallue if available, else an empty map will be returned
     */
    static Map<String, String> getAttributesFromIdempotencyKey(String idempotencyKeyName) {
        try {
            return consentCoreService.getConsentAttributesByName(idempotencyKeyName);
        } catch (ConsentManagementException e) {
            log.debug("No consent ids found for the idempotency key value");
            return new HashMap<>();
        }
    }

    /**
     * Method to compare the client ID sent in the request and client id retrieved from the database.
     *
     * @param requestClientID     Client ID sent in the request
     * @param dbClientID          client ID retrieved from the database
     * @return   true if the client ID sent in the request and client id retrieved from the database are equal
     */
    static boolean isClientIDEqual(String requestClientID, String dbClientID) {
        if (requestClientID == null) {
            return false;
        }
        return requestClientID.equals(dbClientID);
    }

    /**
     * Method to check whether difference between two dates is less than the configured time.
     *
     * @param createdTime Created Time of the request
     * @return  true if the request is received within allowed time
     */
    static boolean isRequestReceivedWithinAllowedTime(long createdTime) {

        if (createdTime == 0L) {
            log.debug("Created time is of the previous request is not correctly set. Hence returning false");
            return false;
        }
        String allowedTimeDuration = FinancialServicesConfigParser.getInstance().getIdempotencyAllowedTime();
        if (StringUtils.isNotBlank(allowedTimeDuration)) {
            OffsetDateTime createdDate = OffsetDateTime.parse(toISO8601DateTime(createdTime));
            OffsetDateTime currDate = OffsetDateTime.now(createdDate.getOffset());

            long diffInHours = Duration.between(createdDate, currDate).toHours();
            return diffInHours <= Long.parseLong(allowedTimeDuration);
        } else {
            log.error("Idempotency allowed duration is not configured in the system. Hence returning false");
            return false;
        }
    }

    /**
     * Convert long date values to ISO 8601 format. ISO 8601 format - "yyyy-MM-dd'T'HH:mm:ssXXX"
     * @param epochDate     Date value in epoch format
     * @return ISO 8601 formatted date
     */
    private static String toISO8601DateTime(long epochDate) {

        DateFormat simple = new SimpleDateFormat(IdempotencyConstants.ISO_FORMAT);
        Date simpleDateVal = new Date(epochDate * 1000);
        return simple.format(simpleDateVal);
    }
}
