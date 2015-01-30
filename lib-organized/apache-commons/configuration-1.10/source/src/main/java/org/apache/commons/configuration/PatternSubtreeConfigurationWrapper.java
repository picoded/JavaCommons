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

import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ExpressionEngine;

/**
 * Wraps a HierarchicalConfiguration and allows subtrees to be access via a configured path with
 * replaceable tokens derived from the ConfigurationInterpolator. When used with injection frameworks
 * such as Spring it allows components to be injected with subtrees of the configuration.
 * @since 1.6
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id: PatternSubtreeConfigurationWrapper.java 1534064 2013-10-21 08:44:33Z henning $
 */
public class PatternSubtreeConfigurationWrapper extends AbstractHierarchicalFileConfiguration
{
    /**
     * Prevent recursion while resolving unprefixed properties.
     */
    private static ThreadLocal<Boolean> recursive = new ThreadLocal<Boolean>()
    {
        @Override
        protected synchronized Boolean initialValue()
        {
            return Boolean.FALSE;
        }
    };

    /** The wrapped configuration */
    private final AbstractHierarchicalFileConfiguration config;

    /** The path to the subtree */
    private final String path;

    /** True if the path ends with '/', false otherwise */
    private final boolean trailing;

    /** True if the constructor has finished */
    private boolean init;

    /**
     * Constructor
     * @param config The Configuration to be wrapped.
     * @param path The base path pattern.
     */
    public PatternSubtreeConfigurationWrapper(AbstractHierarchicalFileConfiguration config, String path)
    {
        this.config = config;
        this.path = path;
        this.trailing = path.endsWith("/");
        this.init = true;
    }

    @Override
    public Object getReloadLock()
    {
        return config.getReloadLock();
    }

    @Override
    public void addProperty(String key, Object value)
    {
        config.addProperty(makePath(key), value);
    }

    @Override
    public void clear()
    {
        getConfig().clear();
    }

    @Override
    public void clearProperty(String key)
    {
        config.clearProperty(makePath(key));
    }

    @Override
    public boolean containsKey(String key)
    {
        return config.containsKey(makePath(key));
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        return config.getBigDecimal(makePath(key), defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key)
    {
        return config.getBigDecimal(makePath(key));
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        return config.getBigInteger(makePath(key), defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key)
    {
        return config.getBigInteger(makePath(key));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return config.getBoolean(makePath(key), defaultValue);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        return config.getBoolean(makePath(key), defaultValue);
    }

    @Override
    public boolean getBoolean(String key)
    {
        return config.getBoolean(makePath(key));
    }

    @Override
    public byte getByte(String key, byte defaultValue)
    {
        return config.getByte(makePath(key), defaultValue);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue)
    {
        return config.getByte(makePath(key), defaultValue);
    }

    @Override
    public byte getByte(String key)
    {
        return config.getByte(makePath(key));
    }

    @Override
    public double getDouble(String key, double defaultValue)
    {
        return config.getDouble(makePath(key), defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue)
    {
        return config.getDouble(makePath(key), defaultValue);
    }

    @Override
    public double getDouble(String key)
    {
        return config.getDouble(makePath(key));
    }

    @Override
    public float getFloat(String key, float defaultValue)
    {
        return config.getFloat(makePath(key), defaultValue);
    }

    @Override
    public Float getFloat(String key, Float defaultValue)
    {
        return config.getFloat(makePath(key), defaultValue);
    }

    @Override
    public float getFloat(String key)
    {
        return config.getFloat(makePath(key));
    }

    @Override
    public int getInt(String key, int defaultValue)
    {
        return config.getInt(makePath(key), defaultValue);
    }

    @Override
    public int getInt(String key)
    {
        return config.getInt(makePath(key));
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue)
    {
        return config.getInteger(makePath(key), defaultValue);
    }

    @Override
    public Iterator<String> getKeys()
    {
        return config.getKeys(makePath());
    }

    @Override
    public Iterator<String> getKeys(String prefix)
    {
        return config.getKeys(makePath(prefix));
    }

    @Override
    public List<Object> getList(String key, List<?> defaultValue)
    {
        return config.getList(makePath(key), defaultValue);
    }

    @Override
    public List<Object> getList(String key)
    {
        return config.getList(makePath(key));
    }

    @Override
    public long getLong(String key, long defaultValue)
    {
        return config.getLong(makePath(key), defaultValue);
    }

    @Override
    public Long getLong(String key, Long defaultValue)
    {
        return config.getLong(makePath(key), defaultValue);
    }

    @Override
    public long getLong(String key)
    {
        return config.getLong(makePath(key));
    }

    @Override
    public Properties getProperties(String key)
    {
        return config.getProperties(makePath(key));
    }

    @Override
    public Object getProperty(String key)
    {
        return config.getProperty(makePath(key));
    }

    @Override
    public short getShort(String key, short defaultValue)
    {
        return config.getShort(makePath(key), defaultValue);
    }

    @Override
    public Short getShort(String key, Short defaultValue)
    {
        return config.getShort(makePath(key), defaultValue);
    }

    @Override
    public short getShort(String key)
    {
        return config.getShort(makePath(key));
    }

    @Override
    public String getString(String key, String defaultValue)
    {
        return config.getString(makePath(key), defaultValue);
    }

    @Override
    public String getString(String key)
    {
        return config.getString(makePath(key));
    }

    @Override
    public String[] getStringArray(String key)
    {
        return config.getStringArray(makePath(key));
    }

    @Override
    public boolean isEmpty()
    {
        return getConfig().isEmpty();
    }

    @Override
    public void setProperty(String key, Object value)
    {
        getConfig().setProperty(key, value);
    }

    @Override
    public Configuration subset(String prefix)
    {
        return getConfig().subset(prefix);
    }

    @Override
    public Node getRoot()
    {
        return getConfig().getRoot();
    }

    @Override
    public void setRoot(Node node)
    {
        if (init)
        {
            getConfig().setRoot(node);
        }
        else
        {
            super.setRoot(node);
        }
    }

    @Override
    public ConfigurationNode getRootNode()
    {
        return getConfig().getRootNode();
    }

    @Override
    public void setRootNode(ConfigurationNode rootNode)
    {
        if (init)
        {
            getConfig().setRootNode(rootNode);
        }
        else
        {
            super.setRootNode(rootNode);
        }
    }

    @Override
    public ExpressionEngine getExpressionEngine()
    {
        return config.getExpressionEngine();
    }

    @Override
    public void setExpressionEngine(ExpressionEngine expressionEngine)
    {
        if (init)
        {
            config.setExpressionEngine(expressionEngine);
        }
        else
        {
            super.setExpressionEngine(expressionEngine);
        }
    }

    @Override
    public void addNodes(String key, Collection<? extends ConfigurationNode> nodes)
    {
        getConfig().addNodes(key, nodes);
    }

    @Override
    public SubnodeConfiguration configurationAt(String key, boolean supportUpdates)
    {
        return config.configurationAt(makePath(key), supportUpdates);
    }

    @Override
    public SubnodeConfiguration configurationAt(String key)
    {
        return config.configurationAt(makePath(key));
    }

    @Override
    public List<HierarchicalConfiguration> configurationsAt(String key)
    {
        return config.configurationsAt(makePath(key));
    }

    @Override
    public void clearTree(String key)
    {
        config.clearTree(makePath(key));
    }

    @Override
    public int getMaxIndex(String key)
    {
        return config.getMaxIndex(makePath(key));
    }

    @Override
    public Configuration interpolatedConfiguration()
    {
        return getConfig().interpolatedConfiguration();
    }

    @Override
    public void addConfigurationListener(ConfigurationListener l)
    {
        getConfig().addConfigurationListener(l);
    }

    @Override
    public boolean removeConfigurationListener(ConfigurationListener l)
    {
        return getConfig().removeConfigurationListener(l);
    }

    @Override
    public Collection<ConfigurationListener> getConfigurationListeners()
    {
        return getConfig().getConfigurationListeners();
    }

    @Override
    public void clearConfigurationListeners()
    {
        getConfig().clearConfigurationListeners();
    }

    @Override
    public void addErrorListener(ConfigurationErrorListener l)
    {
        getConfig().addErrorListener(l);
    }

    @Override
    public boolean removeErrorListener(ConfigurationErrorListener l)
    {
        return getConfig().removeErrorListener(l);
    }

    @Override
    public void clearErrorListeners()
    {
        getConfig().clearErrorListeners();
    }

    public void save(Writer writer) throws ConfigurationException
    {
        config.save(writer);
    }

    public void load(Reader reader) throws ConfigurationException
    {
        config.load(reader);
    }

    @Override
    public Collection<ConfigurationErrorListener> getErrorListeners()
    {
        return getConfig().getErrorListeners();
    }

    @Override
    protected Object resolveContainerStore(String key)
    {
        if (recursive.get().booleanValue())
        {
            return null;
        }
        recursive.set(Boolean.TRUE);
        try
        {
            return super.resolveContainerStore(key);
        }
        finally
        {
            recursive.set(Boolean.FALSE);
        }
    }

    private HierarchicalConfiguration getConfig()
    {
        return config.configurationAt(makePath());
    }

    private String makePath()
    {
        String pathPattern = trailing ? path.substring(0, path.length() - 1) : path;
        return getSubstitutor().replace(pathPattern);
    }

    /*
     * Resolve the root expression and then add the item being retrieved. Insert a
     * separator character as required.
     */
    private String makePath(String item)
    {
        String pathPattern;
        if ((item.length() == 0 || item.startsWith("/")) && trailing)
        {
            pathPattern = path.substring(0, path.length() - 1);
        }
        else  if (!item.startsWith("/") || !trailing)
        {
            pathPattern = path + "/";
        }
        else
        {
            pathPattern = path;
        }
        return getSubstitutor().replace(pathPattern) + item;
    }
}
