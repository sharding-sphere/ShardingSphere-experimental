/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.section;

import io.opensharding.orchestration.reg.zookeeper.natived.client.retry.DelayRetryPolicy;
import io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.base.BaseClientFactory;
import io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.base.BaseContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Client context.
 *
 * @author lidongbo
 */
@Setter
@Getter
public final class ClientContext extends BaseContext {
    
    private DelayRetryPolicy delayRetryPolicy;
    
    private BaseClientFactory clientFactory;
    
    public ClientContext(final String servers, final int sessionTimeoutMilliseconds) {
        setServers(servers);
        setSessionTimeOut(sessionTimeoutMilliseconds);
    }
    
    /**
     * Update context.
     *
     * @param context context
     */
    public void updateContext(final ClientContext context) {
        delayRetryPolicy = context.getDelayRetryPolicy();
        clientFactory = context.clientFactory;
        getWatchers().clear();
        getWatchers().putAll(context.getWatchers());
    }
    
    @Override
    public void close() {
        super.close();
        delayRetryPolicy = null;
        clientFactory = null;
    }
}
