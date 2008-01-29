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
package org.jboss.messaging.core.impl;

import org.jboss.messaging.core.Delivery;
import org.jboss.messaging.core.MessageReference;
import org.jboss.messaging.core.remoting.PacketSender;
import org.jboss.messaging.core.remoting.wireformat.DeliverMessage;
import org.jboss.messaging.util.Logger;

/**
 * 
 * A DeliveryImpl
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class DeliveryImpl implements Delivery
{
   private static final Logger log = Logger.getLogger(DeliveryImpl.class);
   
   private MessageReference reference;
   
   private String consumerID;
   
   private long deliveryID;
   
   private PacketSender sender;

   public DeliveryImpl(MessageReference reference, String consumerID,
                   long deliveryID, PacketSender sender)
   {      
      this.reference = reference;
      this.consumerID = consumerID;
      this.deliveryID = deliveryID;
      this.sender = sender;
   }

   public MessageReference getReference()
   {
      return reference;
   }

   public long getDeliveryID()
   {
      return deliveryID;
   }
   
   public void deliver()
   {
      DeliverMessage message = new DeliverMessage(reference.getMessage(),
                                                  deliveryID,
                                                  reference.getDeliveryCount() + 1);
      
      message.setTargetID(consumerID);
      
      sender.send(message);
   }
}