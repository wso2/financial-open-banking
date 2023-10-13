CREATE TABLE OB_NOTIFICATION (
    NOTIFICATION_ID varchar2(36) NOT NULL,
    CLIENT_ID varchar2(255) NOT NULL,
    RESOURCE_ID varchar2(255) NOT NULL,
    STATUS varchar2(10) NOT NULL,
    UPDATED_TIMESTAMP TIMESTAMP(0) DEFAULT SYSTIMESTAMP,
    PRIMARY KEY (NOTIFICATION_ID)
);

CREATE TABLE OB_NOTIFICATION_EVENT (
    EVENT_ID number(10) NOT NULL,
    NOTIFICATION_ID varchar2(36) NOT NULL,
    EVENT_TYPE varchar2(200) NOT NULL,
    EVENT_INFO varchar2(1000) NOT NULL,
    PRIMARY KEY (EVENT_ID),
    CONSTRAINT FK_NotificationEvent FOREIGN KEY (NOTIFICATION_ID) REFERENCES OB_NOTIFICATION(NOTIFICATION_ID)
);

-- Generate ID using sequence and trigger
CREATE SEQUENCE OB_NOTIFICATION_EVENT_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER OB_NOTIFICATION_EVENT_seq_tr
 BEFORE INSERT ON OB_NOTIFICATION_EVENT FOR EACH ROW
 WHEN (NEW.EVENT_ID IS NULL)
BEGIN
 SELECT OB_NOTIFICATION_EVENT_seq.NEXTVAL INTO :NEW.EVENT_ID FROM DUAL;
END;


CREATE TABLE OB_NOTIFICATION_ERROR (
    NOTIFICATION_ID varchar2(36) NOT NULL,
    ERROR_CODE varchar2(255) NOT NULL,
    DESCRIPTION varchar2(255) NOT NULL,
    PRIMARY KEY (NOTIFICATION_ID),
    CONSTRAINT FK_NotificationError FOREIGN KEY (NOTIFICATION_ID) REFERENCES OB_NOTIFICATION(NOTIFICATION_ID)
)

CREATE TABLE OB_NOTIFICATION_SUBSCRIPTION (
    SUBSCRIPTION_ID varchar(36) NOT NULL,
    CLIENT_ID varchar(255) NOT NULL,
    REQUEST JSON NOT NULL,
    CALLBACK_URL varchar(255),
    TIMESTAMP BIGINT NOT NULL,
    SPEC_VERSION varchar(255),
    STATUS varchar(255) NOT NULL,
    PRIMARY KEY (SUBSCRIPTION_ID)
);

CREATE TABLE OB_NOTIFICATION_SUBSCRIBED_EVENTS (
    SUBSCRIPTION_ID varchar(36) NOT NULL,
    EVENT_TYPE varchar(255) NOT NULL,
    PRIMARY KEY (SUBSCRIPTION_ID, EVENT_TYPE),
    CONSTRAINT FK_NotificationSubEvents FOREIGN KEY (SUBSCRIPTION_ID) REFERENCES OB_NOTIFICATION_SUBSCRIPTION(SUBSCRIPTION_ID)
);


