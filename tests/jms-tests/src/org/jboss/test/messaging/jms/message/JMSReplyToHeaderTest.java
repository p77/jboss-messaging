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

import javax.jms.Message;
import javax.jms.TemporaryQueue;

/**
 *
 * A JMSReplyToHeaderTest

 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 *
 */
public class JMSReplyToHeaderTest extends MessageHeaderTestBase
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------

   public JMSReplyToHeaderTest(String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------


   public void testJMSDestinationSimple() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      TemporaryQueue tempQ = queueProducerSession.createTemporaryQueue();
      m.setJMSReplyTo(tempQ);

      queueProducer.send(m);
      queueConsumer.receive();
      assertEquals(tempQ, m.getJMSReplyTo());
   }

   public void testJMSDestinationNull() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      m.setJMSReplyTo(null);

      queueProducer.send(m);
      queueConsumer.receive();
      assertNull(m.getJMSReplyTo());
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
