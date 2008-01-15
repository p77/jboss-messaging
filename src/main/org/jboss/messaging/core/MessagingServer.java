/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.messaging.core;

import java.util.Collection;
import java.util.List;

import org.jboss.jms.server.ConnectionFactoryManager;
import org.jboss.jms.server.ConnectionManager;
import org.jboss.jms.server.DestinationManager;
import org.jboss.jms.server.SecurityStore;
import org.jboss.jms.server.TransactionRepository;
import org.jboss.jms.server.endpoint.ServerSessionEndpoint;
import org.jboss.jms.server.plugin.contract.JMSUserManager;
import org.jboss.messaging.core.impl.ConditionImpl;
import org.jboss.messaging.core.remoting.impl.mina.MinaService;
import org.jboss.messaging.util.Version;

/**
 * This interface defines the internal interface of the Messaging Server exposed
 * to other components of the server.
 * 
 * The external management interface of the Messaging Server is defined by the
 * MessagingServerManagement interface
 * 
 * This interface is never exposed outside the messaging server, e.g. by JMX or other means
 * 
 * @author <a href="tim.fox@jboss.com">Tim Fox</a>
 */
public interface MessagingServer extends MessagingComponent
{  
   /**
    * @return The configuration for this server
    */
   Configuration getConfiguration(); 
   
   /**
    * 
    * @return The server version
    */
   Version getVersion();
   
   boolean isStarted();
   
   void setConfiguration(Configuration configuration);
   
   void setMinaService(MinaService minaService);
   
   MinaService getMinaService();

   ServerSessionEndpoint getSession(String sessionID);

   Collection getSessions();

   void addSession(String id, ServerSessionEndpoint session);

   void removeSession(String id);

   Queue getDefaultDLQInstance() throws Exception;
   
   Queue getDefaultExpiryQueueInstance() throws Exception;

   SecurityStore getSecurityManager();

   DestinationManager getDestinationManager();

   ConnectionFactoryManager getConnectionFactoryManager();

   ConnectionManager getConnectionManager();

   MemoryManager getMemoryManager();

   TransactionRepository getTransactionRepository();
   
   PersistenceManager getPersistenceManager();

   void setPersistenceManager(PersistenceManager persistenceManager);

   JMSUserManager getJmsUserManagerInstance();

   void setJmsUserManager(JMSUserManager jmsUserManager);

   PostOffice getPostOffice();

   void setPostOffice(PostOffice postOffice);
   
   void createQueue(String name, String jndiName) throws Exception;
   
   void createTopic(String name, String jndiName) throws Exception;
   
   void destroyQueue(String name, String jndiName) throws Exception;
   
   void destroyTopic(String name, String jndiName) throws Exception;
   
   void enableMessageCounters();

   void disableMessageCounters();
   
   void resetAllMessageCounters();

   void resetAllMessageCounterHistories();

   void removeAllMessagesForQueue(String queueName) throws Exception;

   void removeAllMessagesForTopic(String queueName) throws Exception;


}
