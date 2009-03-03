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

package org.jboss.messaging.tests.integration.cluster.management;

import static org.jboss.messaging.tests.integration.management.ManagementControlHelper.createMessagingServerControl;
import static org.jboss.messaging.tests.integration.management.ManagementControlHelper.createQueueControl;
import static org.jboss.messaging.tests.util.RandomUtil.randomLong;
import static org.jboss.messaging.tests.util.RandomUtil.randomPositiveLong;
import static org.jboss.messaging.tests.util.RandomUtil.randomSimpleString;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.jboss.messaging.core.client.ClientMessage;
import org.jboss.messaging.core.client.ClientProducer;
import org.jboss.messaging.core.client.ClientSession;
import org.jboss.messaging.core.client.impl.ClientSessionFactoryImpl;
import org.jboss.messaging.core.client.impl.ClientSessionFactoryInternal;
import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.management.MessagingServerControlMBean;
import org.jboss.messaging.core.management.ObjectNames;
import org.jboss.messaging.core.management.QueueControlMBean;
import org.jboss.messaging.core.remoting.impl.invm.InVMConnectorFactory;
import org.jboss.messaging.tests.util.RandomUtil;
import org.jboss.messaging.utils.SimpleString;

/**
 * A ReplicationAwareQueueControlWrapperTest
 *
 * @author <a href="jmesnil@redhat.com">Jeff Mesnil</a>
 */
public class ReplicationAwareMessagingServerControlWrapperTest extends ReplicationAwareTestBase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   private SimpleString address;

   private ClientSession session;

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testCreateQueue() throws Exception
   {
      SimpleString address = randomSimpleString();
      SimpleString name = randomSimpleString();

      MessagingServerControlMBean liveServerControl = createMessagingServerControl(liveMBeanServer);
      ObjectName queueON = ObjectNames.getQueueObjectName(address, name);

      assertResourceNotExists(liveMBeanServer, queueON);
      assertResourceNotExists(backupMBeanServer, queueON);

      liveServerControl.createQueue(address.toString(), name.toString());

      assertResourceExists(liveMBeanServer, queueON);
      assertResourceExists(backupMBeanServer, queueON);
   }

   public void testDestroyQueue() throws Exception
   {
      SimpleString address = randomSimpleString();
      SimpleString name = randomSimpleString();

      MessagingServerControlMBean liveServerControl = createMessagingServerControl(liveMBeanServer);
      ObjectName queueON = ObjectNames.getQueueObjectName(address, name);

      assertResourceNotExists(liveMBeanServer, queueON);
      assertResourceNotExists(backupMBeanServer, queueON);

      // create the queue...
      liveServerControl.createQueue(address.toString(), name.toString());

      assertResourceExists(liveMBeanServer, queueON);
      assertResourceExists(backupMBeanServer, queueON);

      // ... and destroy it
      liveServerControl.destroyQueue(name.toString());

      assertResourceNotExists(liveMBeanServer, queueON);
      assertResourceNotExists(backupMBeanServer, queueON);
   }

   public void testEnableMessageCounters() throws Exception
   {
      MessagingServerControlMBean liveServerControl = createMessagingServerControl(liveMBeanServer);
      MessagingServerControlMBean backupServerControl = createMessagingServerControl(backupMBeanServer);

      assertFalse(liveServerControl.isMessageCounterEnabled());
      assertFalse(backupServerControl.isMessageCounterEnabled());

      liveServerControl.enableMessageCounters();

      assertTrue(liveServerControl.isMessageCounterEnabled());
      assertTrue(backupServerControl.isMessageCounterEnabled());
   }

   public void testDisableMessageCounters() throws Exception
   {
      MessagingServerControlMBean liveServerControl = createMessagingServerControl(liveMBeanServer);
      MessagingServerControlMBean backupServerControl = createMessagingServerControl(backupMBeanServer);

      assertFalse(liveServerControl.isMessageCounterEnabled());
      assertFalse(backupServerControl.isMessageCounterEnabled());

      // enable the counters...
      liveServerControl.enableMessageCounters();

      assertTrue(liveServerControl.isMessageCounterEnabled());
      assertTrue(backupServerControl.isMessageCounterEnabled());

      // and disable them
      liveServerControl.disableMessageCounters();

      assertFalse(liveServerControl.isMessageCounterEnabled());
      assertFalse(backupServerControl.isMessageCounterEnabled());
   }

   public void testResetAllMessageCounters() throws Exception
   {
      MessagingServerControlMBean liveServerControl = createMessagingServerControl(liveMBeanServer);
      liveServerControl.enableMessageCounters();
      liveServerControl.setMessageCounterSamplePeriod(2000);

      QueueControlMBean liveQueueControl = createQueueControl(address, address, liveMBeanServer);
      QueueControlMBean backupQueueControl = createQueueControl(address, address, backupMBeanServer);

      // send on queue
      ClientProducer producer = session.createProducer(address);
      ClientMessage message = session.createClientMessage(false);
      SimpleString key = randomSimpleString();
      long value = randomLong();
      message.putLongProperty(key, value);
      producer.send(message);

      Thread.sleep(liveServerControl.getMessageCounterSamplePeriod() * 2);

      // check the count is to 1 on both live & backup nodes
      CompositeData counter = liveQueueControl.listMessageCounter();
      assertEquals(1, counter.get("count"));
      counter = backupQueueControl.listMessageCounter();
      assertEquals(1, counter.get("count"));

      liveServerControl.resetAllMessageCounters();
      Thread.sleep(liveServerControl.getMessageCounterSamplePeriod() * 2);

      // check the count has been reset to 0 on both live & backup nodes
      counter = liveQueueControl.listMessageCounter();
      assertEquals(0, counter.get("count"));
      counter = backupQueueControl.listMessageCounter();
      assertEquals(0, counter.get("count"));
   }

   public void testSetMessageCounterSamplePeriod() throws Exception
   {
      long newPeriod = randomPositiveLong();

      MessagingServerControlMBean liveServerControl = createMessagingServerControl(liveMBeanServer);
      MessagingServerControlMBean backupServerControl = createMessagingServerControl(backupMBeanServer);

      assertEquals(liveServerControl.getMessageCounterSamplePeriod(),
                   backupServerControl.getMessageCounterSamplePeriod());

      liveServerControl.setMessageCounterSamplePeriod(newPeriod);

      assertEquals(newPeriod, liveServerControl.getMessageCounterSamplePeriod());
      assertEquals(newPeriod, backupServerControl.getMessageCounterSamplePeriod());
   }

   public void testSetMessageCounterMaxDayCount() throws Exception
   {
      int newCount = RandomUtil.randomPositiveInt();

      MessagingServerControlMBean liveServerControl = createMessagingServerControl(liveMBeanServer);
      MessagingServerControlMBean backupServerControl = createMessagingServerControl(backupMBeanServer);

      assertEquals(liveServerControl.getMessageCounterMaxDayCount(), backupServerControl.getMessageCounterMaxDayCount());

      liveServerControl.setMessageCounterMaxDayCount(newCount);

      assertEquals(newCount, liveServerControl.getMessageCounterMaxDayCount());
      assertEquals(newCount, backupServerControl.getMessageCounterMaxDayCount());
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      address = RandomUtil.randomSimpleString();

      ClientSessionFactoryInternal sf = new ClientSessionFactoryImpl(new TransportConfiguration(InVMConnectorFactory.class.getName()),
                                                                     new TransportConfiguration(InVMConnectorFactory.class.getName(),
                                                                                                backupParams));

      session = sf.createSession(false, true, true);

      session.createQueue(address, address, null, false, false);
   }

   @Override
   protected void tearDown() throws Exception
   {
      session.close();

      super.tearDown();
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
