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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junitx.framework.ListAssert;

import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.junit.Test;

import com.mockobjects.dynamic.Mock;

/**
 * Tests the ConfigurationUtils class
 *
 * @version $Id: TestConfigurationUtils.java 1301996 2012-03-17 20:30:39Z sebb $
 */
public class TestConfigurationUtils
{
    protected Configuration config = new BaseConfiguration();

    @Test
    public void testToString()
    {
        String lineSeparator = System.getProperty("line.separator");

        assertEquals("String representation of an empty configuration", "", ConfigurationUtils.toString(config));

        config.setProperty("one", "1");
        assertEquals("String representation of a configuration", "one=1", ConfigurationUtils.toString(config));

        config.setProperty("two", "2");
        assertEquals("String representation of a configuration", "one=1" + lineSeparator + "two=2" , ConfigurationUtils.toString(config));

        config.clearProperty("one");
        assertEquals("String representation of a configuration", "two=2" , ConfigurationUtils.toString(config));

        config.setProperty("one","1");
        assertEquals("String representation of a configuration", "two=2" + lineSeparator + "one=1" , ConfigurationUtils.toString(config));
    }

    @Test
    public void testGetURL() throws Exception
    {
        assertEquals(
            "http://localhost:8080/webapp/config/config.xml",
            ConfigurationUtils
                .getURL(
                    "http://localhost:8080/webapp/config/baseConfig.xml",
                    "config.xml")
                .toString());
        assertEquals(
            "http://localhost:8080/webapp/config/config.xml",
            ConfigurationUtils
                .getURL(
                    "http://localhost:8080/webapp/baseConfig.xml",
                    "config/config.xml")
                .toString());
        URL url = ConfigurationUtils.getURL(null, "config.xml");
        assertEquals("file", url.getProtocol());
        assertEquals("", url.getHost());

        assertEquals(
            "http://localhost:8080/webapp/config/config.xml",
            ConfigurationUtils
                .getURL(
                    "ftp://ftp.server.com/downloads/baseConfig.xml",
                    "http://localhost:8080/webapp/config/config.xml")
                .toString());
        assertEquals(
            "http://localhost:8080/webapp/config/config.xml",
            ConfigurationUtils
                .getURL(null, "http://localhost:8080/webapp/config/config.xml")
                .toString());
        File absFile = new File("config.xml").getAbsoluteFile();
        assertEquals(
            absFile.toURI().toURL(),
            ConfigurationUtils.getURL(
                "http://localhost:8080/webapp/config/baseConfig.xml",
                absFile.getAbsolutePath()));
        assertEquals(
            absFile.toURI().toURL(),
            ConfigurationUtils.getURL(null, absFile.getAbsolutePath()));

        assertEquals(absFile.toURI().toURL(),
        ConfigurationUtils.getURL(absFile.getParent(), "config.xml"));
    }

    @Test
    public void testGetBasePath() throws Exception
    {
        URL url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("base path of " + url, "http://xyz.net/foo/", ConfigurationUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo/");
        assertEquals("base path of " + url, "http://xyz.net/foo/", ConfigurationUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo");
        assertEquals("base path of " + url, "http://xyz.net/", ConfigurationUtils.getBasePath(url));

        url = new URL("http://xyz.net/");
        assertEquals("base path of " + url, "http://xyz.net/", ConfigurationUtils.getBasePath(url));

        url = new URL("http://xyz.net");
        assertEquals("base path of " + url, "http://xyz.net", ConfigurationUtils.getBasePath(url));
    }

    @Test
    public void testGetFileName() throws Exception
    {
        assertEquals("file name for a null URL", null, ConfigurationUtils.getFileName(null));

        URL url = new URL("http://xyz.net/foo/");
        assertEquals("file for a directory URL " + url, null, ConfigurationUtils.getFileName(url));

        url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("file name for a valid URL " + url, "bar.xml", ConfigurationUtils.getFileName(url));
    }

    @Test
    public void testCopy()
    {
        // create the source configuration
        Configuration conf1 = new BaseConfiguration();
        conf1.addProperty("key1", "value1");
        conf1.addProperty("key2", "value2");

        // create the target configuration
        Configuration conf2 = new BaseConfiguration();
        conf2.addProperty("key1", "value3");
        conf2.addProperty("key2", "value4");

        // copy the source configuration into the target configuration
        ConfigurationUtils.copy(conf1, conf2);

        assertEquals("'key1' property", "value1", conf2.getProperty("key1"));
        assertEquals("'key2' property", "value2", conf2.getProperty("key2"));
    }

    @Test
    public void testAppend()
    {
        // create the source configuration
        Configuration conf1 = new BaseConfiguration();
        conf1.addProperty("key1", "value1");
        conf1.addProperty("key2", "value2");

        // create the target configuration
        Configuration conf2 = new BaseConfiguration();
        conf2.addProperty("key1", "value3");
        conf2.addProperty("key2", "value4");

        // append the source configuration to the target configuration
        ConfigurationUtils.append(conf1, conf2);

        List<Object> expected = new ArrayList<Object>();
        expected.add("value3");
        expected.add("value1");
        ListAssert.assertEquals("'key1' property", expected, conf2.getList("key1"));

        expected = new ArrayList<Object>();
        expected.add("value4");
        expected.add("value2");
        ListAssert.assertEquals("'key2' property", expected, conf2.getList("key2"));
    }

    @Test
    public void testGetFile() throws Exception
    {
        File directory = new File("target");
        File reference = new File(directory, "test.txt").getAbsoluteFile();

        assertEquals(reference, ConfigurationUtils.getFile(null, reference.getAbsolutePath()));
        assertEquals(reference, ConfigurationUtils.getFile(directory.getAbsolutePath(), reference.getAbsolutePath()));
        assertEquals(reference, ConfigurationUtils.getFile(directory.getAbsolutePath(), reference.getName()));
        assertEquals(reference, ConfigurationUtils.getFile(directory.toURI().toURL().toString(), reference.getName()));
        assertEquals(reference, ConfigurationUtils.getFile("invalid", reference.toURI().toURL().toString()));
        assertEquals(reference, ConfigurationUtils.getFile(
                "jar:file:/C:/myjar.jar!/my-config.xml/someprops.properties",
                reference.getAbsolutePath()));
    }

    /**
     * Tests whether a "+" character in the file name is handled correctly by
     * fileFromURL(). This test is related to CONFIGURATION-415.
     */
    @Test
    public void testFileFromURLWithPlus() throws MalformedURLException
    {
        File file = new File(new File("target"), "foo+bar.txt")
                .getAbsoluteFile();
        URL fileURL = file.toURI().toURL();
        File file2 = ConfigurationUtils.fileFromURL(fileURL);
        assertEquals("Wrong file", file, file2);
    }

    /**
     * Tests whether fileFromURL() handles null URLs correctly.
     */
    @Test
    public void testFileFromURLNull() throws Exception
    {
        assertNull("Wrong file for null URL", ConfigurationUtils
                .fileFromURL(null));
    }

    @Test
    public void testLocateWithNullTCCL() throws Exception
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(null);
            assertNull(ConfigurationUtils.locate("abase", "aname"));
            // This assert fails when maven 2 is used, so commented out
            //assertNotNull(ConfigurationUtils.locate("test.xml"));
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     * Tests converting a configuration into a hierarchical one.
     */
    @Test
    public void testConvertToHierarchical()
    {
        Configuration conf = new BaseConfiguration();
        for (int i = 0; i < 10; i++)
        {
            conf.addProperty("test" + i, "value" + i);
            conf.addProperty("test.list", "item" + i);
        }

        HierarchicalConfiguration hc = ConfigurationUtils
                .convertToHierarchical(conf);
        for (Iterator<String> it = conf.getKeys(); it.hasNext();)
        {
            String key = it.next();
            assertEquals("Wrong value for key " + key, conf.getProperty(key),
                    hc.getProperty(key));
        }
    }

    /**
     * Tests converting a configuration into a hierarchical one that is already
     * hierarchical.
     */
    @Test
    public void testConvertHierarchicalToHierarchical()
    {
        Configuration conf = new HierarchicalConfiguration();
        conf.addProperty("test", "yes");
        assertSame("Wrong configuration returned", conf, ConfigurationUtils
                .convertToHierarchical(conf));
    }

    /**
     * Tests converting a null configuration to a hierarchical one. The result
     * should be null, too.
     */
    @Test
    public void testConvertNullToHierarchical()
    {
        assertNull("Wrong conversion result for null config",
                ConfigurationUtils.convertToHierarchical(null));
    }

    /**
     * Tests converting a configuration into a hierarchical one if some of its
     * properties contain escaped list delimiter characters.
     */
    @Test
    public void testConvertToHierarchicalDelimiters()
    {
        Configuration conf = new BaseConfiguration();
        conf.addProperty("test.key", "1\\,2\\,3");
        assertEquals("Wrong property value", "1,2,3", conf
                .getString("test.key"));
        HierarchicalConfiguration hc = ConfigurationUtils
                .convertToHierarchical(conf);
        assertEquals("Escaped list delimiters not correctly handled", "1,2,3",
                hc.getString("test.key"));
    }

    /**
     * Tests converting a configuration to a hierarchical one using a specific
     * expression engine.
     */
    @Test
    public void testConvertToHierarchicalEngine()
    {
        Configuration conf = new BaseConfiguration();
        conf.addProperty("test(a)", Boolean.TRUE);
        conf.addProperty("test(b)", Boolean.FALSE);
        DefaultExpressionEngine engine = new DefaultExpressionEngine();
        engine.setIndexStart("[");
        engine.setIndexEnd("]");
        HierarchicalConfiguration hc = ConfigurationUtils
                .convertToHierarchical(conf, engine);
        assertTrue("Wrong value for test(a)", hc.getBoolean("test(a)"));
        assertFalse("Wrong value for test(b)", hc.getBoolean("test(b)"));
    }

    /**
     * Tests converting an already hierarchical configuration using an
     * expression engine. The new engine should be set.
     */
    @Test
    public void testConvertHierarchicalToHierarchicalEngine()
    {
        HierarchicalConfiguration hc = new HierarchicalConfiguration();
        ExpressionEngine engine = new DefaultExpressionEngine();
        assertSame("Created new configuration", hc, ConfigurationUtils
                .convertToHierarchical(hc, engine));
        assertSame("Engine was not set", engine, hc.getExpressionEngine());
    }

    /**
     * Tests converting an already hierarchical configuration using a null
     * expression engine. In this case the expression engine of the
     * configuration should not be touched.
     */
    @Test
    public void testConvertHierarchicalToHierarchicalNullEngine()
    {
        HierarchicalConfiguration hc = new HierarchicalConfiguration();
        ExpressionEngine engine = new DefaultExpressionEngine();
        hc.setExpressionEngine(engine);
        assertSame("Created new configuration", hc, ConfigurationUtils
                .convertToHierarchical(hc, null));
        assertSame("Expression engine was changed", engine, hc
                .getExpressionEngine());
    }

    /**
     * Tests converting a configuration to a hierarchical one that contains a
     * property with multiple values. This test is related to CONFIGURATION-346.
     */
    @Test
    public void testConvertToHierarchicalMultiValues()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("test", "1,2,3");
        HierarchicalConfiguration hc = ConfigurationUtils
                .convertToHierarchical(config);
        assertEquals("Wrong value 1", 1, hc.getInt("test(0)"));
        assertEquals("Wrong value 2", 2, hc.getInt("test(1)"));
        assertEquals("Wrong value 3", 3, hc.getInt("test(2)"));
    }

    /**
     * Tests cloning a configuration that supports this operation.
     */
    @Test
    public void testCloneConfiguration()
    {
        HierarchicalConfiguration conf = new HierarchicalConfiguration();
        conf.addProperty("test", "yes");
        HierarchicalConfiguration copy = (HierarchicalConfiguration) ConfigurationUtils
                .cloneConfiguration(conf);
        assertNotSame("Same object was returned", conf, copy);
        assertEquals("Property was not cloned", "yes", copy.getString("test"));
    }

    /**
     * Tests cloning a configuration that does not support this operation. This
     * should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCloneConfigurationNotSupported()
    {
        Configuration myNonCloneableConfig = new NonCloneableConfiguration();
        ConfigurationUtils.cloneConfiguration(myNonCloneableConfig);
    }

    /**
     * Tests cloning a <b>null</b> configuration.
     */
    @Test
    public void testCloneConfigurationNull()
    {
        assertNull("Wrong return value", ConfigurationUtils
                .cloneConfiguration(null));
    }

    /**
     * Tests whether runtime exceptions can be enabled.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testEnableRuntimeExceptions()
    {
        PropertiesConfiguration config = new PropertiesConfiguration()
        {
            @Override
            protected void addPropertyDirect(String key, Object value)
            {
                // always simulate an exception
                fireError(EVENT_ADD_PROPERTY, key, value, new RuntimeException(
                        "A faked exception!"));
            }
        };
        config.clearErrorListeners();
        ConfigurationUtils.enableRuntimeExceptions(config);
        config.addProperty("test", "testValue");
    }

    /**
     * Tries to enable runtime exceptions for a configuration that does not
     * inherit from EventSource. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEnableRuntimeExceptionsInvalid()
    {
        ConfigurationUtils.enableRuntimeExceptions((Configuration) new Mock(
                Configuration.class).proxy());
    }

    /**
     * Tries to enable runtime exceptions for a null configuration. This should
     * cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEnableRuntimeExceptionsNull()
    {
        ConfigurationUtils.enableRuntimeExceptions(null);
    }
}
