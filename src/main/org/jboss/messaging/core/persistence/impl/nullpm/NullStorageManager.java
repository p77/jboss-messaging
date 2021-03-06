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

package org.jboss.messaging.core.persistence.impl.nullpm;

import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.paging.LastPageRecord;
import org.jboss.messaging.core.paging.PageTransactionInfo;
import org.jboss.messaging.core.persistence.StorageManager;
import org.jboss.messaging.core.postoffice.Binding;
import org.jboss.messaging.core.postoffice.PostOffice;
import org.jboss.messaging.core.server.MessageReference;
import org.jboss.messaging.core.server.Queue;
import org.jboss.messaging.core.server.QueueFactory;
import org.jboss.messaging.core.server.ServerMessage;
import org.jboss.messaging.core.transaction.ResourceManager;
import org.jboss.messaging.util.IDGenerator;
import org.jboss.messaging.util.SimpleString;
import org.jboss.messaging.util.TimeAndCounterIDGenerator;

import javax.transaction.xa.Xid;
import java.util.List;
import java.util.Map;

/**
 * 
 * A NullStorageManager
 * 
 * @author <a href="mailto:ataylor@redhat.com">Andy Taylor</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class NullStorageManager implements StorageManager
{
   private static final Logger log = Logger.getLogger(NullStorageManager.class);

   
   //FIXME - these need to use id generators from 1.4 null storage manager since is not unique across
   //cluster
	private final IDGenerator idGenerator = new TimeAndCounterIDGenerator();

	private volatile boolean started;
	
	public void addBinding(Binding binding) throws Exception
	{
	}

	public boolean addDestination(SimpleString destination) throws Exception
	{
		return true;
	}

	public void commit(long txID) throws Exception
	{
	}

	public void deleteBinding(Binding binding) throws Exception
	{
	}

	public boolean deleteDestination(SimpleString destination) throws Exception
	{
		return true;
	}

	public void loadBindings(QueueFactory queueFactory, List<Binding> bindings,
			List<SimpleString> destinations) throws Exception
	{
	}

	public void loadMessages(PostOffice postOffice, Map<Long, Queue> queues, ResourceManager resourceManager)
			throws Exception
	{
	}

	public void prepare(long txID, Xid xid) throws Exception
	{
	}

	public void rollback(long txID) throws Exception
	{
	}

	public void storeAcknowledge(long queueID, long messageID) throws Exception
	{
	}

   public void storeMessageReferenceScheduled(long queueID, long messageID, long scheduledDeliveryTime) throws Exception
   {
   }

   public void storeAcknowledgeTransactional(long txID, long queueID,
			long messageiD) throws Exception
	{
	}


   public void storeDelete(long messageID) throws Exception
	{
	}

	public void storeDeleteTransactional(long txID, long messageID)
			throws Exception
	{
	}

	public void storeMessage(ServerMessage message) throws Exception
	{
	}

	public void storeMessageTransactional(long txID, ServerMessage message) throws Exception
	{
	}

   public void storeMessageReferenceScheduledTransactional(long txID, long queueID, long messageID, long scheduledDeliveryTime) throws Exception
   {
   }

   public void storePageTransaction(long txID, PageTransactionInfo pageTransaction) throws Exception
   {
   }

   public void updatePageTransaction(long txID, PageTransactionInfo pageTransaction) throws Exception
   {
   }

   public void updateDeliveryCount(MessageReference ref) throws Exception
	{
	}

	public long generateUniqueID()
	{
	   //FIXME - this needs to use Howard's ID generator from JBM 1.4
		return idGenerator.generateID();
	}
	
	public synchronized void start() throws Exception
	{
		if (started)
		{
			throw new IllegalStateException("Already started");
		}
			
		started = true;
	}

	public synchronized void stop() throws Exception
	{
		if (!started)
		{
			throw new IllegalStateException("Not started");
		}
		
		started = false;
	}
	
	public synchronized boolean isStarted()
	{
	   return started;
	}

   public void storeLastPage(long txID, LastPageRecord pageTransaction) throws Exception
   {
   }

   public void storeDeleteMessageTransactional(long txID, long messageID,
         long queueID) throws Exception
   {
   }


}
