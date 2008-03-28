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
package org.jboss.messaging.core.persistence.impl.nullpm;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.messaging.core.message.Message;
import org.jboss.messaging.core.message.MessageReference;
import org.jboss.messaging.core.persistence.StorageManager;
import org.jboss.messaging.core.postoffice.Binding;
import org.jboss.messaging.core.postoffice.PostOffice;
import org.jboss.messaging.core.server.Queue;
import org.jboss.messaging.core.server.QueueFactory;

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
	private final AtomicLong messageIDSequence = new AtomicLong(0);
	
	private final AtomicLong transactionIDSequence = new AtomicLong(0);
	
	private volatile boolean started;
	
	public void addBinding(Binding binding) throws Exception
	{
	}

	public boolean addDestination(String destination) throws Exception
	{
		return true;
	}

	public void commit(long txID) throws Exception
	{
	}

	public void deleteBinding(Binding binding) throws Exception
	{
	}

	public boolean deleteDestination(String destination) throws Exception
	{
		return true;
	}

	public void loadBindings(QueueFactory queueFactory, List<Binding> bindings,
			List<String> destinations) throws Exception
	{
	}

	public void loadMessages(PostOffice postOffice, Map<Long, Queue> queues)
			throws Exception
	{
	}

	public void prepare(long txID) throws Exception
	{
	}

	public void rollback(long txID) throws Exception
	{
	}

	public void storeAcknowledge(long queueID, long messageID) throws Exception
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

	public void storeMessage(String address, Message message) throws Exception
	{
	}

	public void storeMessageTransactional(long txID, String address,
			Message message) throws Exception
	{
	}

	public void updateDeliveryCount(MessageReference ref) throws Exception
	{
	}

	public long generateMessageID()
	{
		return messageIDSequence.getAndIncrement();
	}
	
	public long generateTransactionID()
	{
		return transactionIDSequence.getAndIncrement();
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
  
}