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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.update;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryProviderAlgorithmSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class AlterDatabaseDiscoveryProviderAlgorithmStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final AlterDatabaseDiscoveryTypeStatementUpdater updater = new AlterDatabaseDiscoveryTypeStatementUpdater();
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicate() throws DistSQLException {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "test");
        List<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments = Arrays.asList(
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())),
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        updater.checkSQLStatement(shardingSphereMetaData, new AlterDatabaseDiscoveryTypeStatement(algorithmSegments),
                new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithNotExist() throws DistSQLException {
        List<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments =
                Collections.singletonList(new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type_1", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        updater.checkSQLStatement(shardingSphereMetaData, new AlterDatabaseDiscoveryTypeStatement(algorithmSegments),
                new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(),
                        Collections.singletonMap("discovery_type", new ShardingSphereAlgorithmConfiguration("DISTSQL.FIXTURE", new Properties()))));
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() throws DistSQLException {
        Collection<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments = Collections.singleton(
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("INVALID_TYPE", new Properties())));
        updater.checkSQLStatement(shardingSphereMetaData, new AlterDatabaseDiscoveryTypeStatement(algorithmSegments),
                new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(),
                        Collections.singletonMap("discovery_type", new ShardingSphereAlgorithmConfiguration("DISTSQL.FIXTURE", new Properties()))));
    }
    
    @Test
    public void assertBuildAndUpdate() throws DistSQLException {
        Properties currentProps = new Properties();
        currentProps.put("key", "value");
        DatabaseDiscoveryRuleConfiguration currentRuleConfig = new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(),
                Collections.singletonMap("discovery_type", new ShardingSphereAlgorithmConfiguration("DISTSQL.FIXTURE", currentProps)));
        Properties props = new Properties();
        props.put("key", "value_1");
        Set<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments = Collections.singleton(
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("DISTSQL.FIXTURE", props)));
        updater.checkSQLStatement(shardingSphereMetaData, new AlterDatabaseDiscoveryTypeStatement(algorithmSegments), currentRuleConfig);
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig =
                (DatabaseDiscoveryRuleConfiguration) updater.buildToBeAlteredRuleConfiguration(new AlterDatabaseDiscoveryTypeStatement(algorithmSegments));
        DatabaseDiscoveryRuleConfiguration currentConfiguration = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        updater.updateCurrentRuleConfiguration(currentConfiguration, databaseDiscoveryRuleConfig);
        assertThat(currentConfiguration.getDiscoveryTypes().size(), is(1));
        assertThat(currentConfiguration.getDiscoveryTypes().get("discovery_type").getType(), is("DISTSQL.FIXTURE"));
        assertThat(currentConfiguration.getDiscoveryTypes().get("discovery_type").getProps().get("key"), is("value_1"));
    }
}