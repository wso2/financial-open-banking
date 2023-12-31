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

CREATE OR REPLACE PROCEDURE WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP IS


rowCount INT;
CURRENT_SCHEMA VARCHAR(100);
-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
enableLog BOOLEAN := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]
logLevel VARCHAR(10):= 'TRACE'; -- SET LOG LEVELS : TRACE


BEGIN

SELECT SYS_CONTEXT( 'USERENV', 'CURRENT_SCHEMA' ) INTO CURRENT_SCHEMA FROM DUAL;

IF (enableLog)
THEN
    SELECT COUNT(*) INTO rowCount from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP');
    IF (rowCount = 1) then
    EXECUTE IMMEDIATE 'DROP TABLE LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP';
    COMMIT;
    END if;
    EXECUTE IMMEDIATE 'CREATE TABLE LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP VARCHAR(250) , LOG VARCHAR(250)) NOLOGGING';
    COMMIT;
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''WSO2_OB_CONSENT_CLEANUP_DATA_RESTORATION_SP STARTED .... !'')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''USING SCHEMA :'||CURRENT_SCHEMA||''')';
    COMMIT;
END IF;


-- ---------------------

SELECT COUNT(1) INTO rowCount FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND TABLE_NAME IN ('OB_CONSENT');
IF (rowCount = 1)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION STARTED ON OB_CONSENT TABLE !'')';
    COMMIT;
    END IF;
    EXECUTE IMMEDIATE 'INSERT INTO OB_CONSENT SELECT A.* FROM BAK_OB_CONSENT A LEFT JOIN OB_CONSENT B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL';
    rowCount:=  sql%Rowcount;
    IF (enableLog ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT WITH '||rowCount||''')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    COMMIT;
    END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowCount FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND TABLE_NAME IN ('OB_CONSENT_ATTRIBUTE');
IF (rowCount = 1)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_ATTRIBUTE TABLE !'')';
    COMMIT;
    END IF;
    EXECUTE IMMEDIATE 'INSERT INTO OB_CONSENT_ATTRIBUTE SELECT A.* FROM BAK_OB_CONSENT_ATTRIBUTE A LEFT JOIN OB_CONSENT_ATTRIBUTE B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL';
    rowCount:=  sql%Rowcount;
    IF (enableLog ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_ATTRIBUTE WITH '||rowCount||''')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    COMMIT;
    END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowCount FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND TABLE_NAME IN ('OB_CONSENT_FILE');
IF (rowCount = 1)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_FILE TABLE !'')';
    COMMIT;
    END IF;
    EXECUTE IMMEDIATE 'INSERT INTO OB_CONSENT_FILE SELECT A.* FROM BAK_OB_CONSENT_FILE A LEFT JOIN OB_CONSENT_FILE B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL';
    rowCount:=  sql%Rowcount;
    IF (enableLog ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_FILE WITH '||rowCount||''')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    COMMIT;
    END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowCount FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND TABLE_NAME IN ('OB_CONSENT_STATUS_AUDIT');
IF (rowCount = 1)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_STATUS_AUDIT TABLE !'')';
    COMMIT;
    END IF;
    EXECUTE IMMEDIATE 'INSERT INTO OB_CONSENT_STATUS_AUDIT SELECT A.* FROM BAK_OB_CONSENT_STATUS_AUDIT A LEFT JOIN OB_CONSENT_STATUS_AUDIT B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL';
    rowCount:=  sql%Rowcount;
    IF (enableLog ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_STATUS_AUDIT WITH '||rowCount||''')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    COMMIT;
    END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowCount FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND TABLE_NAME IN ('OB_CONSENT_AUTH_RESOURCE');
IF (rowCount = 1)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_AUTH_RESOURCE TABLE !'')';
    COMMIT;
    END IF;
    EXECUTE IMMEDIATE 'INSERT INTO OB_CONSENT_AUTH_RESOURCE SELECT A.* FROM BAK_OB_CONSENT_AUTH_RESOURCE A LEFT JOIN OB_CONSENT_AUTH_RESOURCE B ON A.CONSENT_ID = B.CONSENT_ID WHERE B.CONSENT_ID IS NULL';
    rowCount:=  sql%Rowcount;
    IF (enableLog ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_AUTH_RESOURCE WITH '||rowCount||''')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    COMMIT;
    END IF;
END IF;

-- ---------------------

SELECT COUNT(1) INTO rowCount FROM ALL_TABLES where OWNER = CURRENT_SCHEMA AND TABLE_NAME IN ('OB_CONSENT_MAPPING');
IF (rowCount = 1)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION STARTED ON OB_CONSENT_MAPPING TABLE !'')';
    COMMIT;
    END IF;
    EXECUTE IMMEDIATE 'INSERT INTO OB_CONSENT_MAPPING SELECT A.* FROM BAK_OB_CONSENT_MAPPING A LEFT JOIN OB_CONSENT_MAPPING B ON A.MAPPING_ID = B.MAPPING_ID WHERE B.MAPPING_ID IS NULL';
    rowCount:=  sql%Rowcount;
    IF (enableLog ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION COMPLETED ON OB_CONSENT_MAPPING WITH '||rowCount||''')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    COMMIT;
    END IF;
END IF;

-- ---------------------

IF (enableLog)
THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP COMPLETED .... !'')';
    COMMIT;
END IF;

END;
