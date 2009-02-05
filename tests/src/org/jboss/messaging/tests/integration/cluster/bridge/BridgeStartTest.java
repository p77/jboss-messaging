/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.messaging.tests.integration.cluster.bridge;

import static org.jboss.messaging.core.remoting.impl.invm.TransportConstants.SERVER_ID_PROP_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.messaging.core.client.ClientConsumer;
import org.jboss.messaging.core.client.ClientMessage;
import org.jboss.messaging.core.client.ClientProducer;
import org.jboss.messaging.core.client.ClientSession;
import org.jboss.messaging.core.client.ClientSessionFactory;
import org.jboss.messaging.core.client.impl.ClientSessionFactoryImpl;
import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.config.cluster.BridgeConfiguration;
import org.jboss.messaging.core.config.cluster.QueueConfiguration;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.server.MessagingService;
import org.jboss.messaging.core.server.cluster.Bridge;
import org.jboss.messaging.tests.util.ServiceTestBase;
import org.jboss.messaging.util.Pair;
import org.jboss.messaging.util.SimpleString;

/**
 * A BridgeStartTest
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * Created 14 Jan 2009 14:05:01
 *
 *
 */
public class BridgeStartTest extends ServiceTestBase
{
   private static final Logger log = Logger.getLogger(BridgeStartTest.class);

   public void testStartStop() throws Exception
   {
      Map<String, Object> service0Params = new HashMap<String, Object>();
      MessagingService service0 = createClusteredServiceWithParams(0, false, service0Params);

      Map<String, Object> service1Params = new HashMap<String, Object>();
      service1Params.put(SERVER_ID_PROP_NAME, 1);
      MessagingService service1 = createClusteredServiceWithParams(1, false, service1Params);

      final String testAddress = "testAddress";
      final String queueName0 = "queue0";
      final String forwardAddress = "forwardAddress";
      final String queueName1 = "queue1";

      Map<String, TransportConfiguration> connectors = new HashMap<String, TransportConfiguration>();
      TransportConfiguration server0tc = new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                    service0Params);
      TransportConfiguration server1tc = new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                    service1Params);
      connectors.put(server1tc.getName(), server1tc);

      service0.getServer().getConfiguration().setConnectorConfigurations(connectors);

      Pair<String, String> connectorPair = new Pair<String, String>(server1tc.getName(), null);

      final String bridgeName = "bridge1";

      BridgeConfiguration bridgeConfiguration = new BridgeConfiguration(bridgeName,
                                                                        queueName0,
                                                                        forwardAddress,
                                                                        null,
                                                                        1,
                                                                        -1,
                                                                        null,
                                                                        1000,
                                                                        1d,
                                                                        0,
                                                                        0,
                                                                        false,
                                                                        connectorPair);

      List<BridgeConfiguration> bridgeConfigs = new ArrayList<BridgeConfiguration>();
      bridgeConfigs.add(bridgeConfiguration);
      service0.getServer().getConfiguration().setBridgeConfigurations(bridgeConfigs);

      QueueConfiguration queueConfig0 = new QueueConfiguration(testAddress, queueName0, null, true);
      List<QueueConfiguration> queueConfigs0 = new ArrayList<QueueConfiguration>();
      queueConfigs0.add(queueConfig0);
      service0.getServer().getConfiguration().setQueueConfigurations(queueConfigs0);

      QueueConfiguration queueConfig1 = new QueueConfiguration(forwardAddress, queueName1, null, true);
      List<QueueConfiguration> queueConfigs1 = new ArrayList<QueueConfiguration>();
      queueConfigs1.add(queueConfig1);
      service1.getServer().getConfiguration().setQueueConfigurations(queueConfigs1);

      service1.start();
      service0.start();

      ClientSessionFactory sf0 = new ClientSessionFactoryImpl(server0tc);

      ClientSessionFactory sf1 = new ClientSessionFactoryImpl(server1tc);

      ClientSession session0 = sf0.createSession(false, true, true);

      ClientSession session1 = sf1.createSession(false, true, true);

      ClientProducer producer0 = session0.createProducer(new SimpleString(testAddress));

      ClientConsumer consumer1 = session1.createConsumer(queueName1);

      session1.start();

      final int numMessages = 10;

      final SimpleString propKey = new SimpleString("testkey");

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session0.createClientMessage(false);

         message.putIntProperty(propKey, i);

         producer0.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(200);

         assertNotNull(message);

         assertEquals((Integer)i, (Integer)message.getProperty(propKey));

         message.acknowledge();
      }

      assertNull(consumer1.receive(200));

      Bridge bridge = service0.getServer().getClusterManager().getBridges().get(bridgeName);

      bridge.stop();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session0.createClientMessage(false);

         message.putIntProperty(propKey, i);

         producer0.send(message);
      }

      assertNull(consumer1.receive(500));

      bridge.start();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(1000);

         assertNotNull(message);

         assertEquals((Integer)i, (Integer)message.getProperty(propKey));

         message.acknowledge();
      }

      assertNull(consumer1.receive(200));

      session0.close();

      session1.close();

      sf0.close();

      sf1.close();

      service0.stop();

      service1.stop();
   }

   public void testTargetServerUpAndDown() throws Exception
   {
      Map<String, Object> service0Params = new HashMap<String, Object>();
      MessagingService service0 = createClusteredServiceWithParams(0, false, service0Params);

      Map<String, Object> service1Params = new HashMap<String, Object>();
      service1Params.put(SERVER_ID_PROP_NAME, 1);
      MessagingService service1 = createClusteredServiceWithParams(1, false, service1Params);

      final String testAddress = "testAddress";
      final String queueName0 = "queue0";
      final String forwardAddress = "forwardAddress";
      final String queueName1 = "queue1";

      Map<String, TransportConfiguration> connectors = new HashMap<String, TransportConfiguration>();
      TransportConfiguration server0tc = new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                    service0Params);
      TransportConfiguration server1tc = new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                    service1Params);
      connectors.put(server1tc.getName(), server1tc);

      service0.getServer().getConfiguration().setConnectorConfigurations(connectors);

      Pair<String, String> connectorPair = new Pair<String, String>(server1tc.getName(), null);

      final String bridgeName = "bridge1";

      BridgeConfiguration bridgeConfiguration = new BridgeConfiguration(bridgeName,
                                                                        queueName0,
                                                                        forwardAddress,
                                                                        null,
                                                                        1,
                                                                        -1,
                                                                        null,
                                                                        1000,
                                                                        1d,
                                                                        -1,
                                                                        -1,
                                                                        false,
                                                                        connectorPair);

      List<BridgeConfiguration> bridgeConfigs = new ArrayList<BridgeConfiguration>();
      bridgeConfigs.add(bridgeConfiguration);
      service0.getServer().getConfiguration().setBridgeConfigurations(bridgeConfigs);

      QueueConfiguration queueConfig0 = new QueueConfiguration(testAddress, queueName0, null, true);
      List<QueueConfiguration> queueConfigs0 = new ArrayList<QueueConfiguration>();
      queueConfigs0.add(queueConfig0);
      service0.getServer().getConfiguration().setQueueConfigurations(queueConfigs0);

      QueueConfiguration queueConfig1 = new QueueConfiguration(forwardAddress, queueName1, null, true);
      List<QueueConfiguration> queueConfigs1 = new ArrayList<QueueConfiguration>();
      queueConfigs1.add(queueConfig1);
      service1.getServer().getConfiguration().setQueueConfigurations(queueConfigs1);

      try
      {
         // Don't start service 1 yet

         service0.start();

         ClientSessionFactory sf0 = new ClientSessionFactoryImpl(server0tc);

         ClientSession session0 = sf0.createSession(false, true, true);

         ClientProducer producer0 = session0.createProducer(new SimpleString(testAddress));

         final int numMessages = 10;

         final SimpleString propKey = new SimpleString("testkey");

         for (int i = 0; i < numMessages; i++)
         {
            ClientMessage message = session0.createClientMessage(false);

            message.putIntProperty(propKey, i);

            producer0.send(message);
         }

         // Wait a bit
         Thread.sleep(1000);

         service1.start();

         ClientSessionFactory sf1 = new ClientSessionFactoryImpl(server1tc);

         ClientSession session1 = sf1.createSession(false, true, true);

         ClientConsumer consumer1 = session1.createConsumer(queueName1);

         session1.start();

         for (int i = 0; i < numMessages; i++)
         {
            ClientMessage message = consumer1.receive(1000);

            assertNotNull(message);

            assertEquals((Integer)i, (Integer)message.getProperty(propKey));

            message.acknowledge();
         }

         assertNull(consumer1.receive(200));

         for (int i = 0; i < numMessages; i++)
         {
            ClientMessage message = session0.createClientMessage(false);

            message.putIntProperty(propKey, i);

            producer0.send(message);
         }

         for (int i = 0; i < numMessages; i++)
         {
            ClientMessage message = consumer1.receive(1000);

            assertNotNull(message);

            assertEquals((Integer)i, (Integer)message.getProperty(propKey));

            message.acknowledge();
         }

         assertNull(consumer1.receive(200));

         session1.close();

         sf1.close();

         service1.stop();

         for (int i = 0; i < numMessages; i++)
         {
            ClientMessage message = session0.createClientMessage(false);

            message.putIntProperty(propKey, i);

            producer0.send(message);
         }


         service1.start();

         sf1 = new ClientSessionFactoryImpl(server1tc);

         session1 = sf1.createSession(false, true, true);

         consumer1 = session1.createConsumer(queueName1);


         session1.start();

         for (int i = 0; i < numMessages; i++)
         {
            ClientMessage message = consumer1.receive(1000);

            assertNotNull(message);

            assertEquals((Integer)i, (Integer)message.getProperty(propKey));

            message.acknowledge();
         }

         assertNull(consumer1.receive(200));


         session1.close();

         sf1.close();

         session0.close();

         sf0.close();

      }
      finally
      {
         service0.stop();

         service1.stop();
      }
   }

   public void testTargetServerNotAvailableNoReconnectTries() throws Exception
   {
      Map<String, Object> service0Params = new HashMap<String, Object>();
      MessagingService service0 = createClusteredServiceWithParams(0, false, service0Params);

      Map<String, Object> service1Params = new HashMap<String, Object>();
      service1Params.put(SERVER_ID_PROP_NAME, 1);
      MessagingService service1 = createClusteredServiceWithParams(1, false, service1Params);

      final String testAddress = "testAddress";
      final String queueName0 = "queue0";
      final String forwardAddress = "forwardAddress";
      final String queueName1 = "queue1";

      Map<String, TransportConfiguration> connectors = new HashMap<String, TransportConfiguration>();
      TransportConfiguration server0tc = new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                    service0Params);
      TransportConfiguration server1tc = new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                    service1Params);
      connectors.put(server1tc.getName(), server1tc);

      service0.getServer().getConfiguration().setConnectorConfigurations(connectors);

      Pair<String, String> connectorPair = new Pair<String, String>(server1tc.getName(), null);

      final String bridgeName = "bridge1";

      BridgeConfiguration bridgeConfiguration = new BridgeConfiguration(bridgeName,
                                                                        queueName0,
                                                                        forwardAddress,
                                                                        null,
                                                                        1,
                                                                        -1,
                                                                        null,
                                                                        1000,
                                                                        1d,
                                                                        0,
                                                                        0,
                                                                        false,
                                                                        connectorPair);

      List<BridgeConfiguration> bridgeConfigs = new ArrayList<BridgeConfiguration>();
      bridgeConfigs.add(bridgeConfiguration);
      service0.getServer().getConfiguration().setBridgeConfigurations(bridgeConfigs);

      QueueConfiguration queueConfig0 = new QueueConfiguration(testAddress, queueName0, null, true);
      List<QueueConfiguration> queueConfigs0 = new ArrayList<QueueConfiguration>();
      queueConfigs0.add(queueConfig0);
      service0.getServer().getConfiguration().setQueueConfigurations(queueConfigs0);

      QueueConfiguration queueConfig1 = new QueueConfiguration(forwardAddress, queueName1, null, true);
      List<QueueConfiguration> queueConfigs1 = new ArrayList<QueueConfiguration>();
      queueConfigs1.add(queueConfig1);
      service1.getServer().getConfiguration().setQueueConfigurations(queueConfigs1);

      // Don't start service 1 yet

      service0.start();

      ClientSessionFactory sf0 = new ClientSessionFactoryImpl(server0tc);

      ClientSession session0 = sf0.createSession(false, true, true);

      ClientProducer producer0 = session0.createProducer(new SimpleString(testAddress));

      final int numMessages = 10;

      final SimpleString propKey = new SimpleString("testkey");

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session0.createClientMessage(false);

         message.putIntProperty(propKey, i);

         producer0.send(message);
      }

      // Wait a bit
      Thread.sleep(1000);

      // Bridge should be stopped since retries = 0
;
      service1.start();

      ClientSessionFactory sf1 = new ClientSessionFactoryImpl(server1tc);

      ClientSession session1 = sf1.createSession(false, true, true);

      ClientConsumer consumer1 = session1.createConsumer(queueName1);

      session1.start();

      // Won't be received since the bridge was deactivated
      assertNull(consumer1.receive(200));

      // Now start the bridge manually

      Bridge bridge = service0.getServer().getClusterManager().getBridges().get(bridgeName);

      bridge.start();

      // Messages should now be received

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(1000);
         
         assertNotNull(message);

         assertEquals((Integer)i, (Integer)message.getProperty(propKey));

         message.acknowledge();
      }

      assertNull(consumer1.receive(200));

      session1.close();

      sf1.close();

      session0.close();

      sf0.close();

      service0.stop();

      service1.stop();
   }

   public void testManualStopStart() throws Exception
   {
      Map<String, Object> service0Params = new HashMap<String, Object>();
      MessagingService service0 = createClusteredServiceWithParams(0, false, service0Params);

      Map<String, Object> service1Params = new HashMap<String, Object>();
      service1Params.put(SERVER_ID_PROP_NAME, 1);
      MessagingService service1 = createClusteredServiceWithParams(1, false, service1Params);

      final String testAddress = "testAddress";
      final String queueName0 = "queue0";
      final String forwardAddress = "forwardAddress";
      final String queueName1 = "queue1";

      Map<String, TransportConfiguration> connectors = new HashMap<String, TransportConfiguration>();
      TransportConfiguration server0tc = new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                    service0Params);
      TransportConfiguration server1tc = new TransportConfiguration("org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory",
                                                                    service1Params);
      connectors.put(server1tc.getName(), server1tc);

      service0.getServer().getConfiguration().setConnectorConfigurations(connectors);

      Pair<String, String> connectorPair = new Pair<String, String>(server1tc.getName(), null);

      final String bridgeName = "bridge1";

      BridgeConfiguration bridgeConfiguration = new BridgeConfiguration(bridgeName,
                                                                        queueName0,
                                                                        forwardAddress,
                                                                        null,
                                                                        1,
                                                                        -1,
                                                                        null,
                                                                        1000,
                                                                        1d,
                                                                        0,
                                                                        0,
                                                                        false,
                                                                        connectorPair);

      List<BridgeConfiguration> bridgeConfigs = new ArrayList<BridgeConfiguration>();
      bridgeConfigs.add(bridgeConfiguration);
      service0.getServer().getConfiguration().setBridgeConfigurations(bridgeConfigs);

      QueueConfiguration queueConfig0 = new QueueConfiguration(testAddress, queueName0, null, true);
      List<QueueConfiguration> queueConfigs0 = new ArrayList<QueueConfiguration>();
      queueConfigs0.add(queueConfig0);
      service0.getServer().getConfiguration().setQueueConfigurations(queueConfigs0);

      QueueConfiguration queueConfig1 = new QueueConfiguration(forwardAddress, queueName1, null, true);
      List<QueueConfiguration> queueConfigs1 = new ArrayList<QueueConfiguration>();
      queueConfigs1.add(queueConfig1);
      service1.getServer().getConfiguration().setQueueConfigurations(queueConfigs1);

      service1.start();

      service0.start();

      ClientSessionFactory sf0 = new ClientSessionFactoryImpl(server0tc);

      ClientSession session0 = sf0.createSession(false, true, true);

      ClientProducer producer0 = session0.createProducer(new SimpleString(testAddress));

      final int numMessages = 10;

      final SimpleString propKey = new SimpleString("testkey");

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session0.createClientMessage(false);

         message.putIntProperty(propKey, i);

         producer0.send(message);
      }

      ClientSessionFactory sf1 = new ClientSessionFactoryImpl(server1tc);

      ClientSession session1 = sf1.createSession(false, true, true);

      ClientConsumer consumer1 = session1.createConsumer(queueName1);

      session1.start();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(1000);

         assertNotNull(message);

         assertEquals((Integer)i, (Integer)message.getProperty(propKey));

         message.acknowledge();
      }

      assertNull(consumer1.receive(200));

      // Now stop the bridge manually

      Bridge bridge = service0.getServer().getClusterManager().getBridges().get(bridgeName);

      bridge.stop();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session0.createClientMessage(false);

         message.putIntProperty(propKey, i);

         producer0.send(message);
      }

      assertNull(consumer1.receive(200));

      bridge.start();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(1000);

         assertNotNull(message);

         assertEquals((Integer)i, (Integer)message.getProperty(propKey));

         message.acknowledge();
      }

      assertNull(consumer1.receive(200));

      bridge.stop();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session0.createClientMessage(false);

         message.putIntProperty(propKey, i);

         producer0.send(message);
      }

      assertNull(consumer1.receive(200));

      bridge.start();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(1000);

         assertNotNull(message);

         assertEquals((Integer)i, (Integer)message.getProperty(propKey));

         message.acknowledge();
      }

      assertNull(consumer1.receive(200));

      session1.close();

      sf1.close();

      session0.close();

      sf0.close();

      service0.stop();

      service1.stop();
   }

}
