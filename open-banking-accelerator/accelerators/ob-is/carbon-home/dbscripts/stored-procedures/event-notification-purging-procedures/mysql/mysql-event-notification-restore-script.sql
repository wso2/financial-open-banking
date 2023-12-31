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

DROP PROCEDURE IF EXISTS `WSO2_OB_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP`;
DELIMITER $$
CREATE PROCEDURE `WSO2_OB_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP`()

BEGIN

DECLARE rowCount INT;
DECLARE enableLog BOOLEAN;
DECLARE logLevel VARCHAR(10);

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET enableLog = TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]
SET logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE



IF (enableLog) THEN
SELECT  'CLEANUP DATA RESTORATION STARTED .... !';
END IF;


-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA IN (SELECT DATABASE())  AND TABLE_NAME IN ('OB_NOTIFICATION');
IF (rowcount = 1)
THEN
IF (enableLog AND logLevel IN ('TRACE')) THEN
SELECT  'CLEANUP DATA RESTORATION STARTED ON OB_NOTIFICATION TABLE !';
END IF;
INSERT INTO OB_NOTIFICATION SELECT A.* FROM BAK_OB_NOTIFICATION A LEFT JOIN OB_NOTIFICATION B ON A.NOTIFICATION_ID = B.NOTIFICATION_ID WHERE B.NOTIFICATION_ID IS NULL;
SELECT row_count() INTO rowcount;
IF (enableLog ) THEN
SELECT  'CLEANUP DATA RESTORATION COMPLETED ON OB_NOTIFICATION WITH %',rowcount;
END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA IN (SELECT DATABASE())  AND TABLE_NAME IN ('OB_NOTIFICATION_EVENT');
IF (rowcount = 1)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
        SELECT  'CLEANUP DATA RESTORATION STARTED ON OB_NOTIFICATION_EVENT TABLE !';
    END IF;
    INSERT INTO OB_NOTIFICATION_EVENT SELECT A.* FROM BAK_OB_NOTIFICATION_EVENT A LEFT JOIN OB_NOTIFICATION_EVENT B ON A.NOTIFICATION_ID = B.NOTIFICATION_ID WHERE B.NOTIFICATION_ID IS NULL;
    SELECT row_count() INTO rowcount;
    IF (enableLog ) THEN
        SELECT  'CLEANUP DATA RESTORATION COMPLETED ON OB_NOTIFICATION_EVENT WITH %',rowcount;
    END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowcount  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA IN (SELECT DATABASE())  AND TABLE_NAME IN ('OB_NOTIFICATION_ERROR');
IF (rowcount = 1)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
        SELECT  'CLEANUP DATA RESTORATION STARTED ON OB_NOTIFICATION_ERROR TABLE !';
    END IF;
    INSERT INTO OB_NOTIFICATION_ERROR SELECT A.* FROM BAK_OB_NOTIFICATION_ERROR A LEFT JOIN OB_NOTIFICATION_ERROR B ON A.NOTIFICATION_ID = B.NOTIFICATION_ID WHERE B.NOTIFICATION_ID IS NULL;
    SELECT row_count() INTO rowcount;
    IF (enableLog ) THEN
        SELECT  'CLEANUP DATA RESTORATION COMPLETED ON OB_NOTIFICATION_ERROR WITH %',rowcount;
    END IF;
END IF;

-- ---------------------

IF (enableLog) THEN
SELECT  'CLEANUP DATA RESTORATION COMPLETED .... !';
END IF;


END$$

DELIMITER ;
