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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.junit.Test;

/**
 * Tests for MapConfiguration.
 *
 * @author Emmanuel Bourg
 * @version $Id: TestMapConfiguration.java 1222465 2011-12-22 21:32:56Z oheger $
 */
public class TestMapConfiguration extends TestAbstractConfiguration
{
    /** Constant for a test key.*/
    private static final String KEY = "key1";

    /** Constant for a test property value with whitespace.*/
    private static final String SPACE_VALUE = "   Value with whitespace  ";

    /** The trimmed test value.*/
    private static final String TRIM_VALUE = SPACE_VALUE.trim();

    @Override
    protected AbstractConfiguration getConfiguration()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(KEY, "value1");
        map.put("key2", "value2");
        map.put("list", "value1, value2");
        map.put("listesc", "value1\\,value2");

        return new MapConfiguration(map);
    }

    @Override
    protected AbstractConfiguration getEmptyConfiguration()
    {
        return new MapConfiguration(new HashMap<String, Object>());
    }

    @Test
    public void testGetMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();

        MapConfiguration conf = new MapConfiguration(map);
        assertEquals(map, conf.getMap());
    }

    @Test
    public void testClone()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        MapConfiguration copy = (MapConfiguration) config.clone();
        StrictConfigurationComparator comp = new StrictConfigurationComparator();
        assertTrue("Configurations are not equal", comp.compare(config, copy));
    }

    /**
     * Tests if the cloned configuration is decoupled from the original.
     */
    @Test
    public void testCloneModify()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.addConfigurationListener(new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
            {
                // Just a dummy
            }
        });
        MapConfiguration copy = (MapConfiguration) config.clone();
        assertTrue("Event listeners were copied", copy
                .getConfigurationListeners().isEmpty());

        config.addProperty("cloneTest", Boolean.TRUE);
        assertFalse("Map not decoupled", copy.containsKey("cloneTest"));
        copy.clearProperty("key1");
        assertEquals("Map not decoupled (2)", "value1", config
                .getString("key1"));
    }

    /**
     * Tests adding another value to an existing property.
     */
    @Test
    public void testAddProperty()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.addProperty(KEY, TRIM_VALUE);
        config.addProperty(KEY, "anotherValue");
        List<Object> values = config.getList(KEY);
        assertEquals("Wrong number of values", 3, values.size());
    }

    /**
     * Tests querying a property when trimming is active.
     */
    @Test
    public void testGetPropertyTrim()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.getMap().put(KEY, SPACE_VALUE);
        assertEquals("Wrong trimmed value", TRIM_VALUE, config.getProperty(KEY));
    }

    /**
     * Tests querying a property when trimming is disabled.
     */
    @Test
    public void testGetPropertyTrimDisabled()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.getMap().put(KEY, SPACE_VALUE);
        config.setTrimmingDisabled(true);
        assertEquals("Wrong trimmed value", SPACE_VALUE, config.getProperty(KEY));
    }

    /**
     * Tests querying a property when trimming is enabled, but list splitting is
     * disabled. In this case no trimming is performed (trimming only works if
     * list splitting is enabled).
     */
    @Test
    public void testGetPropertyTrimNoSplit()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.getMap().put(KEY, SPACE_VALUE);
        config.setDelimiterParsingDisabled(true);
        assertEquals("Wrong trimmed value", SPACE_VALUE, config.getProperty(KEY));
    }
}
