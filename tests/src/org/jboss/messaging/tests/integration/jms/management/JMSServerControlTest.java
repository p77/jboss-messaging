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

import static org.jboss.messaging.tests.util.RandomUtil.randomBoolean;
import static org.jboss.messaging.tests.util.RandomUtil.randomDouble;
import static org.jboss.messaging.tests.util.RandomUtil.randomPositiveInt;
import static org.jboss.messaging.tests.util.RandomUtil.randomPositiveLong;
import static org.jboss.messaging.tests.util.RandomUtil.randomString;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.messaging.core.client.impl.ClientSessionFactoryImpl;
import org.jboss.messaging.core.config.Configuration;
import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.config.impl.ConfigurationImpl;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.management.ObjectNames;
import org.jboss.messaging.core.remoting.impl.invm.InVMAcceptorFactory;
import org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory;
import org.jboss.messaging.core.server.Messaging;
import org.jboss.messaging.core.server.impl.MessagingServiceImpl;
import org.jboss.messaging.jms.server.impl.JMSServerManagerImpl;
import org.jboss.messaging.jms.server.management.ConnectionFactoryControlMBean;
import org.jboss.messaging.jms.server.management.JMSServerControlMBean;
import org.jboss.messaging.tests.integration.management.ManagementControlHelper;
import org.jboss.messaging.tests.unit.util.InVMContext;
import org.jboss.messaging.tests.util.UnitTestCase;

/**
 * A QueueControlTest
 *
 * @author <a href="jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * Created 14 nov. 2008 13:35:10
 *
 *
 */
public class JMSServerControlTest extends UnitTestCase
{
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(JMSServerControlTest.class);

   // Attributes ----------------------------------------------------

   private MBeanServer mbeanServer;

   protected InVMContext context;

   protected MessagingServiceImpl service;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testGetVersion() throws Exception
   {
      JMSServerControlMBean control = createManagementControl();
      String version = control.getVersion();
      assertEquals(service.getServer().getVersion().getFullVersion(), version);
   }

   public void testCreateQueue() throws Exception
   {
      String queueJNDIBinding = randomString();
      String queueName = randomString();

      checkNoBinding(queueJNDIBinding);
      checkNoResource(ObjectNames.getJMSQueueObjectName(queueName));

      JMSServerControlMBean control = createManagementControl();
      control.createQueue(queueName, queueJNDIBinding);

      Object o = checkBinding(queueJNDIBinding);
      assertTrue(o instanceof Queue);
      Queue queue = (Queue)o;
      assertEquals(queueName, queue.getQueueName());
      checkResource(ObjectNames.getJMSQueueObjectName(queueName));

   }

   public void testDestroyQueue() throws Exception
   {
      String queueJNDIBinding = randomString();
      String queueName = randomString();

      checkNoBinding(queueJNDIBinding);
      checkNoResource(ObjectNames.getJMSQueueObjectName(queueName));

      JMSServerControlMBean control = createManagementControl();
      control.createQueue(queueName, queueJNDIBinding);

      checkBinding(queueJNDIBinding);
      checkResource(ObjectNames.getJMSQueueObjectName(queueName));

      control.destroyQueue(queueName);

      checkNoBinding(queueJNDIBinding);
      checkNoResource(ObjectNames.getJMSQueueObjectName(queueName));

   }

   public void testCreateTopic() throws Exception
   {
      String topicJNDIBinding = randomString();
      String topicName = randomString();

      checkNoBinding(topicJNDIBinding);
      checkNoResource(ObjectNames.getJMSTopicObjectName(topicName));

      JMSServerControlMBean control = createManagementControl();
      control.createTopic(topicName, topicJNDIBinding);

      Object o = checkBinding(topicJNDIBinding);
      assertTrue(o instanceof Topic);
      Topic topic = (Topic)o;
      assertEquals(topicName, topic.getTopicName());
      checkResource(ObjectNames.getJMSTopicObjectName(topicName));
   }

   public void testDestroyTopic() throws Exception
   {
      String topicJNDIBinding = randomString();
      String topicName = randomString();

      checkNoBinding(topicJNDIBinding);
      checkNoResource(ObjectNames.getJMSTopicObjectName(topicName));

      JMSServerControlMBean control = createManagementControl();
      control.createTopic(topicName, topicJNDIBinding);

      checkBinding(topicJNDIBinding);
      checkResource(ObjectNames.getJMSTopicObjectName(topicName));

      control.destroyTopic(topicName);

      checkNoBinding(topicJNDIBinding);
      checkNoResource(ObjectNames.getJMSTopicObjectName(topicName));
   }

   public void testCreateConnectionFactory() throws Exception
   {
      String cfJNDIBinding = randomString();
      String cfName = randomString();

      checkNoBinding(cfJNDIBinding);
      checkNoResource(ObjectNames.getConnectionFactoryObjectName(cfName));

      JMSServerControlMBean control = createManagementControl();
      control.createConnectionFactory(cfName, InVMConnectorFactory.class.getName(), cfJNDIBinding);

      Object o = checkBinding(cfJNDIBinding);
      assertTrue(o instanceof ConnectionFactory);
      ConnectionFactory cf = (ConnectionFactory)o;
      Connection connection = cf.createConnection();
      connection.close();
      checkResource(ObjectNames.getConnectionFactoryObjectName(cfName));
   }

   public void testCreateConnectionFactory_2() throws Exception
   {
      String cfJNDIBinding = randomString();
      String cfName = randomString();
      boolean preAcknowledge = randomBoolean();
      boolean blockOnAcknowledge = randomBoolean();
      boolean blockOnNonPersistentSend = randomBoolean();
      boolean blockOnPersistentSend = randomBoolean();

      checkNoBinding(cfJNDIBinding);
      checkNoResource(ObjectNames.getConnectionFactoryObjectName(cfName));

      JMSServerControlMBean control = createManagementControl();
      control.createConnectionFactory(cfName,
                                      InVMConnectorFactory.class.getName(),
                                      blockOnAcknowledge,
                                      blockOnNonPersistentSend,
                                      blockOnPersistentSend,
                                      preAcknowledge,
                                      cfJNDIBinding);

      Object o = checkBinding(cfJNDIBinding);
      assertTrue(o instanceof ConnectionFactory);
      ConnectionFactory cf = (ConnectionFactory)o;
      Connection connection = cf.createConnection();
      connection.close();

      checkResource(ObjectNames.getConnectionFactoryObjectName(cfName));
      ConnectionFactoryControlMBean cfControl = ManagementControlHelper.createConnectionFactoryControl(cfName,
                                                                                                       mbeanServer);
      assertEquals(preAcknowledge, cfControl.isPreAcknowledge());
      assertEquals(blockOnAcknowledge, cfControl.isBlockOnAcknowledge());
      assertEquals(blockOnNonPersistentSend, cfControl.isBlockOnNonPersistentSend());
      assertEquals(blockOnPersistentSend, cfControl.isBlockOnPersistentSend());
   }

   public void testCreateConnectionFactory_3() throws Exception
   {
      String cfJNDIBinding = randomString();
      String cfName = randomString();
      long pingPeriod = randomPositiveLong();
      long connectionTTL = randomPositiveLong();
      long callTimeout = randomPositiveLong();
      String clientID = randomString();
      int dupsOKBatchSize = randomPositiveInt();
      int transactionBatchSize = randomPositiveInt();
      int consumerWindowSize = randomPositiveInt();
      int consumerMaxRate = randomPositiveInt();
      int producerWindowSize = randomPositiveInt();
      int producerMaxRate = randomPositiveInt();
      int minLargeMessageSize = randomPositiveInt();
      boolean autoGroup = randomBoolean();
      int maxConnections = randomPositiveInt();
      long retryInterval = randomPositiveLong();
      double retryIntervalMultiplier = randomDouble();
      int maxRetriesBeforeFailover = randomPositiveInt();
      int maxRetriesAfterFailover = randomPositiveInt();
      boolean preAcknowledge = randomBoolean();
      boolean blockOnAcknowledge = randomBoolean();
      boolean blockOnNonPersistentSend = randomBoolean();
      boolean blockOnPersistentSend = randomBoolean();

      checkNoBinding(cfJNDIBinding);
      checkNoResource(ObjectNames.getConnectionFactoryObjectName(cfName));

      JMSServerControlMBean control = createManagementControl();

      control.createSimpleConnectionFactory(cfName,
                                      InVMConnectorFactory.class.getName(),
                                      ClientSessionFactoryImpl.DEFAULT_CONNECTION_LOAD_BALANCING_POLICY_CLASS_NAME,
                                      pingPeriod,
                                      connectionTTL,
                                      callTimeout,
                                      clientID,
                                      dupsOKBatchSize,
                                      transactionBatchSize,
                                      consumerWindowSize,
                                      consumerMaxRate,
                                      producerWindowSize,
                                      producerMaxRate,
                                      minLargeMessageSize,
                                      blockOnAcknowledge,
                                      blockOnNonPersistentSend,
                                      blockOnPersistentSend,
                                      autoGroup,
                                      maxConnections,
                                      preAcknowledge,
                                      retryInterval,
                                      retryIntervalMultiplier,
                                      maxRetriesBeforeFailover,
                                      maxRetriesAfterFailover,
                                      cfJNDIBinding);

      Object o = checkBinding(cfJNDIBinding);
      assertTrue(o instanceof ConnectionFactory);
      ConnectionFactory cf = (ConnectionFactory)o;
      Connection connection = cf.createConnection();
      connection.close();

      checkResource(ObjectNames.getConnectionFactoryObjectName(cfName));
      ConnectionFactoryControlMBean cfControl = ManagementControlHelper.createConnectionFactoryControl(cfName,
                                                                                                       mbeanServer);
      assertEquals(cfName, cfControl.getName());
      assertEquals(pingPeriod, cfControl.getPingPeriod());
      assertEquals(connectionTTL, cfControl.getConnectionTTL());
      assertEquals(callTimeout, cfControl.getCallTimeout());
      assertEquals(clientID, cfControl.getClientID());
      assertEquals(dupsOKBatchSize, cfControl.getDupsOKBatchSize());
      assertEquals(transactionBatchSize, cfControl.getTransactionBatchSize());
      assertEquals(consumerWindowSize, cfControl.getConsumerWindowSize());
      assertEquals(consumerMaxRate, cfControl.getConsumerMaxRate());
      assertEquals(producerWindowSize, cfControl.getProducerWindowSize());
      assertEquals(producerMaxRate, cfControl.getProducerMaxRate());
      assertEquals(minLargeMessageSize, cfControl.getMinLargeMessageSize());
      assertEquals(autoGroup, cfControl.isAutoGroup());
      assertEquals(maxConnections, cfControl.getMaxConnections());
      assertEquals(retryInterval, cfControl.getRetryInterval());
      assertEquals(retryIntervalMultiplier, cfControl.getRetryIntervalMultiplier());
      assertEquals(maxRetriesBeforeFailover, cfControl.getMaxRetriesBeforeFailover());
      assertEquals(maxRetriesAfterFailover, cfControl.getMaxRetriesAfterFailover());
      assertEquals(preAcknowledge, cfControl.isPreAcknowledge());
      assertEquals(blockOnAcknowledge, cfControl.isBlockOnAcknowledge());
      assertEquals(blockOnNonPersistentSend, cfControl.isBlockOnNonPersistentSend());
      assertEquals(blockOnPersistentSend, cfControl.isBlockOnPersistentSend());
   }

   public void testDestroyConnectionFactory() throws Exception
   {
      String cfJNDIBinding = randomString();
      String cfName = randomString();

      checkNoBinding(cfJNDIBinding);
      checkNoResource(ObjectNames.getConnectionFactoryObjectName(cfName));

      JMSServerControlMBean control = createManagementControl();
      control.createConnectionFactory(cfName, InVMConnectorFactory.class.getName(), cfJNDIBinding);

      Object o = checkBinding(cfJNDIBinding);
      assertTrue(o instanceof ConnectionFactory);
      ConnectionFactory cf = (ConnectionFactory)o;
      Connection connection = cf.createConnection();
      connection.close();
      checkResource(ObjectNames.getConnectionFactoryObjectName(cfName));

      control.destroyConnectionFactory(cfName);

      checkNoBinding(cfJNDIBinding);
      checkNoResource(ObjectNames.getConnectionFactoryObjectName(cfName));
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      mbeanServer = MBeanServerFactory.createMBeanServer();
      Configuration conf = new ConfigurationImpl();
      conf.setSecurityEnabled(false);
      conf.setJMXManagementEnabled(true);
      conf.getAcceptorConfigurations().add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
      service = Messaging.newNullStorageMessagingService(conf, mbeanServer);
      service.start();

      context = new InVMContext();
      JMSServerManagerImpl serverManager = JMSServerManagerImpl.newJMSServerManagerImpl(service.getServer());
      serverManager.start();
      serverManager.setContext(context);
   }

   @Override
   protected void tearDown() throws Exception
   {
      service.stop();

      super.tearDown();
   }

   protected JMSServerControlMBean createManagementControl() throws Exception
   {
      return ManagementControlHelper.createJMSServerControl(mbeanServer);
   }

   // Private -------------------------------------------------------

   private void checkNoBinding(String binding)
   {
      try
      {
         context.lookup(binding);
         fail("there must be no resource to look up for " + binding);
      }
      catch (Exception e)
      {
      }
   }

   private Object checkBinding(String binding) throws Exception
   {
      Object o = context.lookup(binding);
      assertNotNull(o);
      return o;
   }

   private void checkNoResource(ObjectName on)
   {
      assertFalse(mbeanServer.isRegistered(on));
   }

   private void checkResource(ObjectName on)
   {
      assertTrue(mbeanServer.isRegistered(on));
   }

   // Inner classes -------------------------------------------------

}