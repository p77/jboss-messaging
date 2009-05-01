/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.messaging.ra;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;

import org.jboss.messaging.core.logging.Logger;

/**
 * JBMQueueSender.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class JBMQueueSender extends JBMMessageProducer implements QueueSender
{
   /** The logger */
   private static final Logger log = Logger.getLogger(JBMQueueSender.class);

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /**
    * Create a new wrapper
    * @param producer the producer
    * @param session the session
    */
   public JBMQueueSender(final QueueSender producer, final JBMSession session)
   {
      super(producer, session);

      if (trace)
      {
         log.trace("constructor(" + producer + ", " + session + ")");
      }
   }

   /**
    * Get queue
    * @return The queue
    * @exception JMSException Thrown if an error occurs
    */
   public Queue getQueue() throws JMSException
   {
      if (trace)
      {
         log.trace("getQueue()");
      }

      return ((QueueSender)producer).getQueue();
   }

   /**
    * Send message
    * @param destination The destination
    * @param message The message
    * @param deliveryMode The delivery mode
    * @param priority The priority
    * @param timeToLive The time to live
    * @exception JMSException Thrown if an error occurs
    */
   public void send(final Queue destination,
                    final Message message,
                    final int deliveryMode,
                    final int priority,
                    final long timeToLive) throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
         {
            log.trace("send " + this +
                      " destination=" +
                      destination +
                      " message=" +
                      message +
                      " deliveryMode=" +
                      deliveryMode +
                      " priority=" +
                      priority +
                      " ttl=" +
                      timeToLive);
         }

         checkState();
         producer.send(destination, message, deliveryMode, priority, timeToLive);

         if (trace)
         {
            log.trace("sent " + this + " result=" + message);
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Send message
    * @param destination The destination
    * @param message The message
    * @exception JMSException Thrown if an error occurs
    */
   public void send(final Queue destination, final Message message) throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
         {
            log.trace("send " + this + " destination=" + destination + " message=" + message);
         }

         checkState();
         producer.send(destination, message);

         if (trace)
         {
            log.trace("sent " + this + " result=" + message);
         }
      }
      finally
      {
         session.unlock();
      }
   }
}
