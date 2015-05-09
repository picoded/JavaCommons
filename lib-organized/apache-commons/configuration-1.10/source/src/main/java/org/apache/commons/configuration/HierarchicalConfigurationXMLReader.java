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

import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>A specialized SAX2 XML parser that "parses" hierarchical
 * configuration objects.</p>
 * <p>This class mimics to be a SAX conform XML parser. Instead of parsing
 * XML documents it processes a {@code Configuration} object and
 * generates SAX events for the single properties defined there. This enables
 * the whole world of XML processing for configuration objects.</p>
 * <p>The {@code HierarchicalConfiguration} object to be parsed can be
 * specified using a constructor or the {@code setConfiguration()} method.
 * This object will be processed by the {@code parse()} methods. Note
 * that these methods ignore their argument.</p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id: HierarchicalConfigurationXMLReader.java 1209998 2011-12-03 20:31:16Z oheger $
 */
public class HierarchicalConfigurationXMLReader extends ConfigurationXMLReader
{
    /** Stores the configuration object to be parsed.*/
    private HierarchicalConfiguration configuration;

    /**
     * Creates a new instance of {@code HierarchicalConfigurationXMLReader}.
     */
    public HierarchicalConfigurationXMLReader()
    {
        super();
    }

    /**
     * Creates a new instance of {@code HierarchicalConfigurationXMLReader} and
     * sets the configuration to be parsed.
     *
     * @param config the configuration object
     */
    public HierarchicalConfigurationXMLReader(HierarchicalConfiguration config)
    {
        this();
        setConfiguration(config);
    }

    /**
     * Returns the configuration object to be parsed.
     *
     * @return the configuration object to be parsed
     */
    public HierarchicalConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Sets the configuration object to be parsed.
     *
     * @param config the configuration object to be parsed
     */
    public void setConfiguration(HierarchicalConfiguration config)
    {
        configuration = config;
    }

    /**
     * Returns the configuration object to be processed.
     *
     * @return the actual configuration object
     */
    @Override
    public Configuration getParsedConfiguration()
    {
        return getConfiguration();
    }

    /**
     * Processes the actual configuration object to generate SAX parsing events.
     */
    @Override
    protected void processKeys()
    {
        getConfiguration().getRoot().visit(new SAXVisitor(), null);
    }

    /**
     * A specialized visitor class for generating SAX events for a
     * hierarchical node structure.
     *
     */
    class SAXVisitor extends HierarchicalConfiguration.NodeVisitor
    {
        /** Constant for the attribute type.*/
        private static final String ATTR_TYPE = "CDATA";

        /**
         * Visits the specified node after its children have been processed.
         *
         * @param node the actual node
         * @param key the key of this node
         */
        @Override
        public void visitAfterChildren(Node node, ConfigurationKey key)
        {
            if (!isAttributeNode(node))
            {
                fireElementEnd(nodeName(node));
            }
        }

        /**
         * Visits the specified node.
         *
         * @param node the actual node
         * @param key the key of this node
         */
        @Override
        public void visitBeforeChildren(Node node, ConfigurationKey key)
        {
            if (!isAttributeNode(node))
            {
                fireElementStart(nodeName(node), fetchAttributes(node));

                if (node.getValue() != null)
                {
                    fireCharacters(node.getValue().toString());
                }
            }
        }

        /**
         * Checks if iteration should be terminated. This implementation stops
         * iteration after an exception has occurred.
         *
         * @return a flag if iteration should be stopped
         */
        @Override
        public boolean terminate()
        {
            return getException() != null;
        }

        /**
         * Returns an object with all attributes for the specified node.
         *
         * @param node the actual node
         * @return an object with all attributes of this node
         */
        protected Attributes fetchAttributes(Node node)
        {
            AttributesImpl attrs = new AttributesImpl();

            for (ConfigurationNode child : node.getAttributes())
            {
                if (child.getValue() != null)
                {
                    String attr = child.getName();
                    attrs.addAttribute(NS_URI, attr, attr, ATTR_TYPE, child.getValue().toString());
                }
            }

            return attrs;
        }

        /**
         * Helper method for determining the name of a node. If a node has no
         * name (which is true for the root node), the specified default name
         * will be used.
         *
         * @param node the node to be checked
         * @return the name for this node
         */
        private String nodeName(Node node)
        {
            return (node.getName() == null) ? getRootName() : node.getName();
        }

        /**
         * Checks if the specified node is an attribute node. In the node
         * hierarchy attributes are stored as normal child nodes, but with
         * special names.
         *
         * @param node the node to be checked
         * @return a flag if this is an attribute node
         */
        private boolean isAttributeNode(Node node)
        {
            return node.isAttribute();
        }
    }
}
