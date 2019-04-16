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

package io.shardingsphere.transaction.saga.revert.impl.delete;

import io.shardingsphere.transaction.saga.revert.api.DMLSnapshotDataAccessor;
import io.shardingsphere.transaction.saga.revert.api.SnapshotParameter;
import io.shardingsphere.transaction.saga.revert.impl.AbstractRevertOperate;
import io.shardingsphere.transaction.saga.revert.impl.RevertContextGeneratorParameter;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.DeleteStatement;

import java.sql.SQLException;
import java.util.List;

/**
 * Revert delete.
 *
 * @author duhongjun
 */
@Getter
@Setter
public final class RevertDelete extends AbstractRevertOperate {
    
    private DMLSnapshotDataAccessor snapshotDataAccessor;
    
    public RevertDelete(final String actualTableName, final DeleteStatement deleteStatement, final List<Object> actualSQLParameters) {
        snapshotDataAccessor = new DMLSnapshotDataAccessor(new DeleteSnapshotSQLSegment(actualTableName, deleteStatement, actualSQLParameters));
        this.setRevertSQLGenerator(new RevertDeleteGenerator());
    }
    
    @Override
    protected RevertContextGeneratorParameter createRevertContext(final SnapshotParameter snapshotParameter, final List<String> keys) throws SQLException {
        return new RevertDeleteParameter(snapshotParameter.getActualTable(), snapshotDataAccessor.queryUndoData(snapshotParameter.getConnection()));
    }
}
