/*
 * JBoss, Home of Professional Open Source Copyright 2005-2008, Red Hat
 * Middleware LLC, and individual contributors by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.jboss.messaging.tests.integration.cluster.failover;

import static org.jboss.messaging.core.config.impl.ConfigurationImpl.DEFAULT_MANAGEMENT_ADDRESS;

import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;

import junit.framework.TestCase;

import org.jboss.messaging.core.client.ClientConsumer;
import org.jboss.messaging.core.client.ClientMessage;
import org.jboss.messaging.core.client.ClientProducer;
import org.jboss.messaging.core.client.ClientSession;
import org.jboss.messaging.core.client.impl.ClientSessionFactoryImpl;
import org.jboss.messaging.core.client.impl.ClientSessionFactoryInternal;
import org.jboss.messaging.core.client.impl.ClientSessionImpl;
import org.jboss.messaging.core.client.management.impl.ManagementHelper;
import org.jboss.messaging.core.config.Configuration;
import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.config.impl.ConfigurationImpl;
import org.jboss.messaging.core.exception.MessagingException;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.management.impl.ManagementServiceImpl;
import org.jboss.messaging.core.remoting.RemotingConnection;
import org.jboss.messaging.core.remoting.impl.invm.InVMRegistry;
import org.jboss.messaging.core.remoting.impl.invm.TransportConstants;
import org.jboss.messaging.core.server.MessagingService;
import org.jboss.messaging.core.server.impl.MessagingServiceImpl;
import org.jboss.messaging.util.SimpleString;

/**
 * 
 * A FailoverManagementTest
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * Created 5 Nov 2008 15:05:14
 *
 *
 */
public class FailoverManagementTest extends TestCase
{
   private static final Logger log = Logger.getLogger(FailoverManagementTest.class);

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private static final SimpleString ADDRESS = new SimpleString("FailoverTestAddress");

   private MessagingService liveService;

   private MessagingService backupService;

   private final Map<String, Object> backupParams = new HashMap<String, Object>();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testManagementMessages() throws Exception
   {            
      ClientSessionFactoryInternal sf1 = new ClientSessionFactoryImpl(new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory"),
                                                                      new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                                                 backupParams));
      
      sf1.setSendWindowSize(32 * 1024);
  
      ClientSession session1 = sf1.createSession(false, true, true);

      session1.createQueue(ADDRESS, ADDRESS, null, false, false, true);
      
      SimpleString replyTo = new SimpleString("replyto");
      
      session1.createQueue(replyTo, new SimpleString("replyto"), null, false, false, true);
      
      ClientProducer producer = session1.createProducer(ADDRESS);
      
      final int numMessages = 10;
      
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage msg  = session1.createClientMessage(false);
         
         msg.getBody().flip();
         
         producer.send(msg);
      }
      
      for (int i = 0; i < numMessages / 2; i++)
      {
         ClientMessage managementMessage  = session1.createClientMessage(false);
         
         ManagementHelper.putAttributes(managementMessage,
                                        replyTo,
                                        ManagementServiceImpl.getQueueObjectName(ADDRESS, ADDRESS),
                                        "MessageCount");
         
         managementMessage.getBody().flip();
         
         producer.send(DEFAULT_MANAGEMENT_ADDRESS, managementMessage);
      }
                            
      ClientConsumer consumer1 = session1.createConsumer(replyTo);
                 
      final RemotingConnection conn1 = ((ClientSessionImpl)session1).getConnection();
 
      conn1.fail(new MessagingException(MessagingException.NOT_CONNECTED));
      
      //Send the other half
      for (int i = 0; i < numMessages / 2; i++)
      {
         ClientMessage managementMessage  = session1.createClientMessage(false);
         
         ManagementHelper.putAttributes(managementMessage,
                                        replyTo,
                                        ManagementServiceImpl.getQueueObjectName(ADDRESS, ADDRESS),
                                        "MessageCount");
         
         managementMessage.getBody().flip();
         
         producer.send(DEFAULT_MANAGEMENT_ADDRESS, managementMessage);
      }
            
      session1.start();
                   
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(1000);
         
         assertNotNull(message);
                        
         message.acknowledge();
         
         assertTrue(ManagementHelper.isAttributesResult(message));
         
         assertEquals(numMessages, message.getProperty(new SimpleString("MessageCount")));
      }
      
      session1.close();
      
      //Make sure no more messages
      ClientSession session2 = sf1.createSession(false, true, true);
      
      session2.start();
      
      ClientConsumer consumer2 = session2.createConsumer(replyTo);
      
      ClientMessage message = consumer2.receive(1000);
      
      assertNull(message);
      
      session2.close();      
   }
   
   public void testManagementMessages2() throws Exception
   {            
      ClientSessionFactoryInternal sf1 = new ClientSessionFactoryImpl(new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory"),
                                                                      new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                                                 backupParams));
      
      sf1.setSendWindowSize(32 * 1024);
  
      ClientSession session1 = sf1.createSession(false, true, true);

      session1.createQueue(ADDRESS, ADDRESS, null, false, false, true);
      
      SimpleString replyTo = new SimpleString("replyto");
      
      session1.createQueue(replyTo, new SimpleString("replyto"), null, false, false, true);
      
      ClientProducer producer = session1.createProducer(ADDRESS);
      
      final int numMessages = 10;
      
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage msg  = session1.createClientMessage(false);
         
         msg.getBody().flip();
         
         producer.send(msg);
      }
      
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage managementMessage  = session1.createClientMessage(false);
         
         ManagementHelper.putAttributes(managementMessage,
                                        replyTo,
                                        ManagementServiceImpl.getQueueObjectName(ADDRESS, ADDRESS),
                                        "MessageCount");
         
         managementMessage.getBody().flip();
         
         producer.send(DEFAULT_MANAGEMENT_ADDRESS, managementMessage);
      }
                            
      ClientConsumer consumer1 = session1.createConsumer(replyTo);
                       
                      
      session1.start();
                   
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(1000);
         
         assertNotNull(message);
         
         if (i == 0)
         {
            //Fail after receipt but before ack
            final RemotingConnection conn1 = ((ClientSessionImpl)session1).getConnection();
            
            conn1.fail(new MessagingException(MessagingException.NOT_CONNECTED));
         }
                        
         message.acknowledge();
         
         assertTrue(ManagementHelper.isAttributesResult(message));
         
         assertEquals(numMessages, message.getProperty(new SimpleString("MessageCount")));
      }
      
      session1.close();
      
      //Make sure no more messages
      ClientSession session2 = sf1.createSession(false, true, true);
      
      session2.start();
      
      ClientConsumer consumer2 = session2.createConsumer(replyTo);
      
      ClientMessage message = consumer2.receive(1000);
      
      assertNull(message);
      
      session2.close();      
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   @Override
   protected void setUp() throws Exception
   {
      Configuration backupConf = new ConfigurationImpl();
      backupConf.setSecurityEnabled(false);
      backupParams.put(TransportConstants.SERVER_ID_PROP_NAME, 1);
      backupConf.getAcceptorConfigurations()
                .add(new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMAcceptorFactory",
                                                backupParams));
      backupConf.setBackup(true);
      backupService = MessagingServiceImpl.newNullStorageMessagingServer(backupConf);
      backupService.start();

      Configuration liveConf = new ConfigurationImpl();
      liveConf.setSecurityEnabled(false);
      liveConf.getAcceptorConfigurations()
              .add(new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMAcceptorFactory"));
      liveConf.setBackupConnectorConfiguration(new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                          backupParams));
      liveService = MessagingServiceImpl.newNullStorageMessagingServer(liveConf);
      liveService.start();
   }

   @Override
   protected void tearDown() throws Exception
   {
      assertEquals(0, backupService.getServer().getRemotingService().getConnections().size());

      backupService.stop();

      assertEquals(0, liveService.getServer().getRemotingService().getConnections().size());

      liveService.stop();

      assertEquals(0, InVMRegistry.instance.size());
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

