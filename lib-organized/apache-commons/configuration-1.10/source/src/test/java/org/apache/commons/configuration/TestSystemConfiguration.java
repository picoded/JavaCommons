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

package org.apache.commons.configuration;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

/**
 * Tests for {@code SystemConfiguration}.
 *
 * @author Emmanuel Bourg
 * @version $Id: TestSystemConfiguration.java 1225020 2011-12-27 21:20:45Z oheger $
 */
public class TestSystemConfiguration
{
    @Test
    public void testSystemConfiguration()
    {
        Properties props = System.getProperties();
        props.put("test.number", "123");

        Configuration conf = new SystemConfiguration();
        assertEquals("number", 123, conf.getInt("test.number"));
    }

    @Test
    public void testSetSystemProperties()
    {
        PropertiesConfiguration props = new PropertiesConfiguration();
        props.addProperty("test.name", "Apache");
        SystemConfiguration.setSystemProperties(props);
        assertEquals("System Properties", "Apache", System.getProperty("test.name"));
    }
}
