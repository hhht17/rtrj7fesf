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

package org.apache.shardingsphere.shadow.route.engine.dml;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.engine.util.ShadowExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Shadow update statement routing engine.
 */
@RequiredArgsConstructor
public final class ShadowUpdateStatementRoutingEngine extends AbstractShadowDMLStatementRouteEngine {
    
    private final UpdateStatementContext updateStatementContext;
    
    private final List<Object> parameters;
    
    @Override
    protected Collection<SimpleTableSegment> getAllTables() {
        return updateStatementContext.getAllTables();
    }
    
    @Override
    protected ShadowOperationType getShadowOperationType() {
        return ShadowOperationType.UPDATE;
    }
    
    @Override
    protected Optional<Collection<String>> parseSQLComments() {
        Collection<String> result = new LinkedList<>();
        updateStatementContext.getSqlStatement().getCommentSegments().forEach(each -> result.add(each.getText()));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    @Override
    protected Iterator<Optional<ShadowColumnCondition>> getShadowColumnConditionIterator(final String shadowColumn) {
        return new ShadowColumnConditionIterator(shadowColumn, parseWhereSegment());
    }
    
    private Collection<ExpressionSegment> parseWhereSegment() {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (WhereSegment each : updateStatementContext.getWhereSegments()) {
            for (AndPredicate predicate : ExpressionExtractUtils.getAndPredicates(each.getExpr())) {
                result.addAll(predicate.getPredicates());
            }
        }
        return result;
    }
    
    private final class ShadowColumnConditionIterator extends AbstractWhereSegmentShadowColumnConditionIterator {
        
        private ShadowColumnConditionIterator(final String shadowColumn, final Collection<ExpressionSegment> predicates) {
            super(shadowColumn, predicates.iterator());
        }
        
        @Override
        protected Optional<ShadowColumnCondition> nextShadowColumnCondition(final ExpressionSegment expressionSegment, final ColumnSegment columnSegment) {
            return ShadowExtractor.extractValues(expressionSegment, parameters).map(values -> new ShadowColumnCondition(getSingleTableName(), getShadowColumn(), values));
        }
    }
}
