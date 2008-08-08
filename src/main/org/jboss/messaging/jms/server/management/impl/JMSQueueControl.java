/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.messaging.jms.server.management.impl;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.management.openmbean.TabularData;

import org.jboss.messaging.core.exception.MessagingException;
import org.jboss.messaging.core.filter.Filter;
import org.jboss.messaging.core.filter.impl.FilterImpl;
import org.jboss.messaging.core.management.impl.MBeanInfoHelper;
import org.jboss.messaging.core.server.MessageReference;
import org.jboss.messaging.core.server.Queue;
import org.jboss.messaging.core.server.ServerMessage;
import org.jboss.messaging.core.settings.impl.QueueSettings;
import org.jboss.messaging.jms.JBossDestination;
import org.jboss.messaging.jms.JBossQueue;
import org.jboss.messaging.jms.client.JBossMessage;
import org.jboss.messaging.jms.client.SelectorTranslator;
import org.jboss.messaging.jms.server.JMSServerManager;
import org.jboss.messaging.jms.server.management.JMSMessageInfo;
import org.jboss.messaging.jms.server.management.JMSQueueControlMBean;
import org.jboss.messaging.util.SimpleString;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * @version <tt>$Revision$</tt>
 * 
 */
public class JMSQueueControl extends StandardMBean implements
      JMSQueueControlMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private final JBossQueue managedQueue;
   private Queue coreQueue;
   private final String binding;
   private JMSServerManager server;

   // Static --------------------------------------------------------

   private static Filter createFilterForJMSMessageID(String jmsMessageID)
         throws Exception
   {
      return new FilterImpl(new SimpleString(JBossMessage.JBM_MESSAGE_ID
            + " = '" + jmsMessageID + "'"));
   }

   // Constructors --------------------------------------------------

   public JMSQueueControl(final JBossQueue queue, final Queue coreQueue,
         final String jndiBinding, final JMSServerManager server)
         throws NotCompliantMBeanException
   {
      super(JMSQueueControlMBean.class);
      this.managedQueue = queue;
      this.coreQueue = coreQueue;
      this.binding = jndiBinding;
      this.server = server;
   }

   // Public --------------------------------------------------------

   // ManagedJMSQueueMBean implementation ---------------------------

   public String getName()
   {
      return managedQueue.getName();
   }

   public String getAddress()
   {
      return managedQueue.getAddress();
   }

   public boolean isTemporary()
   {
      return managedQueue.isTemporary();
   }

   public int getMessageCount()
   {
      return coreQueue.getMessageCount();
   }

   public int getMessagesAdded()
   {
      return coreQueue.getMessagesAdded();
   }

   public int getConsumerCount()
   {
      return coreQueue.getConsumerCount();
   }
   
   public int getDeliveringCount()
   {
      return coreQueue.getDeliveringCount();
   }
   
   public int getMaxSizeBytes()
   {
      return coreQueue.getMaxSizeBytes();
   }
   
   public long getScheduledCount()
   {
      return coreQueue.getScheduledCount();
   }
   
   public long getSizeBytes()
   {
      return coreQueue.getSizeBytes();
   }
   
   public boolean isClustered()
   {
      return coreQueue.isClustered();
   }
   
   public boolean isDurable()
   {
      return coreQueue.isDurable();
   }
   
   public String getJNDIBinding()
   {
      return binding;
   }

   public String getDLQ()
   {
      QueueSettings settings = server.getSettings(managedQueue);
      if (settings != null && settings.getDLQ() != null)
      {
         return JBossDestination.fromAddress(settings.getDLQ().toString())
               .getName();
      } else
      {
         return null;
      }
   }

   public String getExpiryQueue()
   {
      QueueSettings settings = server.getSettings(managedQueue);
      if (settings != null && settings.getExpiryQueue() != null)
      {
         return JBossDestination.fromAddress(
               settings.getExpiryQueue().toString()).getName();
      } else
      {
         return null;
      }
   }

   public boolean removeMessage(final String messageID) throws Exception
   {
      Filter filter = createFilterForJMSMessageID(messageID);
      List<MessageReference> refs = coreQueue.list(filter);
      if (refs.size() != 1)
      {
         throw new IllegalArgumentException(
               "No message found for JMSMessageID: " + messageID);
      }
      return server.removeMessage(refs.get(0).getMessage().getMessageID(),
            managedQueue);
   }

   public void removeAllMessages() throws Exception
   {
      server.removeAllMessages(managedQueue);
   }

   public TabularData listAllMessages() throws Exception
   {
      return listMessages(null);
   }

   public TabularData listMessages(final String filterStr) throws Exception
   {
      try
      {
         Filter filter = filterStr == null ? null : new FilterImpl(
               new SimpleString(SelectorTranslator
                     .convertToJBMFilterString(filterStr)));

         List<MessageReference> messageRefs = coreQueue.list(filter);
         List<JMSMessageInfo> infos = new ArrayList<JMSMessageInfo>(messageRefs
               .size());
         for (MessageReference messageRef : messageRefs)
         {
            ServerMessage message = messageRef.getMessage();
            JMSMessageInfo info = JMSMessageInfo.fromServerMessage(message);
            infos.add(info);
         }
         return JMSMessageInfo.toTabularData(infos);
      } catch (MessagingException e)
      {
         throw new IllegalStateException(e.getMessage());
      }
   }

   public boolean expireMessage(final String messageID) throws Exception
   {
      Filter filter = createFilterForJMSMessageID(messageID);
      List<MessageReference> refs = coreQueue.list(filter);
      if (refs.size() != 1)
      {
         throw new IllegalArgumentException(
               "No message found for JMSMessageID: " + messageID);
      }
      return server.expireMessages(filter, managedQueue) == 1;
   }

   public int expireMessages(final String filterStr) throws Exception
   {
      try
      {
         Filter filter = filterStr == null ? null : new FilterImpl(
               new SimpleString(SelectorTranslator
                     .convertToJBMFilterString(filterStr)));
         return server.expireMessages(filter, managedQueue);
      } catch (MessagingException e)
      {
         throw new IllegalStateException(e.getMessage());
      }
   }

   public boolean sendMessageTDLQ(final String messageID) throws Exception
   {
      Filter filter = createFilterForJMSMessageID(messageID);
      List<MessageReference> refs = coreQueue.list(filter);
      if (refs.size() != 1)
      {
         throw new IllegalArgumentException(
               "No message found for JMSMessageID: " + messageID);
      }
      return server.sendMessagesToDLQ(filter, managedQueue) == 1;
   }

   public boolean changeMessagePriority(final String messageID,
         final int newPriority) throws Exception
   {
      if (newPriority < 0 || newPriority > 9)
      {
         throw new IllegalArgumentException("invalid newPriority value: "
               + newPriority + ". It must be between 0 and 9 (both included)");
      }
      Filter filter = createFilterForJMSMessageID(messageID);
      List<MessageReference> refs = coreQueue.list(filter);
      if (refs.size() != 1)
      {
         throw new IllegalArgumentException(
               "No message found for JMSMessageID: " + messageID);
      }
      return server.changeMessagesPriority(filter, (byte) newPriority,
            managedQueue) == 1;
   }

   // StandardMBean overrides ---------------------------------------

   @Override
   public MBeanInfo getMBeanInfo()
   {
      MBeanInfo info = super.getMBeanInfo();
      return new MBeanInfo(info.getClassName(), info.getDescription(), info
            .getAttributes(), info.getConstructors(), MBeanInfoHelper
            .getMBeanOperationsInfo(JMSQueueControlMBean.class), info
            .getNotifications());
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
