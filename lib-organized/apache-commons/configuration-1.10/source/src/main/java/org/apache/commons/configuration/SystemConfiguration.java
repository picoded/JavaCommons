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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A configuration based on the system properties.
 *
 * @author Emmanuel Bourg
 * @version $Id: SystemConfiguration.java 1210204 2011-12-04 20:38:02Z oheger $
 * @since 1.1
 */
public class SystemConfiguration extends MapConfiguration
{
    /** The logger. */
    private static Log log = LogFactory.getLog(SystemConfiguration.class);

    /**
     * Create a Configuration based on the system properties.
     *
     * @see System#getProperties
     */
    public SystemConfiguration()
    {
        super(System.getProperties());
    }

    /**
     * The method allows system properties to be set from a property file.
     * @param fileName The name of the property file.
     * @throws Exception if an error occurs.
     * @since 1.6
     */
    public static void setSystemProperties(String fileName) throws Exception
    {
        setSystemProperties(null, fileName);
    }

    /**
     * The method allows system properties to be set from a property file.
     * @param basePath The base path to look for the property file.
     * @param fileName The name of the property file.
     * @throws Exception if an error occurs.
     * @since 1.6
     */
    public static void setSystemProperties(String basePath, String fileName) throws Exception
    {
        PropertiesConfiguration config = fileName.endsWith(".xml")
            ? new XMLPropertiesConfiguration() : new PropertiesConfiguration();
        if (basePath != null)
        {
            config.setBasePath(basePath);
        }
        config.setFileName(fileName);
        config.load();
        setSystemProperties(config);
    }

    /**
     * Set System properties from a configuration file.
     * @param systemConfig The configuration containing the properties to be set.
     * @since 1.6
     */
    public static void setSystemProperties(PropertiesConfiguration systemConfig)
    {
        Iterator<String> iter = systemConfig.getKeys();
        while (iter.hasNext())
        {
            String key = iter.next();
            String value = (String) systemConfig.getProperty(key);
            if (log.isDebugEnabled())
            {
                log.debug("Setting system property " + key + " to " + value);
            }
            System.setProperty(key, value);
        }
    }
}
