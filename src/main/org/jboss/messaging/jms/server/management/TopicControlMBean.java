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

package org.jboss.messaging.jms.server.management;

import javax.management.openmbean.TabularData;

import org.jboss.messaging.core.management.Operation;
import org.jboss.messaging.core.management.Parameter;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * @version <tt>$Revision$</tt>
 * 
 */
public interface TopicControlMBean extends DestinationControlMBean
{
   // Attributes ----------------------------------------------------

   int getSubcriptionsCount();

   int getDurableSubcriptionsCount();

   int getNonDurableSubcriptionsCount();

   int getDurableMessagesCount();

   int getNonDurableMessagesCount();

   // Operations ----------------------------------------------------

   // Open Type API

   @Operation(desc = "List all subscriptions")
   TabularData listAllSubscriptions();

   @Operation(desc = "List only the durable subscriptions")
   TabularData listDurableSubscriptions();

   @Operation(desc = "List only the non durable subscriptions")
   TabularData listNonDurableSubscriptions();

   @Operation(desc = "List all the message for the given subscription")
   public TabularData listMessagesForSubscription(
         @Parameter(name = "queueName", desc = "the name of the queue representing a subscription") String queueName)
         throws Exception;

   // Specific API

   @Operation(desc = "List all subscriptions")
   SubscriptionInfo[] listAllSubscriptionInfos();

   @Operation(desc = "List only the durable subscriptions")
   SubscriptionInfo[] listDurableSubscriptionInfos();

   @Operation(desc = "List only the non durable subscriptions")
   SubscriptionInfo[] listNonDurableSubscriptionInfos();

}
