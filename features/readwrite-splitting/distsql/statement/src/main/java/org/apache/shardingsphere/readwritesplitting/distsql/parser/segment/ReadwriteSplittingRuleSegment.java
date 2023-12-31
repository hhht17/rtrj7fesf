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

package org.apache.shardingsphere.readwritesplitting.distsql.parser.segment;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sql.parser.api.ASTNode;

import java.util.Collection;

/**
 * Readwrite-splitting rule segment.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ReadwriteSplittingRuleSegment implements ASTNode {
    
    private final String name;
    
    private final String autoAwareResource;
    
    private final String writeDataSource;
    
    private final Collection<String> readDataSources;
    
    private final AlgorithmSegment loadBalancer;
    
    public ReadwriteSplittingRuleSegment(final String name, final String autoAwareResource, final AlgorithmSegment loadBalancer) {
        this(name, autoAwareResource, null, null, loadBalancer);
    }
    
    public ReadwriteSplittingRuleSegment(final String name, final String writeDataSource, final Collection<String> readDataSources, final AlgorithmSegment loadBalancer) {
        this(name, null, writeDataSource, readDataSources, loadBalancer);
    }
    
    /**
     * Is it an auto aware type.
     *
     * @return is auto ware or not
     */
    public boolean isAutoAware() {
        return !Strings.isNullOrEmpty(autoAwareResource);
    }
}
