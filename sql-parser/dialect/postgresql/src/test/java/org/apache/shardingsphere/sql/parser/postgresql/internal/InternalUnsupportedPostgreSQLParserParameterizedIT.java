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

package org.apache.shardingsphere.sql.parser.postgresql.internal;

import org.apache.shardingsphere.test.sql.parser.internal.InternalUnsupportedSQLParserParameterizedIT;
import org.apache.shardingsphere.test.sql.parser.internal.InternalSQLParserTestParameter;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public final class InternalUnsupportedPostgreSQLParserParameterizedIT extends InternalUnsupportedSQLParserParameterizedIT {
    
    public InternalUnsupportedPostgreSQLParserParameterizedIT(final InternalSQLParserTestParameter testParameter) {
        super(testParameter);
    }
    
    @Parameters(name = "{0}")
    public static Collection<InternalSQLParserTestParameter> getTestParameters() {
        return InternalUnsupportedSQLParserParameterizedIT.getTestParameters("PostgreSQL");
    }
}