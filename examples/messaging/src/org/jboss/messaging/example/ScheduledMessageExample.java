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
package org.jboss.messaging.example;

import org.jboss.messaging.core.client.ClientConsumer;
import org.jboss.messaging.core.client.ClientMessage;
import org.jboss.messaging.core.client.ClientProducer;
import org.jboss.messaging.core.client.ClientSession;
import org.jboss.messaging.core.client.ClientSessionFactory;
import org.jboss.messaging.core.client.impl.ClientSessionFactoryImpl;
import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.exception.MessagingException;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.jms.client.JBossTextMessage;
import org.jboss.messaging.util.SimpleString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class ScheduledMessageExample
{
   private static final  Logger log = Logger.getLogger(ScheduledMessageExample.class);
   public static void main(final String[] args)
   {
      ClientSession clientSession = null;
      DateFormat df = new SimpleDateFormat("hh:mm:ss");
      try
      {
         ClientSessionFactory sessionFactory =
            new ClientSessionFactoryImpl(new TransportConfiguration("org.jboss.messaging.core.remoting.impl.netty.NettyConnectorFactory"));
         clientSession = sessionFactory.createSession(false, true, true, false);
         SimpleString queue = new SimpleString("queuejms.testQueue");
         ClientProducer clientProducer = clientSession.createProducer(queue);
         ClientMessage message = clientSession.createClientMessage(JBossTextMessage.TYPE, false, 0,
                                                       System.currentTimeMillis(), (byte) 1);
         message.getBody().putString("Hello!");
         Calendar cal = Calendar.getInstance();
         log.info("current time " + df.format(cal.getTime()));
         cal.add(Calendar.SECOND, 5);
         log.info("message scheduled for " + df.format(cal.getTime()));
         clientProducer.send(message, cal.getTimeInMillis());
         ClientConsumer clientConsumer = clientSession.createConsumer(queue);
         clientSession.start();
         ClientMessage msg = clientConsumer.receive(7000);
         log.info("message received at " + df.format(Calendar.getInstance().getTime()));
         msg.acknowledge();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         if (clientSession != null)
         {
            try
            {
               clientSession.close();
            }
            catch (MessagingException ignore)
            {
            }
         }
      }
   }
}
