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
package org.jboss.jms.example;

import java.util.HashSet;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

/**
 * An example showing how messages are moved to an expiry queue when they expire.
 *
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 *
 */
public class ExpiryExample extends JMSExample
{
   public static void main(String[] args)
   {
      new ExpiryExample().run(args);
   }

   public void runExample() throws Exception
   {
      Connection connection = null;
      try
      {
         // Step 1. Create an initial context to perform the JNDI lookup.
         InitialContext initialContext = getContext();

         // Step 2. Perfom a lookup on the queue
         Queue queue = (Queue)initialContext.lookup("/queue/exampleQueue");

         // Step 3. Perform a lookup on the Connection Factory
         ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("/ConnectionFactory");

         // Step 4.Create a JMS Connection
         connection = cf.createConnection();

         // Step 5. Create a JMS Session
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         // Step 6. Create a JMS Message Producer
         MessageProducer producer = session.createProducer(queue);

         // Step 7. Messages sent by this producer will be retained for 1s (1000ms) before expiration
         producer.setTimeToLive(1000);

         // Step 8. Create a Text Message
         TextMessage message = session.createTextMessage("this is a text message");

         // Step 9. Send the Message
         producer.send(message);
         System.out.println("Sent message to " + queue.getQueueName() + ": " + message.getText());

         // Step 10. Sleep for 5s. Once we wake up, the message will have been expired
         System.out.println("Sleep a little bit to let the message expire...");
         Thread.sleep(5000);

         // Step 11. Create a JMS Message Consumer for the queue
         MessageConsumer messageConsumer = session.createConsumer(queue);

         // Step 12. Start the Connection
         connection.start();

         // Step 13. Trying to receive a message. Since there is none on the queue, the call will timeout after 5000ms
         // and messageReceived will be null
         TextMessage messageReceived = (TextMessage)messageConsumer.receive(5000);
         System.out.println("Received message from " + queue.getQueueName() + ": " + messageReceived);

         // Step 15. Perfom a lookup on the expiry queue
         Queue expiryQueue = (Queue)initialContext.lookup("/queue/expiryQueue");

         // Step 16. Create a JMS Message Consumer for the expiry queue
         MessageConsumer expiryConsumer = session.createConsumer(expiryQueue);

         // Step 17. Receive the message from the expiry queue
         messageReceived = (TextMessage)expiryConsumer.receive(5000);

         // Step 18. The message sent to the queue was moved to the expiry queue when it expired.
         System.out.println("Received message from " + expiryQueue.getQueueName() + ": " + messageReceived.getText());

         initialContext.close();
      }
      finally
      {
         // Step 19. Be sure to close our JMS resources!
         if (connection != null)
         {
            connection.close();
         }
      }
   }

   @Override
   public Set<String> getQueues()
   {
      HashSet<String> queues = new HashSet<String>();
      queues.add("exampleQueue");
      queues.add("expiryQueue");
      return queues;
   }

}