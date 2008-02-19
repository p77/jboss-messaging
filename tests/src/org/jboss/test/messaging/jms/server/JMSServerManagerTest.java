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
package org.jboss.test.messaging.jms.server;

import org.jboss.test.messaging.JBMServerTestCase;
import org.jboss.jms.server.JMSServerManager;
import org.jboss.jms.server.ClientInfo;
import org.jboss.jms.client.JBossConnectionFactory;
import org.jboss.jms.destination.JBossQueue;
import org.jboss.messaging.core.impl.server.SubscriptionInfo;

import javax.jms.*;
import javax.naming.NameNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="ataylor@redhat.com">Andy Taylor</a>
 */
public class JMSServerManagerTest extends JBMServerTestCase
{
   JMSServerManager jmsServerManager;

   protected void setUp() throws Exception
   {
      super.setUp();
      jmsServerManager = getJmsServerManager();
   }

   public void testIsStarted()
   {
      assertTrue(jmsServerManager.isStarted());
   }


   public void testCreateAndDestroyQueue() throws Exception
   {
      jmsServerManager.createQueue("anewtestqueue", "anewtestqueue");
      Queue q = (Queue) getInitialContext().lookup("anewtestqueue");
      assertNotNull(q);
      jmsServerManager.destroyQueue("anewtestqueue");
      try
      {
         getInitialContext().lookup("anewtestqueue");
         fail("should throw eception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }
      jmsServerManager.createQueue("anewtestqueue", "/anewtestqueue");
      q = (Queue) getInitialContext().lookup("/anewtestqueue");
      assertNotNull(q);
      jmsServerManager.destroyQueue("anewtestqueue");
      try
      {
         getInitialContext().lookup("/anewtestqueue");
         fail("should throw eception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }

      jmsServerManager.createQueue("anewtestqueue", "/queues/anewtestqueue");
      getInitialContext().lookup("/queues/anewtestqueue");
      assertNotNull(q);
      jmsServerManager.destroyQueue("anewtestqueue");
      try
      {
         getInitialContext().lookup("/queues/newtestqueue");
         fail("should throw eception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }

      jmsServerManager.createQueue("anewtestqueue", "/queues/and/anewtestqueue");
      q = (Queue) getInitialContext().lookup("/queues/and/anewtestqueue");
      assertNotNull(q);
      jmsServerManager.destroyQueue("anewtestqueue");
      try
      {
         getInitialContext().lookup("/queues/and/anewtestqueue");
         fail("should throw eception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }
   }

   public void testCreateAndDestroyTopic() throws Exception
   {
      jmsServerManager.createTopic("anewtesttopic", "anewtesttopic");
      Topic q = (Topic) getInitialContext().lookup("anewtesttopic");
      assertNotNull(q);
      jmsServerManager.destroyTopic("anewtesttopic");
      try
      {
         q = (Topic) getInitialContext().lookup("anewtesttopic");
         fail("should throw eception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }
      jmsServerManager.createTopic("anewtesttopic", "/anewtesttopic");
      q = (Topic) getInitialContext().lookup("/anewtesttopic");
      assertNotNull(q);
      jmsServerManager.destroyTopic("anewtesttopic");
      try
      {
         q = (Topic) getInitialContext().lookup("/anewtesttopic");
         fail("should throw eception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }

      jmsServerManager.createTopic("anewtesttopic", "/topics/anewtesttopic");
      q = (Topic) getInitialContext().lookup("/topics/anewtesttopic");
      assertNotNull(q);
      jmsServerManager.destroyTopic("anewtesttopic");
      try
      {
         q = (Topic) getInitialContext().lookup("/topics/newtesttopic");
         fail("should throw eception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }

      jmsServerManager.createTopic("anewtesttopic", "/topics/and/anewtesttopic");
      q = (Topic) getInitialContext().lookup("/topics/and/anewtesttopic");
      assertNotNull(q);
      jmsServerManager.destroyTopic("anewtesttopic");
      try
      {
         q = (Topic) getInitialContext().lookup("/topics/and/anewtesttopic");
         fail("should throw eception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }
   }

   public void testCreateAndDestroyConectionFactory() throws Exception
   {
      jmsServerManager.createConnectionFactory("newtestcf", "anid", 100, true, 100, "newtestcf");
      JBossConnectionFactory jbcf = (JBossConnectionFactory) getInitialContext().lookup("newtestcf");
      assertNotNull(jbcf);
      assertNotNull(jbcf.getDelegate());
      jmsServerManager.destroyConnectionFactory("newtestcf");
      try
      {
         getInitialContext().lookup("newtestcf");
         fail("should throw exception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }
      ArrayList<String> bindings = new ArrayList<String>();
      bindings.add("oranewtestcf");
      bindings.add("newtestcf");
      jmsServerManager.createConnectionFactory("newtestcf", "anid", 100, true, 100, bindings);
      jbcf = (JBossConnectionFactory) getInitialContext().lookup("newtestcf");
      assertNotNull(jbcf);
      assertNotNull(jbcf.getDelegate());
      jbcf = (JBossConnectionFactory) getInitialContext().lookup("oranewtestcf");
      assertNotNull(jbcf);
      assertNotNull(jbcf.getDelegate());
      jmsServerManager.destroyConnectionFactory("newtestcf");
      try
      {
         getInitialContext().lookup("newtestcf");
         fail("should throw exception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }
      try
      {
         getInitialContext().lookup("oranewtestcf");
         fail("should throw exception");
      }
      catch (NameNotFoundException e)
      {
         //pass
      }
   }

   public void testClientInfo() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      List<ClientInfo> clientInfos = jmsServerManager.getClients();
      assertNotNull(clientInfos);
      assertEquals(1, clientInfos.size());
      ClientInfo clientInfo = clientInfos.get(0);
      assertEquals("guest", clientInfo.getUser());
      assertEquals(ClientInfo.status.STOPPED, clientInfo.getStatus());
      conn.start();
      clientInfos = jmsServerManager.getClients();
      assertNotNull(clientInfos);
      assertEquals(1, clientInfos.size());
      clientInfo = clientInfos.get(0);
      assertEquals(ClientInfo.status.STARTED, clientInfo.getStatus());
      clientInfo.getAddress();
      clientInfo.getTimeCreated();
      clientInfo.getAliveTime();
      conn.close();
      clientInfos = jmsServerManager.getClients();
      assertNotNull(clientInfos);
      assertEquals(0, clientInfos.size());
      Connection conn2 = getConnectionFactory().createConnection("guest", "guest");
      Connection conn3 = getConnectionFactory().createConnection("guest", "guest");
      clientInfos = jmsServerManager.getClients();
      assertNotNull(clientInfos);
      assertEquals(2, clientInfos.size());
      conn2.close();
      clientInfos = jmsServerManager.getClients();
      assertNotNull(clientInfos);
      assertEquals(1, clientInfos.size());
      conn3.close();
      clientInfos = jmsServerManager.getClients();
      assertNotNull(clientInfos);
      assertEquals(0, clientInfos.size());
   }

   public void test() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      Session sess = conn.createSession(true, Session.AUTO_ACKNOWLEDGE);
      sess.createConsumer(queue1);
      jmsServerManager.createQueue("Queue1", "binding");
      jmsServerManager.getConsumerCountForQueue("Queue1");
      sess.createConsumer(queue1);
      sess.createConsumer(queue1);
      sess.createConsumer(queue1);
      sess.createConsumer(queue1);
      sess.createConsumer(queue1);
      assertEquals(jmsServerManager.getConsumerCountForQueue("Queue1"), 6);
      conn.close();
      assertEquals(jmsServerManager.getConsumerCountForQueue("Queue1"), 0);
      conn = getConnectionFactory().createConnection("guest", "guest");
      sess = conn.createSession(true, Session.AUTO_ACKNOWLEDGE);
      sess.createConsumer(topic1);
      conn.close();
   }

   public void testListMessagesForQueue() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      try
      {
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = sess.createProducer(queue1);
         producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
         for (int i = 0; i < 10; i++)
         {
            TextMessage message = sess.createTextMessage();
            message.setIntProperty("count", i);
            producer.send(message);
         }
         producer.setDeliveryMode(DeliveryMode.PERSISTENT);
         for (int i = 10; i < 20; i++)
         {
            TextMessage message = sess.createTextMessage();
            message.setIntProperty("count", i);
            producer.send(message);
         }
         List<Message> messageList = jmsServerManager.listMessagesForQueue("Queue1");
         assertEquals(messageList.size(), 20);
         for (int i = 0; i < messageList.size(); i++)
         {
            Message message = messageList.get(i);
            assertEquals(message.getIntProperty("count"), i);
            assertTrue(message instanceof TextMessage);
         }
         messageList = jmsServerManager.listMessagesForQueue("Queue1", JMSServerManager.ListType.NON_DURABLE);
         assertEquals(messageList.size(), 10);
         for (int i = 0; i < messageList.size(); i++)
         {
            Message message = messageList.get(i);
            assertEquals(message.getIntProperty("count"), i);
            assertTrue(message instanceof TextMessage);
            assertTrue(message.getJMSDeliveryMode() == DeliveryMode.NON_PERSISTENT);
         }
         messageList = jmsServerManager.listMessagesForQueue("Queue1", JMSServerManager.ListType.DURABLE);
         assertEquals(messageList.size(), 10);
         for (int i = 10; i < messageList.size() + 10; i++)
         {
            Message message = messageList.get(i - 10);
            assertEquals(message.getIntProperty("count"), i);
            assertTrue(message instanceof TextMessage);
            assertTrue(message.getJMSDeliveryMode() == DeliveryMode.PERSISTENT);
         }
      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }
   }

   public void testListMessagesForSubscription() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      try
      {
         String cid = "myclientid";
         String id = "mysubid";
         conn.setClientID(cid);
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         TopicSubscriber subscriber = sess.createDurableSubscriber(topic1, id);
         MessageProducer producer = sess.createProducer(topic1);

         producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
         for (int i = 0; i < 10; i++)
         {
            TextMessage message = sess.createTextMessage();
            message.setIntProperty("count", i);
            producer.send(message);
         }
         producer.setDeliveryMode(DeliveryMode.PERSISTENT);
         for (int i = 10; i < 20; i++)
         {
            TextMessage message = sess.createTextMessage();
            message.setIntProperty("count", i);
            producer.send(message);
         }

         assertEquals(20, jmsServerManager.listMessagesForSubscription(cid + "." + id).size());
         assertEquals(10, jmsServerManager.listMessagesForSubscription(cid + "." + id, JMSServerManager.ListType.DURABLE).size());
         assertEquals(10, jmsServerManager.listMessagesForSubscription(cid + "." + id, JMSServerManager.ListType.NON_DURABLE).size());
         subscriber.close();
         sess.unsubscribe(id);
      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }
   }

   public void testRemoveMessageFromQueue() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      try
      {
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = sess.createProducer(queue1);
         Message messageToDelete = null;
         for (int i = 0; i < 10; i++)
         {
            TextMessage message = sess.createTextMessage();
            message.setIntProperty("pos", i);
            producer.send(message);
            if (i == 5)
            {
               messageToDelete = message;
            }
         }
         jmsServerManager.removeMessageFromQueue("Queue1", messageToDelete.getJMSMessageID());
         sess = conn.createSession(true, Session.AUTO_ACKNOWLEDGE);
         MessageConsumer consumer = sess.createConsumer(queue1);
         conn.start();
         int lastPos = -1;
         for (int i = 0; i < 9; i++)
         {
            Message message = consumer.receive();
            assertNotSame(messageToDelete.getJMSMessageID(), message.getJMSMessageID());
            int pos = message.getIntProperty("pos");
            assertTrue("returned in wrong order", pos > lastPos);
            lastPos = pos;
         }
      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }
   }

   public void testRemoveMessageFromTopic() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      try
      {
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = sess.createProducer(topic1);
         MessageConsumer consumer = sess.createConsumer(topic1);
         MessageConsumer consumer2 = sess.createConsumer(topic1);
         Message messageToDelete = null;
         for (int i = 0; i < 10; i++)
         {
            TextMessage message = sess.createTextMessage();
            producer.send(message);
            if (i == 5)
            {
               messageToDelete = message;
            }
         }
         jmsServerManager.removeMessageFromTopic("Topic1", messageToDelete.getJMSMessageID());
         conn.start();
         for (int i = 0; i < 9; i++)
         {
            Message message = consumer.receive();
            assertNotSame(messageToDelete.getJMSMessageID(), message.getJMSMessageID());
            message = consumer2.receive();
            assertNotSame(messageToDelete.getJMSMessageID(), message.getJMSMessageID());
         }
      }
      finally
      {
         if(conn != null)
         {
            conn.close();
         }
      }

   }

   public void testRemoveAllMessagesFromQueue() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      try
      {
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = sess.createProducer(queue1);
         for (int i = 0; i < 10; i++)
         {
            TextMessage message = sess.createTextMessage();
            producer.send(message);
         }
         jmsServerManager.removeAllMessagesForQueue("Queue1");
         sess = conn.createSession(true, Session.AUTO_ACKNOWLEDGE);
         MessageConsumer consumer = sess.createConsumer(queue1);
         assertEquals("messages still exist", 0, jmsServerManager.getMessageCountForQueue("Queue1"));

      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }
   }

   public void testRemoveAllMessagesFromTopic() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      try
      {
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = sess.createProducer(topic1);
         MessageConsumer consumer = sess.createConsumer(topic1);
         MessageConsumer consumer2 = sess.createConsumer(topic1);
         Message messageToDelete = null;
         for (int i = 0; i < 10; i++)
         {
            TextMessage message = sess.createTextMessage();
            producer.send(message);
            if (i == 5)
            {
               messageToDelete = message;
            }
         }
         jmsServerManager.removeAllMessagesForTopic("Topic1");
         List<SubscriptionInfo> subscriptionInfos = jmsServerManager.listSubscriptions("Topic1");
         for (SubscriptionInfo subscriptionInfo : subscriptionInfos)
         {
            assertEquals(0, jmsServerManager.listMessagesForSubscription(subscriptionInfo.getId()).size());
         }
      }
      finally
      {
         if(conn != null)
         {
            conn.close();
         }
      }

   }

   public void testMoveMessage() throws Exception
   {
      Connection conn = getConnectionFactory().createConnection("guest", "guest");
      try
      {
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = sess.createProducer(queue1);
         Message messageToMove = null;
         for (int i = 0; i < 10; i++)
         {
            TextMessage message = sess.createTextMessage();
            producer.send(message);
            if (i == 5)
            {
               messageToMove = message;
            }
         }
         jmsServerManager.moveMessage("Queue1", "Queue2", messageToMove.getJMSMessageID());
         MessageConsumer consumer = sess.createConsumer(queue1);
         conn.start();
         for (int i = 0; i < 9; i++)
         {
            Message message = consumer.receive();
            assertNotSame(messageToMove.getJMSMessageID(), message.getJMSMessageID());
         }
         consumer.close();
         consumer = sess.createConsumer(queue2);
         Message message = consumer.receive();
         assertEquals(messageToMove.getJMSMessageID(), message.getJMSMessageID());
      }
      finally
      {
         if(conn != null)
         {
            conn.close();
         }
      }

   }
}