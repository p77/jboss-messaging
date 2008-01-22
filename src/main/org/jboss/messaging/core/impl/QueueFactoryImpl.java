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

import org.jboss.messaging.core.Filter;
import org.jboss.messaging.core.Queue;
import org.jboss.messaging.core.QueueFactory;
import org.jboss.messaging.core.QueueSettings;
import org.jboss.messaging.util.HierarchicalObjectRepository;
import org.jboss.messaging.util.HierarchicalRepository;

/**
 * 
 * A QueueFactoryImpl
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class QueueFactoryImpl implements QueueFactory
{
   HierarchicalRepository<QueueSettings> queueSettingsRepository;


   public QueueFactoryImpl()
   {
      queueSettingsRepository = new HierarchicalObjectRepository<QueueSettings>();
      queueSettingsRepository.setDefault(new QueueSettings());
   }

   public Queue createQueue(long id, String name, Filter filter,
                            boolean durable, boolean temporary)
   {
      QueueSettings queueSettings = queueSettingsRepository.getMatch(name);

      Queue queue =  new QueueImpl(id, name, filter, queueSettings.isClustered(), durable, temporary, queueSettings.getMaxSize());
      
      queue.setMaxDeliveryAttempts(queueSettings.getMaxDeliveryAttempts());
      queue.setMessageCounterHistoryDayLimit(queueSettings.getMessageCounterHistoryDayLimit());
      queue.setRedeliveryDelay(queueSettings.getRedeliveryDelay());                           
      queue.setDistributionPolicy(queueSettings.getDistributionPolicy());

      return queue;

   }

   public void setQueueSettingsRepository(HierarchicalRepository<QueueSettings> queueSettingsRepository)
   {
      this.queueSettingsRepository = queueSettingsRepository;
   }
}
