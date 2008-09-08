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

package org.jboss.messaging.core.remoting.impl.wireformat;

import org.jboss.messaging.core.remoting.spi.MessagingBuffer;


/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * @version <tt>$Revision$</tt>
 */
public class SessionReplicateDeliveryMessage extends PacketImpl
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   
   private long messageID;
   
   private int consumerID;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionReplicateDeliveryMessage(final long messageID, final int consumerID)
   {
      super(SESS_REPLICATE_DELIVERY);
      
      this.messageID = messageID;
      
      this.consumerID = consumerID;
   }
   
   public SessionReplicateDeliveryMessage()
   {
      super(SESS_REPLICATE_DELIVERY);
   }

   // Public --------------------------------------------------------
   
   public long getMessageID()
   {
      return messageID;
   }
   
   public int getConsumerID()
   {
      return consumerID;
   }
   
   public void encodeBody(final MessagingBuffer buffer)
   {
      buffer.putLong(messageID);
      buffer.putInt(consumerID);
   }
   
   public void decodeBody(final MessagingBuffer buffer)
   {
      messageID = buffer.getLong();
      consumerID = buffer.getInt();
   }
   
   public boolean isUsesConfirmations()
   {
      return false;
   }

   @Override
   public String toString()
   {
      return getParentString() + ", messageID=" + messageID + ", consumerID=" + consumerID + "]";
   }
   
   public boolean equals(Object other)
   {
      if (other instanceof SessionReplicateDeliveryMessage == false)
      {
         return false;
      }
            
      SessionReplicateDeliveryMessage r = (SessionReplicateDeliveryMessage)other;
      
      return super.equals(other) && this.messageID == r.messageID && this.consumerID == r.consumerID;
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
