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

import org.jboss.messaging.core.client.impl.ClientSessionInternal;
import org.jboss.messaging.core.exception.MessagingException;

/**
 * A ConnectionManager
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * Created 27 Nov 2008 18:45:46
 *
 *
 */
public interface ConnectionManager
{
   ClientSession createSession(final String username,
                               final String password,
                               final boolean xa,
                               final boolean autoCommitSends,
                               final boolean autoCommitAcks,
                               final boolean preAcknowledge,
                               final int ackBatchSize,
                               final int minLargeMessageSize,
                               final boolean blockOnAcknowledge,
                               final boolean autoGroup,
                               final int sendWindowSize,
                               final int consumerWindowSize,
                               final int consumerMaxRate,
                               final int producerMaxRate,
                               final boolean blockOnNonPersistentSend,
                               final boolean blockOnPersistentSend) throws MessagingException;

   void removeSession(final ClientSessionInternal session);

   int numConnections();

   int numSessions();
}