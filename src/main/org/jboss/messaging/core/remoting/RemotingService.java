/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting;

import org.jboss.messaging.core.MessagingComponent;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 *
 * @version <tt>$Revision$</tt>
 *
 */
public interface RemotingService extends MessagingComponent
{
   PacketDispatcher getDispatcher();

   RemotingConfiguration getRemotingConfiguration();
   
   void addInterceptor(Interceptor interceptor);

   void removeInterceptor(Interceptor interceptor);

   void addConnectionExceptionListener(ConnectionExceptionListener listener);

   void removeConnectionExceptionListener(ConnectionExceptionListener listener);  
}