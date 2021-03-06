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

package io.opensharding.transaction.base.hook;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import io.opensharding.transaction.base.context.ExecuteStatus;
import io.opensharding.transaction.base.context.LogicSQLTransaction;
import io.opensharding.transaction.base.context.SQLTransaction;
import io.opensharding.transaction.base.context.ShardingSQLTransaction;
import io.opensharding.transaction.base.hook.revert.DMLSQLRevertEngine;
import io.opensharding.transaction.base.hook.revert.RevertSQLResult;
import io.opensharding.transaction.base.hook.revert.executor.SQLRevertExecutorContext;
import io.opensharding.transaction.base.hook.revert.executor.SQLRevertExecutorFactory;
import io.opensharding.transaction.base.utils.Constant;
import org.apache.shardingsphere.core.execute.hook.SQLExecutionHook;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.spi.database.DataSourceMetaData;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * SQL transaction execution hook.
 *
 * @author yangyi
 * @author zhaojun
 */
public final class SQLTransactionExecutionHook implements SQLExecutionHook {
    
    private ShardingSQLTransaction shardingSQLTransaction;
    
    private SQLTransaction sqlTransaction;
    
    @Override
    public void start(final RouteUnit routeUnit, final DataSourceMetaData dataSourceMetaData, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) {
        if (!shardingExecuteDataMap.containsKey(Constant.SAGA_TRANSACTION_KEY)) {
            return;
        }
        shardingSQLTransaction = (ShardingSQLTransaction) shardingExecuteDataMap.get(Constant.SAGA_TRANSACTION_KEY);
        if (!shardingSQLTransaction.getCurrentLogicSQLTransaction().isWritableTransaction()) {
            return;
        }
        sqlTransaction = new SQLTransaction(routeUnit.getDataSourceName(), routeUnit.getSqlUnit().getSql(), splitParameters(routeUnit.getSqlUnit()), ExecuteStatus.EXECUTING);
        sqlTransaction.setRevertSQLResult(doSQLRevert(shardingSQLTransaction.getCurrentLogicSQLTransaction(), routeUnit).orNull());
        shardingSQLTransaction.addSQLTransaction(sqlTransaction);
    }
    
    @Override
    public void finishSuccess() {
        if (null != sqlTransaction) {
            sqlTransaction.setExecuteStatus(ExecuteStatus.SUCCESS);
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        if (null != sqlTransaction) {
            sqlTransaction.setExecuteStatus(ExecuteStatus.FAILURE);
        }
    }
    
    private Optional<RevertSQLResult> doSQLRevert(final LogicSQLTransaction logicSQLTransaction, final RouteUnit routeUnit) {
        SQLRevertExecutorContext context = getSqlRevertExecutorContext(logicSQLTransaction, routeUnit);
        return new DMLSQLRevertEngine(SQLRevertExecutorFactory.newInstance(context)).revert();
    }
    
    private SQLRevertExecutorContext getSqlRevertExecutorContext(LogicSQLTransaction logicSQLTransaction, RouteUnit routeUnit) {
        Connection connection = shardingSQLTransaction.getCachedConnections().get(routeUnit.getDataSourceName());
        return new SQLRevertExecutorContext(logicSQLTransaction.getLogicSQL(), logicSQLTransaction.getSqlRouteResult(), routeUnit, logicSQLTransaction.getTableMetaData(), connection);
    }
    
    private List<Collection<Object>> splitParameters(final SQLUnit sqlUnit) {
        List<Collection<Object>> result = Lists.newArrayList();
        int placeholderCount = countPlaceholder(sqlUnit.getSql());
        if (placeholderCount == sqlUnit.getParameters().size()) {
            result.add(sqlUnit.getParameters());
        } else {
            result.addAll(Lists.partition(sqlUnit.getParameters(), placeholderCount));
        }
        return result;
    }
    
    private int countPlaceholder(final String sql) {
        int result = 0;
        int currentIndex = 0;
        while (-1 != (currentIndex = sql.indexOf("?", currentIndex))) {
            result++;
            currentIndex += 1;
        }
        return result;
    }
    
}
