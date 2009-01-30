/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.messaging.ra;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.ConnectionMetaData;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.jms.client.JBossConnectionFactory;

/**
 * JBM ManagedConectionFactory
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>.
 * @version $Revision: $
 */
public class JBMManagedConnectionFactory implements ManagedConnectionFactory
{
   /** The logger */
   private static final Logger log = Logger.getLogger(JBMManagedConnectionFactory.class);
   
   /** Trace enabled */
   private static boolean trace = log.isTraceEnabled();
   
   /** Connection manager */
   private ConnectionManager cm;

   /** The managed connection factory properties */
   private JBMMCFProperties mcfProperties;

   /** The JBoss connection factory */
   private JBossConnectionFactory factory;
   
   /** Have the factory been configured */
   private AtomicBoolean configured;

   /**
    * Constructor
    */
   public JBMManagedConnectionFactory()
   {
      if (trace)
         log.trace("constructor()");
      
      mcfProperties = new JBMMCFProperties();
      configured = new AtomicBoolean(false);
   }
  
   /**
    * Creates a Connection Factory instance
    * @return javax.resource.cci.ConnectionFactory instance
    * @exception ResourceException Thrown if a connection factory cant be created
    */
   public Object createConnectionFactory() throws ResourceException
   {
      if (trace)
         log.debug("createConnectionFactory()");
      
      return createConnectionFactory(new JBMConnectionManager());
   }

   /**
    * Creates a Connection Factory instance
    * @param cxManager The connection manager
    * @return javax.resource.cci.ConnectionFactory instance
    * @exception ResourceException Thrown if a connection factory cant be created
    */
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      if (trace)
         log.trace("createConnectionFactory(" + cxManager + ")");

      cm = cxManager;

      JBMConnectionFactory cf = new JBMConnectionFactoryImpl(this, cm);

      if (trace)
         log.trace("Created connection factory: " + cf + ", using connection manager: " + cm);
      
      return cf;
   }
   
   /**
    * Creates a new physical connection to the underlying EIS resource manager.
    * @param subject Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @return The managed connection
    * @exception ResourceException Thrown if a managed connection cant be created
    */
   public ManagedConnection createManagedConnection(Subject subject,
                                                    ConnectionRequestInfo cxRequestInfo)
      throws ResourceException 
   {
      if (trace)
         log.trace("createManagedConnection(" + subject + ", " + cxRequestInfo + ")");

      JBMConnectionRequestInfo cri = getCRI((JBMConnectionRequestInfo)cxRequestInfo);

      JBMCredential credential = JBMCredential.getCredential(this, subject, cri);

      if (trace)
         log.trace("jms credential: " + credential);

      JBMManagedConnection mc = new JBMManagedConnection(this, cri, credential.getUserName(), credential.getPassword());
    
      if (trace)
         log.trace("created new managed connection: " + mc);
      
      return mc;
   }

   /**
    * Returns a matched connection from the candidate set of connections.
    * @param connectionSet The candidate connection set
    * @param subject Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @return The managed connection
    * @exception ResourceException Thrown if no managed connection cant be found
    */
   public ManagedConnection matchManagedConnections(Set connectionSet,
                                                    Subject subject,
                                                    ConnectionRequestInfo cxRequestInfo)
      throws ResourceException
   {
      if (trace)
         log.trace("matchManagedConnections(" + connectionSet + ", " + subject + ", " + cxRequestInfo + ")");

      JBMConnectionRequestInfo cri = getCRI((JBMConnectionRequestInfo)cxRequestInfo);
      JBMCredential credential = JBMCredential.getCredential(this, subject, cri);
      
      if (trace)
         log.trace("Looking for connection matching credentials: " + credential);
      
      Iterator connections = connectionSet.iterator();
      
      while (connections.hasNext())
      {
         Object obj = connections.next();
      
         if (obj instanceof JBMManagedConnection)
         {
            JBMManagedConnection mc = (JBMManagedConnection) obj;
            ManagedConnectionFactory mcf = mc.getManagedConnectionFactory();
        
            if ((mc.getUserName() == null || (mc.getUserName() != null && mc.getUserName().equals(credential.getUserName()))) && mcf.equals(this))
            {
               if (cri.equals(mc.getCRI()))
               {
                  if (trace)
                     log.trace("Found matching connection: " + mc);
                  
                  return mc;
               }
            }
         }
      }
      
      if (trace)
         log.trace("No matching connection was found");

      return null;
   }

   /**
    * Set the log writer -- NOT SUPPORTED
    * @param out The writer
    * @exception ResourceException Thrown if the writer cant be set
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      if (trace)
         log.trace("setLogWriter(" + out + ")");
   }

   /**
    * Get the log writer -- NOT SUPPORTED
    * @return The writer
    * @exception ResourceException Thrown if the writer cant be retrieved
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      if (trace)
         log.trace("getLogWriter()");

      return null;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    * @param obj Object with which to compare
    * @return True if this object is the same as the obj argument; false otherwise.
    */
   public boolean equals(Object obj)
   {
      if (trace)
         log.trace("equals(" + obj + ")");

      if (obj == null)
         return false;

      if (obj instanceof JBMManagedConnectionFactory)
      {
         return mcfProperties.equals(((JBMManagedConnectionFactory)obj).getProperties());
      }
      else
      {
         return false;
      }
   }

   /**
    * Return the hash code for the object
    * @return The hash code
    */
   public int hashCode()
   {
      if (trace)
         log.trace("hashCode()");

      return mcfProperties.hashCode();
   }

   /**
    * Get the discovery group name
    * @return The value
    */
   public String getDiscoveryGroupName()
   {
      if (trace)
         log.trace("getDiscoveryGroupName()");

      return mcfProperties.getDiscoveryGroupName();
   }

   /**
    * Set the discovery group name
    * @param dgn The value
    */
   public void setDiscoveryGroupName(String dgn)
   {
      if (trace)
         log.trace("setDiscoveryGroupName(" + dgn + ")");

      mcfProperties.setDiscoveryGroupName(dgn);
   }

   /**
    * Get the discovery group port
    * @return The value
    */
   public Integer getDiscoveryGroupPort()
   {
      if (trace)
         log.trace("getDiscoveryGroupPort()");

      return mcfProperties.getDiscoveryGroupPort();
   }

   /**
    * Set the discovery group port
    * @param dgp The value
    */
   public void setDiscoveryGroupPort(Integer dgp)
   {
      if (trace)
         log.trace("setDiscoveryGroupPort(" + dgp + ")");

      mcfProperties.setDiscoveryGroupPort(dgp);
   }

   /**
    * Get the user name
    * @return The value
    */
   public String getUserName()
   {
      if (trace)
         log.trace("getUserName()");

      return mcfProperties.getUserName();
   }

   /**
    * Set the user name
    * @param userName The value
    */
   public void setUserName(String userName)
   {
      if (trace)
         log.trace("setUserName(" + userName + ")");

      mcfProperties.setUserName(userName);
   }

   /**
    * Get the password
    * @return The value
    */
   public String getPassword()
   {
      if (trace)
         log.trace("getPassword()");

      return mcfProperties.getPassword();
   }

   /**
    * Set the password
    * @param password The value
    */
   public void setPassword(String password)
   {
      if (trace)
         log.trace("setPassword(****)");

      mcfProperties.setPassword(password);
   }
   
   /**
    * Get the client ID
    * @return The value
    */
   public String getClientID()
   {
      if (trace)
         log.trace("getClientID()");

      return mcfProperties.getClientID();
   }

   /**
    * Set the client ID
    * @param clientId The client id
    */
   public void setClientID(String clientID)
   {
      if (trace)
         log.trace("setClientID(" + clientID + ")");

      mcfProperties.setClientID(clientID);
   }
   
   /**
    * Get the use XA flag
    * @return The value
    */
   public Boolean getUseXA()
   {
      if (trace)
         log.trace("getUseXA()");

      return mcfProperties.getUseXA();
   }

   /**
    * Set the use XA flag
    * @param xa The value
    */
   public void setUseXA(Boolean xa)
   {
      if (trace)
         log.trace("setUseXA(" + xa + ")");

      mcfProperties.setUseXA(xa);
   }

   /**
    * Get the default session type
    * @return The value
    */
   public String getSessionDefaultType()
   {
      if (trace)
         log.trace("getSessionDefaultType()");

      return mcfProperties.getSessionDefaultType();
   }

   /**
    * Set the default session type
    * @param type either javax.jms.Topic or javax.jms.Queue
    */
   public void setSessionDefaultType(String type)
   {
      if (trace)
         log.trace("setSessionDefaultType(" + type + ")");

      mcfProperties.setSessionDefaultType(type);
   }

   /**
    * Get the useTryLock.
    * @return the useTryLock.
    */
   public Integer getUseTryLock()
   {
      if (trace)
         log.trace("getUseTryLock()");

      return mcfProperties.getUseTryLock();
   }
   
   /**
    * Set the useTryLock.
    * @param useTryLock the useTryLock.
    */
   public void setUseTryLock(Integer useTryLock)
   {
      if (trace)
         log.trace("setUseTryLock(" + useTryLock + ")");

      mcfProperties.setUseTryLock(useTryLock);
   }
   
   /**
    * Get a connection request info instance
    * @param info The instance that should be updated; may be <code>null</code>
    * @return The instance
    */
   private JBMConnectionRequestInfo getCRI(JBMConnectionRequestInfo info)
   {
      if (trace)
         log.trace("getCRI(" + info + ")");

      if (info == null)
      {
         // Create a default one
         return new JBMConnectionRequestInfo(mcfProperties);
      }
      else
      {
         // Fill the one with any defaults
         info.setDefaults(mcfProperties);
         return info;
      }
   }
   
   /**
    * Get the connection metadata
    * @return The metadata
    */
   public ConnectionMetaData getMetaData()
   {
      if (trace)
         log.trace("getMetadata()");

      return new JBMConnectionMetaData();
   }

   /**
    * Get the managed connection factory properties
    * @return The properties
    */
   public JBMMCFProperties getProperties()
   {
      if (trace)
         log.trace("getProperties()");

      return mcfProperties;
   }

   /**
    * Get the JBoss connection factory
    * @return The factory
    */
   protected JBossConnectionFactory getJBossConnectionFactory()
   {
      if (!configured.get()) {
         setup();
      }

      return factory;
   }

   /**
    * Setup the factory
    */
   protected void setup()
   {
      if (getDiscoveryGroupName() != null &&
          !getDiscoveryGroupName().trim().equals("") &&
          getDiscoveryGroupPort() != null)
      {
         factory = new JBossConnectionFactory(getDiscoveryGroupName(), getDiscoveryGroupPort().intValue());

         configured.set(true);
      }
   }
}