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

package io.shardingsphere.transaction.saga.hook;

import io.shardingsphere.transaction.saga.SagaShardingTransactionManager;
import io.shardingsphere.transaction.saga.context.SagaTransaction;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.hook.ShardHook;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.route.SQLRouteResult;

/**
 * Saga SQL shard hook.
 *
 * @author zhaojun
 */
public final class SagaSQLShardHook implements ShardHook {
    
    private final SagaTransaction sagaTransaction = SagaShardingTransactionManager.getCurrentTransaction();
    
    private String sql;
    
    @Override
    public void start(final String sql) {
        this.sql = sql;
    }
    
    @Override
    public void finishSuccess(final SQLRouteResult sqlRouteResult, final ShardingTableMetaData shardingTableMetaData) {
        if (null != sagaTransaction && SQLType.DML.equals(sqlRouteResult.getSqlStatement().getType())) {
            sagaTransaction.nextLogicSQLTransaction(sql, sqlRouteResult, shardingTableMetaData);
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
    }
}