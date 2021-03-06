/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.messaging.tools.jboss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.util.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The convenience object model of a JBoss <mbean> service descriptor configuration element.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 * 
 * $Id$
 */
public class MBeanConfigurationElement
 {
   // Constants ------------------------------------------------------------------------------------
   
   private static final Logger log = Logger.getLogger(MBeanConfigurationElement.class);


   // Static ---------------------------------------------------------------------------------------

   public static Class stringToClass(String type) throws Exception
   {
      // TODO - very quick and very dirty implementation
      if ("java.lang.String".equals(type))
      {
         return String.class;
      }
      else if ("java.lang.Integer".equals(type))
      {
         return Integer.class;
      }
      else if ("int".equals(type))
      {
         return Integer.TYPE;
      }
      else
      {
         throw new Exception("Don't know to convert " + type + " to Class!");
      }
   }

   // Attributes -----------------------------------------------------------------------------------

   protected Element delegate;
   protected ObjectName on;
   protected String className;
   protected String xmbeandd;
   protected Map mbeanConfigAttributes;
   protected Map mbeanOptionalAttributeNames;
   protected List constructors;

   // Constructors ---------------------------------------------------------------------------------

   public MBeanConfigurationElement(Node delegate) throws Exception
   {
      if (!delegate.getNodeName().equals("mbean"))
      {
         throw new IllegalStateException("This is NOT an mbean, it's an " +
                                         delegate.getNodeName() + "!");
      }

      this.delegate = (Element)delegate;

      NamedNodeMap attrs = delegate.getAttributes();

      Node mbeanNameAttr = attrs.getNamedItem("name");
      on = new ObjectName(mbeanNameAttr.getNodeValue());
      
      log.trace("ObjectName is: " + on);

      Node mbeanCodeAttr = attrs.getNamedItem("code");
      className = mbeanCodeAttr.getNodeValue();

      Node mbeanXMBeanDD = attrs.getNamedItem("xmbean-dd");
      if (mbeanXMBeanDD != null)
      {
         xmbeandd = mbeanXMBeanDD.getNodeValue();
      }

      mbeanConfigAttributes = new HashMap();
      mbeanOptionalAttributeNames = new HashMap();
      constructors = new ArrayList();

      if (delegate.hasChildNodes())
      {
         NodeList l = delegate.getChildNodes();
         for(int i = 0; i < l.getLength(); i ++)
         {
            Node mbeanConfigNode = l.item(i);
            String mbeanConfigNodeName = mbeanConfigNode.getNodeName();
            if ("attribute".equals(mbeanConfigNodeName))
            {
               attrs = mbeanConfigNode.getAttributes();
               String configAttribName = attrs.getNamedItem("name").getNodeValue();
               String configAttribValue = null;
               Node n = attrs.getNamedItem("value");
               if (n != null)
               {
                  configAttribValue = n.getNodeValue();
               }
               else
               {
                  configAttribValue = XMLUtil.getTextContent(mbeanConfigNode);
               }
               configAttribValue = XMLUtil.stripCDATA(configAttribValue);

               mbeanConfigAttributes.put(configAttribName, configAttribValue);
            }
            else if ("depends".equals(mbeanConfigNodeName))
            {
               attrs = mbeanConfigNode.getAttributes();
               Node optionalAttributeNode = attrs.getNamedItem("optional-attribute-name");
               if (optionalAttributeNode != null)
               {
                  String optionalAttributeName = optionalAttributeNode.getNodeValue();
                  String optionalAttributeValue = XMLUtil.getTextContent(mbeanConfigNode);
                  mbeanOptionalAttributeNames.put(optionalAttributeName, optionalAttributeValue);
               }
            }
            else if ("constructor".equals(mbeanConfigNodeName))
            {
               ConstructorElement c = new ConstructorElement(mbeanConfigNode);
               constructors.add(c);

               if (mbeanConfigNode.hasChildNodes())
               {
                  NodeList nl = mbeanConfigNode.getChildNodes();
                  for(int j = 0; j < nl.getLength(); j++)
                  {
                     Node n = nl.item(j);
                     String name = n.getNodeName();
                     if ("arg".equals(name))
                     {
                        NamedNodeMap at = n.getAttributes();
                        Node attributeNode = at.getNamedItem("type");
                        Class type = stringToClass(attributeNode.getNodeValue());
                        attributeNode = at.getNamedItem("value");
                        String value = attributeNode.getNodeValue();
                        c.addArgument(type, value, attributeNode);
                     }
                  }
               }
            }
         }
      }
   }

   // Public ---------------------------------------------------------------------------------------

   public Element getDelegate()
   {
      return delegate;
   }

   public ObjectName getObjectName()
   {
      return on;
   }

   /**
    * Returns the fully qualified name of the class that implements the service.
    */
   public String getMBeanClassName()
   {
      return className;
   }

   /**
    * @return Set<String>
    */
   public Set attributeNames()
   {
      return mbeanConfigAttributes.keySet();
   }

   public String getAttributeValue(String name)
   {
      return (String)mbeanConfigAttributes.get(name);
   }

   public void setAttribute(String name, String value)
   {
      mbeanConfigAttributes.put(name, value);
   }

   /**
    * @return Set<String>
    */
   public Set dependencyOptionalAttributeNames()
   {
      return mbeanOptionalAttributeNames.keySet();
   }

   public String getDependencyOptionalAttributeValue(String optionalAttributeName)
   {
      return (String)mbeanOptionalAttributeNames.get(optionalAttributeName);
   }

   public Class getConstructorArgumentType(int constructorIndex, int paramIndex)
   {
      return ((ConstructorElement)constructors.get(constructorIndex)).getArgType(paramIndex);
   }

   public String getConstructorArgumentValue(int constructorIndex, int paramIndex)
   {
      return ((ConstructorElement)constructors.get(constructorIndex)).getArgValue(paramIndex);
   }

   public void setConstructorArgumentValue(int constructorIndex, int paramIndex, String value)
      throws Exception
   {
      ConstructorElement c = ((ConstructorElement)constructors.get(constructorIndex));
      c.setArgValue(paramIndex, value);
   }

   /**
    * Removes all &lt;constructor&gt; elements from the configuration.
    */
   public void removeConstructors()
   {
      if (constructors.isEmpty())
      {
         return;
      }

      for (Iterator iter = constructors.iterator(); iter.hasNext();)
      {
         ConstructorElement element = (ConstructorElement) iter.next();
         element.node.getParentNode().removeChild(element.node);
      }
      
      constructors.clear();
   }

   public String toString()
   {
      return getMBeanClassName() + "[" + getObjectName() + "]";
   }

   // Package protected ----------------------------------------------------------------------------
   
   // Protected ------------------------------------------------------------------------------------
   
   // Private --------------------------------------------------------------------------------------

   // Inner classes --------------------------------------------------------------------------------

   private class ConstructorElement
   {
      // List<Class>
      private List argTypes = new ArrayList();
      // List<String>
      private List argValues = new ArrayList();
      // List<Node>
      private List argNodes = new ArrayList();

      protected Node node;

      public ConstructorElement(Node node)
      {
         this.node = node;
      }

      public void addArgument(Class type, String value, Node node)
      {
         argTypes.add(type);
         argValues.add(value);
         argNodes.add(node);
      }

      public Class getArgType(int idx)
      {
         return (Class)argTypes.get(idx);
      }

      public String getArgValue(int idx)
      {
         return (String)argValues.get(idx);
      }

      public void setArgValue(int idx, String value)
      {
         Node n = (Node)argNodes.get(idx);
         n.setNodeValue(value);
         argValues.set(idx, value);
      }
   }
}
