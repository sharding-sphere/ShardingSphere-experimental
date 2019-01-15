/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.saga.persistence;

import io.shardingsphere.transaction.saga.persistence.impl.EmptySagaPersistence;

import java.util.ServiceLoader;

/**
 * Saga persistence loader.
 *
 * @author yangyi
 */
public final class SagaPersistenceLoader {
    
    /**
     * Load saga persistence.
     *
     * @param isEnablePersistence is enable persistence
     * @return saga persistence
     */
    public static SagaPersistence load(final boolean isEnablePersistence) {
        SagaPersistence result = null;
        if (isEnablePersistence) {
            for (SagaPersistence each : ServiceLoader.load(SagaPersistence.class)) {
                result = each;
            }
        }
        if (null == result) {
            result = new EmptySagaPersistence();
        }
        return result;
    }
}