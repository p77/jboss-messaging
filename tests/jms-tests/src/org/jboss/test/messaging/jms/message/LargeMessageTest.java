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
package org.jboss.test.messaging.jms.message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.jboss.messaging.jms.client.JBossMessage;
import org.jboss.messaging.tests.util.UnitTestCase;
import org.jboss.test.messaging.jms.JMSTestCase;

/**
 *
 * @author <a href="mailto:clebert.suconic@feodorov.com">Clebert Suconic</a>
 * @version <tt>$Revision: 6220 $</tt>
 *
 * $Id: MessageHeaderTest.java 6220 2009-03-30 19:38:11Z timfox $
 */
public class LargeMessageTest extends JMSTestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testSimpleLargeMessage() throws Exception
   {
      Connection conn = null;

      try
      {
         conn = cf.createConnection();

         Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         MessageProducer prod = session.createProducer(queue1);
         prod.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

         BytesMessage m = session.createBytesMessage();

         ((JBossMessage)m).setInputStream(UnitTestCase.createFakeLargeStream(1024 * 1024));

         prod.send(m);

         conn.close();

         conn = cf.createConnection();

         session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         MessageConsumer cons = session.createConsumer(queue1);

         conn.start();

         BytesMessage rm = (BytesMessage)cons.receive(10000);

         byte data[] = new byte[1024];

         System.out.println("Message = " + rm);

         for (int i = 0; i < 1024 * 1024; i += 1024)
         {
            System.out.println("Read message chunk " + i);
            int numberOfBytes = rm.readBytes(data);
            assertEquals(1024, numberOfBytes);
            for (int j = 0; j < 1024; j++)
            {
               assertEquals(UnitTestCase.getSamplebyte(i + j), data[j]);
            }
         }

         assertNotNull(rm);

      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }

   }

   public void testSimpleLargeMessage2() throws Exception
   {
      Connection conn = null;

      try
      {
         conn = cf.createConnection();

         Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         MessageProducer prod = session.createProducer(queue1);
         prod.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

         BytesMessage m = session.createBytesMessage();

         ((JBossMessage)m).setInputStream(UnitTestCase.createFakeLargeStream(10));

         prod.send(m);

         conn.close();

         conn = cf.createConnection();

         session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         MessageConsumer cons = session.createConsumer(queue1);

         conn.start();

         BytesMessage rm = (BytesMessage)cons.receive(10000);

         byte data[] = new byte[1024];

         System.out.println("Message = " + rm);

         int numberOfBytes = rm.readBytes(data);
         assertEquals(10, numberOfBytes);
         for (int j = 0; j < numberOfBytes; j++)
         {
            assertEquals(UnitTestCase.getSamplebyte(j), data[j]);
         }

         assertNotNull(rm);

      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }

   }


   public void testWaitOnOutputStream() throws Exception
   {
      int msgSize = 1024 * 1024;
      
      Connection conn = null;

      try
      {
         conn = cf.createConnection();

         Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         MessageProducer prod = session.createProducer(queue1);
         prod.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

         BytesMessage m = session.createBytesMessage();

         ((JBossMessage)m).setInputStream(UnitTestCase.createFakeLargeStream(msgSize));

         prod.send(m);

         conn.close();

         conn = cf.createConnection();

         session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         MessageConsumer cons = session.createConsumer(queue1);

         conn.start();

         BytesMessage rm = (BytesMessage)cons.receive(10000);
         assertNotNull(rm);

         final AtomicLong numberOfBytes = new AtomicLong(0);
         
         final AtomicInteger numberOfErrors = new AtomicInteger(0);

         OutputStream out = new OutputStream()
         {

            int position = 0;
            @Override
            public void write(int b) throws IOException
            {
               numberOfBytes.incrementAndGet();
               if (UnitTestCase.getSamplebyte(position++) != b)
               {
                  System.out.println("Wrong byte at position " + position);
                  numberOfErrors.incrementAndGet();
               }
            }
            
         };

         
         ((JBossMessage)rm).setOutputStream(out);
         
         assertTrue(((JBossMessage)rm).waitCompletionOnStream(10000));
         
         assertEquals(msgSize, numberOfBytes.get());
         
         assertEquals(0, numberOfErrors.get());
         

      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }

   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   class ThreadReader extends Thread
   {
      CountDownLatch latch;

      ThreadReader(CountDownLatch latch)
      {
         this.latch = latch;
      }

      public void run()
      {
      }
   }

}
