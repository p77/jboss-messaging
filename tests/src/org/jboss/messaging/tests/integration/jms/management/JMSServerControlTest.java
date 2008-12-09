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

package org.jboss.messaging.tests.integration.jms.management;

import static org.jboss.messaging.tests.util.RandomUtil.randomString;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.management.MBeanServerInvocationHandler;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.messaging.core.client.impl.ClientSessionFactoryImpl;
import org.jboss.messaging.core.config.Configuration;
import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.config.impl.ConfigurationImpl;
import org.jboss.messaging.core.remoting.impl.invm.InVMAcceptorFactory;
import org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory;
import org.jboss.messaging.core.server.MessagingService;
import org.jboss.messaging.core.server.impl.MessagingServiceImpl;
import org.jboss.messaging.integration.transports.mina.MinaAcceptorFactory;
import org.jboss.messaging.integration.transports.mina.MinaConnectorFactory;
import org.jboss.messaging.integration.transports.netty.NettyAcceptorFactory;
import org.jboss.messaging.integration.transports.netty.NettyConnectorFactory;
import org.jboss.messaging.jms.server.impl.JMSServerManagerImpl;
import org.jboss.messaging.jms.server.management.ConnectionFactoryControlMBean;
import org.jboss.messaging.jms.server.management.JMSServerControlMBean;
import org.jboss.messaging.jms.server.management.impl.JMSManagementServiceImpl;
import org.jboss.messaging.tests.util.RandomUtil;

/**
 * A QueueControlTest
 *
 * @author <a href="jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * Created 14 nov. 2008 13:35:10
 *
 *
 */
public class JMSServerControlTest extends TestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   private static JMSServerControlMBean createJMSServerControl() throws Exception
   {
      JMSServerControlMBean control = (JMSServerControlMBean)MBeanServerInvocationHandler.newProxyInstance(ManagementFactory.getPlatformMBeanServer(),
                                                                                                           JMSManagementServiceImpl.getJMSServerObjectName(),
                                                                                                           JMSServerControlMBean.class,
                                                                                                           false);
      return control;
   }
   
   private static ConnectionFactoryControlMBean createConnectionFactoryControl(String name) throws Exception
   {
      ConnectionFactoryControlMBean control = (ConnectionFactoryControlMBean)MBeanServerInvocationHandler.newProxyInstance(ManagementFactory.getPlatformMBeanServer(),
                                                                                                           JMSManagementServiceImpl.getConnectionFactoryObjectName(name),
                                                                                                           ConnectionFactoryControlMBean.class,
                                                                                                           false);
      return control;
   }

   private MessagingService startMessagingService(String acceptorFactory) throws Exception
   {
      Configuration conf = new ConfigurationImpl();
      conf.setSecurityEnabled(false);
      conf.setJMXManagementEnabled(true);
      conf.getAcceptorConfigurations().add(new TransportConfiguration(acceptorFactory));
      MessagingService service = MessagingServiceImpl.newNullStorageMessagingService(conf);
      service.start();

      JMSServerManagerImpl serverManager = JMSServerManagerImpl.newJMSServerManagerImpl(service.getServer());
      serverManager.start();
      serverManager.setInitialContext(new NullInitialContext());

      return service;
   }
   
   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testListClientConnectionsForInVM() throws Exception
   {
      doListClientConnections(InVMAcceptorFactory.class.getName(), InVMConnectorFactory.class.getName());
   }

   public void testListClientConnectionsForNetty() throws Exception
   {
      doListClientConnections(NettyAcceptorFactory.class.getName(), NettyConnectorFactory.class.getName());
   }

   public void testListClientConnectionsForMina() throws Exception
   {
      doListClientConnections(MinaAcceptorFactory.class.getName(), MinaConnectorFactory.class.getName());
   }

   public void testCloseConnectionsForAddressForInVM() throws Exception
   {
      doCloseConnectionsForAddress(InVMAcceptorFactory.class.getName(), InVMConnectorFactory.class.getName());
   }

   public void testCloseConnectionsForAddressForNetty() throws Exception
   {
      doCloseConnectionsForAddress(NettyAcceptorFactory.class.getName(), NettyConnectorFactory.class.getName());
   }

   public void testCloseConnectionsForAddressForMina() throws Exception
   {
      doCloseConnectionsForAddress(MinaAcceptorFactory.class.getName(), MinaConnectorFactory.class.getName());
   }

   public void testCloseConnectionsForUnknownAddressForInVM() throws Exception
   {
      doCloseConnectionsForUnknownAddress(InVMAcceptorFactory.class.getName(), InVMConnectorFactory.class.getName());
   }

   public void testCloseConnectionsForUnknownAddressForNetty() throws Exception
   {
      doCloseConnectionsForUnknownAddress(NettyAcceptorFactory.class.getName(), NettyConnectorFactory.class.getName());
   }

   public void testCloseConnectionsForUnknownAddressForMina() throws Exception
   {
      doCloseConnectionsForUnknownAddress(MinaAcceptorFactory.class.getName(), MinaConnectorFactory.class.getName());
   }

   public void testListSessionsForInVM() throws Exception
   {
      doListSessions(InVMAcceptorFactory.class.getName(), InVMConnectorFactory.class.getName());
   }

   public void testListSessionsForNetty() throws Exception
   {
      doListSessions(NettyAcceptorFactory.class.getName(), NettyConnectorFactory.class.getName());
   }

   public void testListSessionsForMina() throws Exception
   {
      doListSessions(MinaAcceptorFactory.class.getName(), MinaConnectorFactory.class.getName());
   }

   public void testListConnectionIDsForInVM() throws Exception
   {
      doListConnectionIDs(InVMAcceptorFactory.class.getName(), InVMConnectorFactory.class.getName());
   }

   public void testListConnectionIDsForNetty() throws Exception
   {
      doListConnectionIDs(NettyAcceptorFactory.class.getName(), NettyConnectorFactory.class.getName());
   }

   public void testListConnectionIDsForMina() throws Exception
   {
      doListConnectionIDs(MinaAcceptorFactory.class.getName(), MinaConnectorFactory.class.getName());
   }

   public void testCreateConnectionFactoryWithDiscoveryGroup() throws Exception
   {
      String cfJNDIBinding = randomString();
      String cfName = randomString();
      MessagingService service = null;
      try
      {
         service = startMessagingService(NettyAcceptorFactory.class.getName());

         try {
            ConnectionFactoryControlMBean cfControl = createConnectionFactoryControl(cfName);
            // invoke an operation on the proxy to check that there is no such mbean
            cfControl.getName();
            fail("no CF was created with name " + cfName);          
         } catch (Exception e)
         {
         }
         JMSServerControlMBean control = createJMSServerControl();
         control.createConnectionFactory(cfName,
                                         randomString(),
                                         "localhost",
                                         8765,
                                         ConfigurationImpl.DEFAULT_BROADCAST_REFRESH_TIMEOUT,
                                         ClientSessionFactoryImpl.DEFAULT_DISCOVERY_INITIAL_WAIT,
                                         ClientSessionFactoryImpl.DEFAULT_CONNECTION_LOAD_BALANCING_POLICY_CLASS_NAME,
                                         ClientSessionFactoryImpl.DEFAULT_PING_PERIOD,
                                         ClientSessionFactoryImpl.DEFAULT_CONNECTION_TTL,
                                         ClientSessionFactoryImpl.DEFAULT_CALL_TIMEOUT,
                                         null,
                                         ClientSessionFactoryImpl.DEFAULT_ACK_BATCH_SIZE,
                                         ClientSessionFactoryImpl.DEFAULT_ACK_BATCH_SIZE,
                                         ClientSessionFactoryImpl.DEFAULT_CONSUMER_WINDOW_SIZE,
                                         ClientSessionFactoryImpl.DEFAULT_CONSUMER_MAX_RATE,
                                         ClientSessionFactoryImpl.DEFAULT_SEND_WINDOW_SIZE,
                                         ClientSessionFactoryImpl.DEFAULT_PRODUCER_MAX_RATE,
                                         ClientSessionFactoryImpl.DEFAULT_MIN_LARGE_MESSAGE_SIZE,
                                         ClientSessionFactoryImpl.DEFAULT_BLOCK_ON_ACKNOWLEDGE,
                                         ClientSessionFactoryImpl.DEFAULT_BLOCK_ON_NON_PERSISTENT_SEND,
                                         ClientSessionFactoryImpl.DEFAULT_BLOCK_ON_PERSISTENT_SEND,
                                         ClientSessionFactoryImpl.DEFAULT_AUTO_GROUP,
                                         ClientSessionFactoryImpl.DEFAULT_MAX_CONNECTIONS,
                                         ClientSessionFactoryImpl.DEFAULT_PRE_ACKNOWLEDGE,
                                         ClientSessionFactoryImpl.DEFAULT_RETRY_INTERVAL,
                                         ClientSessionFactoryImpl.DEFAULT_RETRY_INTERVAL_MULTIPLIER,
                                         ClientSessionFactoryImpl.DEFAULT_MAX_RETRIES_BEFORE_FAILOVER,
                                         ClientSessionFactoryImpl.DEFAULT_MAX_RETRIES_AFTER_FAILOVER,
                                         cfJNDIBinding);     
         
         ConnectionFactoryControlMBean cfControl = createConnectionFactoryControl(cfName);
         assertEquals(cfName, cfControl.getName());
      }
      finally
      {
         if (service != null)
         {
            service.stop();
         }
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private void doListConnectionIDs(String acceptorFactory, String connectorFactory) throws Exception
   {
      MessagingService service = null;
      try
      {
         service = startMessagingService(acceptorFactory);

         JMSServerControlMBean control = createJMSServerControl();

         assertEquals(0, control.listConnectionIDs().length);

         Connection connection = JMSUtil.createConnection(connectorFactory);
         connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         String[] connectionIDs = control.listConnectionIDs();
         assertEquals(1, connectionIDs.length);

         Connection connection2 = JMSUtil.createConnection(connectorFactory);
         connection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
         assertEquals(2, control.listConnectionIDs().length);

         connection.close();
         Thread.sleep(500);

         assertEquals(1, control.listConnectionIDs().length);

         connection2.close();
         Thread.sleep(500);

         assertEquals(0, control.listConnectionIDs().length);
      }
      finally
      {
         if (service != null)
         {
            service.stop();
         }
      }
   }

   private void doListSessions(String acceptorFactory, String connectorFactory) throws Exception
   {
      MessagingService service = null;
      try
      {
         service = startMessagingService(acceptorFactory);

         JMSServerControlMBean control = createJMSServerControl();

         assertEquals(0, control.listConnectionIDs().length);

         Connection connection = JMSUtil.createConnection(connectorFactory);
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         String[] connectionIDs = control.listConnectionIDs();
         assertEquals(1, connectionIDs.length);
         String connectionID = connectionIDs[0];

         String[] sessions = control.listSessions(connectionID);
         assertEquals(1, sessions.length);

         session.close();

         sessions = control.listSessions(connectionID);
         assertEquals(0, sessions.length);

         connection.close();

         Thread.sleep(500);

         assertEquals(0, control.listConnectionIDs().length);
      }
      finally
      {
         if (service != null)
         {
            service.stop();
         }
      }
   }

   private void doListClientConnections(String acceptorFactory, String connectorFactory) throws Exception
   {
      MessagingService service = null;
      try
      {
         service = startMessagingService(acceptorFactory);

         JMSServerControlMBean control = createJMSServerControl();

         assertEquals(0, control.listRemoteAddresses().length);

         Connection connection = JMSUtil.createConnection(connectorFactory);
         // the connection won't connect to the server until a session is created
         connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         String[] remoteAddresses = control.listRemoteAddresses();
         assertEquals(1, remoteAddresses.length);

         for (String remoteAddress : remoteAddresses)
         {
            System.out.println(remoteAddress);
         }
         connection.close();

         // FIXME: with Netty, the server is not notified immediately that the connection is closed
         Thread.sleep(500);

         assertEquals(0, control.listRemoteAddresses().length);
      }
      finally
      {
         if (service != null)
         {
            service.stop();
         }
      }

   }

   private void doCloseConnectionsForAddress(String acceptorFactory, String connectorFactory) throws Exception
   {
      MessagingService service = null;
      try
      {
         service = startMessagingService(acceptorFactory);

         JMSServerControlMBean control = createJMSServerControl();

         assertEquals(0, service.getServer().getConnectionCount());
         assertEquals(0, control.listRemoteAddresses().length);

         Connection connection = JMSUtil.createConnection(connectorFactory);
         // the connection won't connect to the server until a session is created
         connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         assertEquals(1, service.getServer().getConnectionCount());

         String[] remoteAddresses = control.listRemoteAddresses();
         assertEquals(1, remoteAddresses.length);
         String remoteAddress = remoteAddresses[0];

         final CountDownLatch exceptionLatch = new CountDownLatch(1);
         connection.setExceptionListener(new ExceptionListener()
         {
            public void onException(JMSException e)
            {
               exceptionLatch.countDown();
            }
         });

         assertTrue(control.closeConnectionsForAddress(remoteAddress));

         boolean gotException = exceptionLatch.await(1, TimeUnit.SECONDS);
         assertTrue("did not received the expected JMSException", gotException);
         assertEquals(0, control.listRemoteAddresses().length);
         assertEquals(0, service.getServer().getConnectionCount());
      }
      finally
      {
         if (service != null)
         {
            service.stop();
         }
      }
   }

   private void doCloseConnectionsForUnknownAddress(String acceptorFactory, String connectorFactory) throws Exception
   {
      String unknownAddress = randomString();

      MessagingService service = null;

      try
      {
         service = startMessagingService(acceptorFactory);

         JMSServerControlMBean control = createJMSServerControl();

         assertEquals(0, service.getServer().getConnectionCount());
         assertEquals(0, control.listRemoteAddresses().length);

         Connection connection = JMSUtil.createConnection(connectorFactory);
         // the connection won't connect to the server until a session is created
         connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         assertEquals(1, service.getServer().getConnectionCount());
         String[] remoteAddresses = control.listRemoteAddresses();
         assertEquals(1, remoteAddresses.length);

         final CountDownLatch exceptionLatch = new CountDownLatch(1);
         connection.setExceptionListener(new ExceptionListener()
         {
            public void onException(JMSException e)
            {
               exceptionLatch.countDown();
            }
         });

         assertFalse(control.closeConnectionsForAddress(unknownAddress));

         boolean gotException = exceptionLatch.await(500, TimeUnit.MILLISECONDS);
         assertFalse(gotException);

         assertEquals(1, control.listRemoteAddresses().length);
         assertEquals(1, service.getServer().getConnectionCount());

      }
      finally
      {
         if (service != null)
         {
            service.stop();
         }
      }
   }
   // Inner classes -------------------------------------------------

}