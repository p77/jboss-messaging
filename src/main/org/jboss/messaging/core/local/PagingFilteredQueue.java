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
package org.jboss.messaging.core.local;

import org.jboss.logging.Logger;
import org.jboss.messaging.core.Delivery;
import org.jboss.messaging.core.DeliveryObserver;
import org.jboss.messaging.core.Filter;
import org.jboss.messaging.core.PagingChannelSupport;
import org.jboss.messaging.core.Queue;
import org.jboss.messaging.core.SimpleDelivery;
import org.jboss.messaging.core.message.MessageReference;
import org.jboss.messaging.core.plugin.contract.MessageStore;
import org.jboss.messaging.core.plugin.contract.PersistenceManager;
import org.jboss.messaging.core.tx.Transaction;

import EDU.oswego.cs.dl.util.concurrent.QueuedExecutor;

/**
 * 
 * A PagingFilteredQueue
 * 
 * Can be used to implement a point to point queue, or a subscription fed from a topic
 *
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @version <tt>$Revision: 1295 $</tt>
 *
 * $Id: Queue.java 1295 2006-09-15 17:44:02Z timfox $
 *
 */
public class PagingFilteredQueue extends PagingChannelSupport implements Queue
{
   // Constants -----------------------------------------------------
   
   private static final Logger log;
   
   private static final boolean trace;
   
   static
   {
      log = Logger.getLogger(PagingFilteredQueue.class);
      trace = log.isTraceEnabled();
   }

   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected String name;
   
   protected Filter filter;
   
   // Constructors --------------------------------------------------

   public PagingFilteredQueue(String name, long id, MessageStore ms, PersistenceManager pm,             
                              boolean acceptReliableMessages, boolean recoverable,
                              QueuedExecutor executor, int maxSize,
                              Filter filter)
   {
      super(id, ms, pm, acceptReliableMessages, recoverable, executor, maxSize);
      
      router = new RoundRobinPointToPointRouter();
      
      this.name = name;
      
      this.filter = filter;
   }
   
   public PagingFilteredQueue(String name, long id, MessageStore ms, PersistenceManager pm,             
                              boolean acceptReliableMessages, boolean recoverable,
                              QueuedExecutor executor, int maxSize,
                              Filter filter,
                              int fullSize, int pageSize, int downCacheSize)
   {
      super(id, ms, pm, acceptReliableMessages, recoverable, executor, maxSize, fullSize, pageSize, downCacheSize);
      
      router = new RoundRobinPointToPointRouter();
      
      this.name = name;
      
      this.filter = filter;
   }
   
   // Queue implementation
   // ---------------------------------------------------------------
   
   public boolean isClustered()
   {
      return false;
   }
   
   public String getName()
   {
      return name;
   }
      
   public Filter getFilter()
   {
      return filter;
   }
    
   // Channel implementation ----------------------------------------   
   
   public Delivery handle(DeliveryObserver sender, MessageReference ref, Transaction tx)
   {
      if (filter == null || filter.accept(ref.getMessage()))
      {
         return super.handle(sender, ref, tx);
      }
      else
      {
         Delivery del = new SimpleDelivery(this, ref, true, false);
         
         return del;
      }
   }
   
   // Public --------------------------------------------------------
   
   public String toString()
   {
      return "Queue[" + getChannelID() + "/" + this.getName() +  "]";
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------   
}
