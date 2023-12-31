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

package com.wso2.openbanking.accelerator.data.publisher.common.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.data.publisher.common.DataPublisherFactory;
import com.wso2.openbanking.accelerator.data.publisher.common.DataPublisherPool;
import com.wso2.openbanking.accelerator.data.publisher.common.EventQueue;
import com.wso2.openbanking.accelerator.data.publisher.common.OpenBankingDataPublisher;
import com.wso2.openbanking.accelerator.data.publisher.common.constants.DataPublishingConstants;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPoolConfig;

import java.util.Map;

/**
 * Data holder for Open Banking Analytics.
 */
public class OBAnalyticsDataHolder {

    private static volatile OBAnalyticsDataHolder instance;
    private OpenBankingConfigurationService openBankingConfigurationService;
    private Map<String, Object> configurationMap;
    private DataPublisherPool<OpenBankingDataPublisher> pool;
    private int poolSize;
    private EventQueue eventQueue;

    public static OBAnalyticsDataHolder getInstance() {

        if (instance == null) {
            synchronized (OBAnalyticsDataHolder.class) {
                if (instance == null) {
                    instance = new OBAnalyticsDataHolder();
                }
            }
        }
        return instance;
    }

    public Map<String, Object> getConfigurationMap() {

        return configurationMap;
    }

    public OpenBankingConfigurationService getOpenBankingConfigurationService() {

        return openBankingConfigurationService;
    }

    public void setOpenBankingConfigurationService(
            OpenBankingConfigurationService openBankingConfigurationService) {

        this.openBankingConfigurationService = openBankingConfigurationService;
        this.configurationMap = openBankingConfigurationService.getConfigurations();
    }

    /**
     * Initialize pool of data publishers.
     */
    public void initializePool() {

        GenericObjectPoolConfig<OpenBankingDataPublisher> config = new GenericObjectPoolConfig<>();
        poolSize = Integer.parseInt((String) configurationMap.get(DataPublishingConstants.DATA_PUBLISHING_POOL_SIZE));
        int timeout = Integer.parseInt(
                (String) configurationMap.get(DataPublishingConstants.DATA_PUBLISHING_POOL_WAIT_TIME));
        config.setMaxIdle(poolSize);
        config.setMaxTotal(poolSize);
        config.setMaxWaitMillis(timeout);
        pool = new DataPublisherPool<>(new DataPublisherFactory<>(), config);
    }

    public DataPublisherPool<OpenBankingDataPublisher> getDataPublisherPool() {

        return pool;
    }

    public void closePool() {

        pool.close();
    }

    public void initializeEventQueue() {

        int queueSize = Integer.parseInt((String) configurationMap.get(DataPublishingConstants.QUEUE_SIZE));
        int workerThreadCount =
                Integer.parseInt((String) configurationMap.get(DataPublishingConstants.WORKER_THREAD_COUNT));
        eventQueue = new EventQueue(queueSize, workerThreadCount);
    }

    public EventQueue getEventQueue() {

        return eventQueue;
    }

    @Generated(message = "Event queue setter for testing purposes")
    public void setEventQueue(EventQueue eventQueue) {

        this.eventQueue = eventQueue;
    }

}
