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

package org.jboss.messaging.core.client;

import org.jboss.messaging.core.exception.MessagingException;
import org.jboss.messaging.util.SimpleString;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:ataylor@redhat.com">Andy Taylor</a>
 */
public interface ClientProducer
{        
	SimpleString getAddress();
	
	void send(ClientMessage message) throws MessagingException;
	
   void send(SimpleString address, ClientMessage message) throws MessagingException;
   
   void send(final ClientMessage msg, long scheduleDeliveryTime) throws MessagingException;

   void send(final SimpleString address, final ClientMessage msg, long scheduleDeliveryTime) throws MessagingException;

   void sendManagement(ClientMessage mngmntMessage) throws MessagingException;

   void registerAcknowledgementHandler(AcknowledgementHandler handler);
   
   void unregisterAcknowledgementHandler(AcknowledgementHandler handler);
      
   void close() throws MessagingException;
   
   boolean isClosed();   
   
   boolean isBlockOnPersistentSend();
   
   boolean isBlockOnNonPersistentSend();
   
   int getMaxRate();
   
   int getInitialWindowSize();

}
