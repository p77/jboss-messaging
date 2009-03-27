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
package org.jboss.messaging.tests.integration.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.messaging.core.client.ClientConsumer;
import org.jboss.messaging.core.client.ClientMessage;
import org.jboss.messaging.core.client.ClientProducer;
import org.jboss.messaging.core.client.ClientSession;
import org.jboss.messaging.core.client.ClientSessionFactory;
import org.jboss.messaging.core.client.MessageHandler;
import org.jboss.messaging.core.client.impl.ClientConsumerInternal;
import org.jboss.messaging.core.server.MessagingService;
import org.jboss.messaging.tests.util.ServiceTestBase;
import org.jboss.messaging.utils.SimpleString;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 */
public class ClientConsumerWindowSizeTest extends ServiceTestBase
{
   private final SimpleString addressA = new SimpleString("addressA");

   private final SimpleString queueA = new SimpleString("queueA");

   private final int TIMEOUT = 5;

   /*
   * tests send window size. we do this by having 2 receivers on the q. since we roundrobin the consumer for delivery we
   * know if consumer 1 has received n messages then consumer 2 must have also have received n messages or at least up
   * to its window size
   * */
   public void testSendWindowSize() throws Exception
   {
      MessagingService messagingService = createService(false);
      ClientSessionFactory cf = createInVMFactory();
      try
      {
         messagingService.start();
         cf.setBlockOnNonPersistentSend(false);
         ClientSession sendSession = cf.createSession(false, true, true);
         ClientSession receiveSession = cf.createSession(false, true, true);
         sendSession.createQueue(addressA, queueA, false);
         ClientConsumer receivingConsumer = receiveSession.createConsumer(queueA);
         ClientMessage cm = sendSession.createClientMessage(false);
         cm.setDestination(addressA);
         int encodeSize = cm.getEncodeSize();
         int numMessage = 100;
         cf.setConsumerWindowSize(numMessage * encodeSize);
         ClientSession session = cf.createSession(false, true, true);
         ClientProducer cp = sendSession.createProducer(addressA);
         ClientConsumer cc = session.createConsumer(queueA);
         session.start();
         receiveSession.start();
         for (int i = 0; i < numMessage * 4; i++)
         {
            cp.send(sendSession.createClientMessage(false));
         }

         for (int i = 0; i < numMessage * 2; i++)
         {
            ClientMessage m = receivingConsumer.receive(5000);
            assertNotNull(m);
            m.acknowledge();
         }
         receiveSession.close();
         
         for (int i = 0; i < numMessage * 2; i++)
         {
            ClientMessage m = cc.receive(5000);
            assertNotNull(m);
            m.acknowledge();
         }
         

         session.close();
         sendSession.close();
         

         assertEquals(0, getMessageCount(messagingService, queueA.toString()));

      }
      finally
      {
         if (messagingService.isStarted())
         {
            messagingService.stop();
         }
      }
   }

   public void testSlowConsumerBufferingOne() throws Exception
   {
      MessagingService service = createService(false);

      ClientSession sessionB = null;
      ClientSession session = null;

      try
      {
         final int numberOfMessages = 100;

         service.start();

         ClientSessionFactory sf = createInVMFactory();
         sf.setConsumerWindowSize(1);

         session = sf.createSession(false, true, true);

         SimpleString ADDRESS = addressA;

         session.createQueue(ADDRESS, ADDRESS, true);

         sessionB = sf.createSession(false, true, true);
         sessionB.start();

         session.start();

         ClientConsumer consNeverUsed = sessionB.createConsumer(ADDRESS);

         ClientConsumer cons1 = session.createConsumer(ADDRESS);

         ClientProducer prod = session.createProducer(ADDRESS);

         for (int i = 0; i < numberOfMessages; i++)
         {
            prod.send(createTextMessage(session, "Msg" + i));
         }

         for (int i = 0; i < numberOfMessages - 1; i++)
         {
            ClientMessage msg = cons1.receive(1000);
            assertNotNull("expected message at i = " + i, msg);
            msg.acknowledge();
         }

         ClientMessage msg = consNeverUsed.receive(500);
         assertNotNull(msg);
         msg.acknowledge();

         session.close();
         session = null;

         sessionB.close();
         sessionB = null;

         assertEquals(0, getMessageCount(service, ADDRESS.toString()));

      }
      finally
      {
         try
         {
            if (session != null)
               session.close();
            if (sessionB != null)
               sessionB.close();
         }
         catch (Exception ignored)
         {
         }

         if (service.isStarted())
         {
            service.stop();
         }
      }
   }

   public void testSlowConsumerNoBuffer() throws Exception
   {
      internalTestSlowConsumerNoBuffer(false);
   }

   public void testSlowConsumerNoBufferLargeMessages() throws Exception
   {
      internalTestSlowConsumerNoBuffer(true);
   }

   private void internalTestSlowConsumerNoBuffer(boolean largeMessages) throws Exception
   {
      MessagingService service = createService(false);

      ClientSession sessionB = null;
      ClientSession session = null;

      try
      {
         final int numberOfMessages = 100;

         service.start();

         ClientSessionFactory sf = createInVMFactory();
         sf.setConsumerWindowSize(0);

         if (largeMessages)
         {
            sf.setMinLargeMessageSize(100);
         }

         session = sf.createSession(false, true, true);

         SimpleString ADDRESS = addressA;

         session.createQueue(ADDRESS, ADDRESS, true);

         sessionB = sf.createSession(false, true, true);
         sessionB.start();

         session.start();

         ClientConsumerInternal consNeverUsed = (ClientConsumerInternal)sessionB.createConsumer(ADDRESS);

         ClientConsumer cons1 = session.createConsumer(ADDRESS);

         ClientProducer prod = session.createProducer(ADDRESS);

         for (int i = 0; i < numberOfMessages; i++)
         {
            ClientMessage msg = createTextMessage(session, "Msg" + i);

            if (largeMessages)
            {
               msg.getBody().writeBytes(new byte[600]);
            }

            prod.send(msg);
         }

         for (int i = 0; i < numberOfMessages; i++)
         {
            ClientMessage msg = cons1.receive(1000);
            assertNotNull("expected message at i = " + i, msg);
            assertEquals("Msg" + i, getTextMessage(msg));
            msg.acknowledge();
         }

         assertEquals(0, consNeverUsed.getBufferSize());

         session.close();
         session = null;

         sessionB.close();
         sessionB = null;

         assertEquals(0, getMessageCount(service, ADDRESS.toString()));

      }
      finally
      {
         try
         {
            if (session != null)
               session.close();
            if (sessionB != null)
               sessionB.close();
         }
         catch (Exception ignored)
         {
         }

         if (service.isStarted())
         {
            service.stop();
         }
      }
   }

   public void testSlowConsumerNoBuffer2() throws Exception
   {
      internalTestSlowConsumerNoBuffer2(false);
   }

   public void testSlowConsumerNoBuffer2LargeMessages() throws Exception
   {
      internalTestSlowConsumerNoBuffer2(true);
   }

   private void internalTestSlowConsumerNoBuffer2(final boolean largeMessages) throws Exception
   {
      MessagingService service = createService(false);

      ClientSession session1 = null;
      ClientSession session2 = null;

      try
      {
         final int numberOfMessages = 100;

         service.start();

         ClientSessionFactory sf = createInVMFactory();

         sf.setConsumerWindowSize(0);

         if (largeMessages)
         {
            sf.setMinLargeMessageSize(100);
         }

         session1 = sf.createSession(false, true, true);

         session2 = sf.createSession(false, true, true);

         session1.start();

         session2.start();

         SimpleString ADDRESS = new SimpleString("some-queue");

         session1.createQueue(ADDRESS, ADDRESS, true);

         ClientConsumerInternal cons1 = (ClientConsumerInternal)session1.createConsumer(ADDRESS);

         // Note we make sure we send the messages *before* cons2 is created

         ClientProducer prod = session1.createProducer(ADDRESS);

         for (int i = 0; i < numberOfMessages; i++)
         {
            ClientMessage msg = createTextMessage(session1, "Msg" + i);
            if (largeMessages)
            {
               msg.getBody().writeBytes(new byte[600]);
            }
            prod.send(msg);
         }

         ClientConsumerInternal cons2 = (ClientConsumerInternal)session2.createConsumer(ADDRESS);

         for (int i = 0; i < numberOfMessages / 2; i++)
         {
            ClientMessage msg = cons1.receive(1000);
            assertNotNull("expected message at i = " + i, msg);

            String str = getTextMessage(msg);
            assertEquals("Msg" + i, str);

            msg.acknowledge();

            assertEquals("A slow consumer shouldn't buffer anything on the client side!", 0, cons1.getBufferSize());
         }

         for (int i = numberOfMessages / 2; i < numberOfMessages; i++)
         {
            ClientMessage msg = cons2.receive(1000);

            assertNotNull("expected message at i = " + i, msg);

            assertEquals("Msg" + i, msg.getBody().readString());

            msg.acknowledge();

            assertEquals("A slow consumer shouldn't buffer anything on the client side!", 0, cons2.getBufferSize());
         }

         session1.commit(); // just to make sure everything is flushed and no pending packets on the sending buffer, or
         // the getMessageCount would fail
         session2.commit();

         assertEquals(0, getMessageCount(service, ADDRESS.toString()));

         // This should also work the other way around

         cons1.close();

         cons2.close();

         cons1 = (ClientConsumerInternal)session1.createConsumer(ADDRESS);

         // Note we make sure we send the messages *before* cons2 is created

         for (int i = 0; i < numberOfMessages; i++)
         {
            ClientMessage msg = createTextMessage(session1, "Msg" + i);
            if (largeMessages)
            {
               msg.getBody().writeBytes(new byte[600]);
            }
            prod.send(msg);
         }

         cons2 = (ClientConsumerInternal)session2.createConsumer(ADDRESS);

         // Now we receive on cons2 first

         for (int i = 0; i < numberOfMessages / 2; i++)
         {
            ClientMessage msg = cons2.receive(1000);
            assertNotNull("expected message at i = " + i, msg);

            assertEquals("Msg" + i, msg.getBody().readString());

            msg.acknowledge();

            assertEquals("A slow consumer shouldn't buffer anything on the client side!", 0, cons2.getBufferSize());

         }

         for (int i = numberOfMessages / 2; i < numberOfMessages; i++)
         {
            ClientMessage msg = cons1.receive(1000);

            assertNotNull("expected message at i = " + i, msg);

            assertEquals("Msg" + i, msg.getBody().readString());

            msg.acknowledge();

            assertEquals("A slow consumer shouldn't buffer anything on the client side!", 0, cons1.getBufferSize());
         }

         session1.commit(); // just to make sure everything is flushed and no pending packets on the sending buffer, or
         // the getMessageCount would fail
         session2.commit();

         assertEquals(0, getMessageCount(service, ADDRESS.toString()));

      }
      finally
      {
         try
         {
            if (session1 != null)
               session1.close();
            if (session2 != null)
               session2.close();
         }
         catch (Exception ignored)
         {
         }

         if (service.isStarted())
         {
            service.stop();
         }
      }
   }

   public void testSlowConsumerOnMessageHandlerNoBuffers() throws Exception
   {
      MessagingService service = createService(false);

      ClientSession sessionB = null;
      ClientSession session = null;

      try
      {
         final int numberOfMessages = 100;

         service.start();

         ClientSessionFactory sf = createInVMFactory();
         sf.setConsumerWindowSize(0);

         session = sf.createSession(false, true, true);

         SimpleString ADDRESS = new SimpleString("some-queue");

         session.createQueue(ADDRESS, ADDRESS, true);

         sessionB = sf.createSession(false, true, true);
         sessionB.start();

         session.start();

         ClientConsumerInternal consReceiveOneAndHold = (ClientConsumerInternal)sessionB.createConsumer(ADDRESS);

         final CountDownLatch latchReceived = new CountDownLatch(2);

         final CountDownLatch latchDone = new CountDownLatch(1);

         // It should receive two messages and then give up
         class LocalHandler implements MessageHandler
         {
            boolean failed = false;

            int count = 0;

            /* (non-Javadoc)
             * @see org.jboss.messaging.core.client.MessageHandler#onMessage(org.jboss.messaging.core.client.ClientMessage)
             */
            public synchronized void onMessage(ClientMessage message)
            {
               try
               {
                  String str = getTextMessage(message);

                  failed = failed || !str.equals("Msg" + count);

                  message.acknowledge();
                  latchReceived.countDown();

                  if (count++ == 1)
                  {
                     // it will hold here for a while
                     if (!latchDone.await(TIMEOUT, TimeUnit.SECONDS)) // a timed wait, so if the test fails, one less
                                                                      // thread around
                     {
                        new Exception("ClientConsuemrWindowSizeTest Handler couldn't receive signal in less than 5 seconds").printStackTrace(); // hudson
                                                                                                                                                // or
                                                                                                                                                // junit
                                                                                                                                                // report
                        failed = true;
                     }
                  }
               }
               catch (Exception e)
               {
                  e.printStackTrace(); // Hudson / JUnit report
                  failed = true;
               }
            }
         }

         LocalHandler handler = new LocalHandler();

         ClientConsumer cons1 = session.createConsumer(ADDRESS);

         ClientProducer prod = session.createProducer(ADDRESS);

         for (int i = 0; i < numberOfMessages; i++)
         {
            prod.send(createTextMessage(session, "Msg" + i));
         }

         consReceiveOneAndHold.setMessageHandler(handler);

         assertTrue(latchReceived.await(TIMEOUT, TimeUnit.SECONDS));

         assertEquals(0, consReceiveOneAndHold.getBufferSize());

         for (int i = 2; i < numberOfMessages; i++)
         {
            ClientMessage msg = cons1.receive(1000);
            assertNotNull("expected message at i = " + i, msg);
            assertEquals("Msg" + i, getTextMessage(msg));
            msg.acknowledge();
         }

         assertEquals(0, consReceiveOneAndHold.getBufferSize());

         latchDone.countDown();

         session.close();
         session = null;

         sessionB.close();
         sessionB = null;

         assertEquals(0, getMessageCount(service, ADDRESS.toString()));

         assertFalse("MessageHandler received a failure", handler.failed);

      }
      finally
      {
         try
         {
            if (session != null)
               session.close();
            if (sessionB != null)
               sessionB.close();
         }
         catch (Exception ignored)
         {
         }

         if (service.isStarted())
         {
            service.stop();
         }
      }
   }

   public void testSlowConsumerOnMessageHandlerBufferOne() throws Exception
   {
      internalTestSlowConsumerOnMessageHandlerBufferOne(false);
   }

   public void testSlowConsumerOnMessageHandlerBufferOneLargeMessages() throws Exception
   {
      internalTestSlowConsumerOnMessageHandlerBufferOne(true);
   }
   
   
   private void internalTestSlowConsumerOnMessageHandlerBufferOne(boolean largeMessage) throws Exception
   {
      MessagingService service = createService(false);

      ClientSession sessionB = null;
      ClientSession session = null;

      try
      {
         final int numberOfMessages = 100;

         service.start();

         ClientSessionFactory sf = createInVMFactory();
         sf.setConsumerWindowSize(1);

         if (largeMessage)
         {
            sf.setMinLargeMessageSize(100);
         }

         session = sf.createSession(false, true, true);

         SimpleString ADDRESS = new SimpleString("some-queue");

         session.createQueue(ADDRESS, ADDRESS, true);

         sessionB = sf.createSession(false, true, true);
         sessionB.start();

         session.start();

         ClientConsumerInternal consReceiveOneAndHold = (ClientConsumerInternal)sessionB.createConsumer(ADDRESS);

         final CountDownLatch latchReceived = new CountDownLatch(2);
         final CountDownLatch latchReceivedBuffered = new CountDownLatch(3);

         final CountDownLatch latchDone = new CountDownLatch(1);

         // It should receive two messages and then give up
         class LocalHandler implements MessageHandler
         {
            boolean failed = false;

            int count = 0;

            /* (non-Javadoc)
             * @see org.jboss.messaging.core.client.MessageHandler#onMessage(org.jboss.messaging.core.client.ClientMessage)
             */
            public synchronized void onMessage(ClientMessage message)
            {
               try
               {
                  String str = getTextMessage(message);

                  System.out.println("Received " + str);

                  failed = failed || !str.equals("Msg" + count);

                  message.acknowledge();
                  latchReceived.countDown();
                  latchReceivedBuffered.countDown();

                  if (count++ == 1)
                  {
                     // it will hold here for a while
                     if (!latchDone.await(TIMEOUT, TimeUnit.SECONDS))
                     {
                        new Exception("ClientConsuemrWindowSizeTest Handler couldn't receive signal in less than 5 seconds").printStackTrace(); // hudson
                                                                                                                                                // or
                                                                                                                                                // junit
                                                                                                                                                // report
                        failed = true;
                     }
                  }
               }
               catch (Exception e)
               {
                  e.printStackTrace(); // Hudson / JUnit report
                  failed = true;
               }
            }
         }

         LocalHandler handler = new LocalHandler();

         ClientProducer prod = session.createProducer(ADDRESS);

         for (int i = 0; i < numberOfMessages; i++)
         {
            ClientMessage msg = createTextMessage(session, "Msg" + i);
            if (largeMessage)
            {
               msg.getBody().writeBytes(new byte[600]);
            }
            prod.send(msg);
         }

         consReceiveOneAndHold.setMessageHandler(handler);

         assertTrue(latchReceived.await(TIMEOUT, TimeUnit.SECONDS));

         ClientConsumer cons1 = session.createConsumer(ADDRESS);

         for (int i = 3; i < numberOfMessages; i++)
         {
            ClientMessage msg = cons1.receive(1000);
            assertNotNull("expected message at i = " + i, msg);
            assertEquals("Msg" + i, getTextMessage(msg));
            msg.acknowledge();
         }

         latchDone.countDown();
         
         assertTrue(latchReceivedBuffered.await(TIMEOUT, TimeUnit.SECONDS));

         session.close();
         session = null;

         sessionB.close();
         sessionB = null;

         assertEquals(0, getMessageCount(service, ADDRESS.toString()));

         assertFalse("MessageHandler received a failure", handler.failed);

      }
      finally
      {
         try
         {
            if (session != null)
               session.close();
            if (sessionB != null)
               sessionB.close();
         }
         catch (Exception ignored)
         {
         }

         if (service.isStarted())
         {
            service.stop();
         }
      }
   }
   
   public void testNoWindowRoundRobin() throws Exception
   {
      testNoWindowRoundRobin(false);
   }
   
   
   public void testNoWindowRoundRobinLargeMessage() throws Exception
   {
      testNoWindowRoundRobin(true);
   }
   
   private void testNoWindowRoundRobin(final boolean largeMessages) throws Exception
   {
      
      MessagingService service = createService(false);

      ClientSession sessionA = null;
      ClientSession sessionB = null;

      try
      {
         final int numberOfMessages = 100;

         service.start();

         ClientSessionFactory sf = createInVMFactory();
         sf.setConsumerWindowSize(-1);
         
         if (largeMessages)
         {
            sf.setMinLargeMessageSize(100);
         }

         sessionA = sf.createSession(false, true, true);

         SimpleString ADDRESS = new SimpleString("some-queue");

         sessionA.createQueue(ADDRESS, ADDRESS, true);

         sessionB = sf.createSession(false, true, true);

         sessionA.start();
         sessionB.start();

         ClientConsumerInternal consA = (ClientConsumerInternal)sessionA.createConsumer(ADDRESS);

         ClientConsumerInternal consB = (ClientConsumerInternal)sessionB.createConsumer(ADDRESS);

         ClientProducer prod = sessionA.createProducer(ADDRESS);

         for (int i = 0; i < numberOfMessages; i++)
         {
            ClientMessage msg = createTextMessage(sessionA, "Msg" + i);
            if (largeMessages)
            {
               msg.getBody().writeBytes(new byte[600]);
            }
            prod.send(msg);
         }
         
         
         long timeout = System.currentTimeMillis() + TIMEOUT * 1000;
         
         boolean foundA = false;
         boolean foundB = false;
         
         do
         {
            foundA = consA.getBufferSize() == numberOfMessages / 2;
            foundB = consB.getBufferSize() == numberOfMessages / 2;
            
            Thread.sleep(10);
         } while ((!foundA || !foundB) && System.currentTimeMillis() < timeout);
         
         
         assertTrue("ConsumerA didn't receive the expected number of messages on buffer (consA=" + consA.getBufferSize() + ", consB=" + consB.getBufferSize() + ") foundA = " + foundA + " foundB = " + foundB, foundA);
         assertTrue("ConsumerB didn't receive the expected number of messages on buffer (consA=" + consA.getBufferSize() + ", consB=" + consB.getBufferSize() + ") foundA = " + foundA + " foundB = " + foundB, foundB);
         

      }
      finally
      {
         try
         {
            if (sessionA != null)
               sessionA.close();
            if (sessionB != null)
               sessionB.close();
         }
         catch (Exception ignored)
         {
         }

         if (service.isStarted())
         {
            service.stop();
         }
      }
   }

}
