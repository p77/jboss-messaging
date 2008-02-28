/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.messaging.core.client;

import javax.transaction.xa.XAResource;

import org.jboss.messaging.core.remoting.impl.wireformat.SessionBindingQueryResponseMessage;
import org.jboss.messaging.core.remoting.impl.wireformat.SessionQueueQueryResponseMessage;
import org.jboss.messaging.core.server.MessagingException;

/**
 *  
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 */
public interface ClientSession extends XAResource
{   
   void createQueue(String address, String queueName, String filterString, boolean durable, boolean temporary)
                    throws MessagingException;
   
   void deleteQueue(String queueName) throws MessagingException;
   
   void addAddress(String address) throws MessagingException;
   
   void removeAddress(String address) throws MessagingException;
   
   SessionQueueQueryResponseMessage queueQuery(String queueName) throws MessagingException;
   
   SessionBindingQueryResponseMessage bindingQuery(String address) throws MessagingException;
   
   ClientConsumer createConsumer(String queueName, String filterString, boolean noLocal,
                                 boolean autoDeleteQueue, boolean direct) throws MessagingException;
   
   ClientBrowser createBrowser(String queueName, String messageSelector) throws MessagingException;
   
   ClientProducer createProducer(String address) throws MessagingException;
   
   XAResource getXAResource();

   void commit() throws MessagingException;

   void rollback() throws MessagingException;
      
   void acknowledge() throws MessagingException;
   
   void close() throws MessagingException;
   
   boolean isClosed();        
}