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
package org.jboss.messaging.jms.client;

import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;

import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.jms.JBossDestination;

/**
 * This class implements javax.jms.ConnectionConsumer
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * 
 * Partially based on JBossMQ version by:
 * 
 * @author Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * 
 * @version $Revision$
 *
 * $Id$
 */
public class JBossConnectionConsumer implements ConnectionConsumer, Runnable
{
   // Constants -----------------------------------------------------

   private static Logger log = Logger.getLogger(JBossConnectionConsumer.class);

   private static boolean trace = log.isTraceEnabled();
   
   private static final int TIMEOUT = 20000;
   
   // Attributes ----------------------------------------------------
   
   private org.jboss.messaging.core.client.ClientConsumer cons;
   
   private org.jboss.messaging.core.client.ClientSession sess;
   
   private String consumerID;
   
   /** The ServerSessionPool that is implemented by the AS */
   private ServerSessionPool serverSessionPool;
   
   /** The maximum number of messages that a single session will be loaded with. */
   private int maxMessages;
   
   /** Is the ConnectionConsumer closed? */
   private volatile boolean closed;
     
   /** The "listening" thread that gets messages from destination and queues
   them for delivery to sessions */
   private Thread internalThread;
   
   /** The thread id */
   private int id;
   
   /** The thread id generator */
   private static AtomicInteger threadId = new AtomicInteger(0);
   
   private int maxDeliveries;
   
   private String queueName;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   public JBossConnectionConsumer(org.jboss.messaging.core.client.ClientConnection conn, JBossDestination dest,
                                  String subName, String messageSelector,
                                  ServerSessionPool sessPool, int maxMessages) throws JMSException
   {
//      this.serverSessionPool = sessPool;
//      this.maxMessages = maxMessages;
//
//      if (this.maxMessages < 1)
//      {
//         this.maxMessages = 1;
//      }
//
//      // Create a consumer. The ClientConsumer knows we are a connection consumer so will
//      // not call pre or postDeliver so messages won't be acked, or stored in session/tx.
//      sess = conn.createClientSession(false, Session.CLIENT_ACKNOWLEDGE, false);
//          
//      //cons = sess.createClientConsumer(dest.toCoreDestination(), messageSelector, false, subName);
//
//      this.consumerID = cons.getID();      
//        
//      //this.maxDeliveries = cons.getMaxDeliveries();
//         
//      if (subName != null)
//      {
//         queueName = MessageQueueNameHelper.createSubscriptionName(conn.getClientID(), subName);
//      }
//      else
//      {
//         queueName = dest.getName();
//      }
//
//      id = threadId.increment();
//      internalThread = new Thread(this, "Connection ClientConsumer for dest " + dest + " id=" + id);
//      internalThread.start();
//
//      if (trace) { log.trace(this + " created"); }
   }
   
   // ConnectionConsumer implementation -----------------------------

   public ServerSessionPool getServerSessionPool() throws JMSException
   {
      return serverSessionPool;
   }

   public void close() throws JMSException
   {
      if (trace) { log.trace("close " + this); }
      
      doClose();
      
      //Wait for internal thread to complete
      if (trace) { log.trace(this + " Waiting for internal thread to complete"); }
      
      try
      {
         internalThread.join(TIMEOUT);
         
         if (internalThread.isAlive())
         {            
            throw new JMSException(this + " Waited " + TIMEOUT + " ms for internal thread to complete, but it didn't");
         }
      }
      catch (InterruptedException e)
      {
         if (trace) { log.trace(this + " Thread interrupted while waiting for internal thread to complete"); }
         //Ignore
      }
      
      if (trace) { log.trace("Closed: " + this); }      
   }
   
   // Runnable implementation ---------------------------------------
    
   public void run()
   {
      //TODO - need to work out how to get ASF to work with core
      
//      if (trace) { log.trace("running connection consumer"); }
//      try
//      {
//         List mesList = new ArrayList();
//         
//         while (true)
//         {            
//            if (closed)
//            {
//               if (trace) { log.trace("Connection consumer is closed, breaking"); }
//               break;
//            }
//            
//            if (mesList.isEmpty())
//            {
//               // Remove up to maxMessages messages from the consumer
//               for (int i = 0; i < maxMessages; i++)
//               {               
//                  // receiveNoWait
//
//                  if (trace) { log.trace(this + " attempting to get message with receiveNoWait"); }
//                  
//                  Message m = null;
//                  
//                  try
//                  {
//                     m = cons.receive(-1);
//                  }
//                  catch (JMSException e)
//                  {
//                     //If the consumer is closed, we will get a JMSException so we ignore
//                     if (!closed)
//                     {
//                        throw e;
//                     }                        
//                  }
//               
//                  if (m == null)
//                  {
//                     if (trace) { log.trace("receiveNoWait did not retrieve any message"); }
//                     break;
//                  }
//
//                  if (trace) { log.trace("receiveNoWait got message " + m + " adding to queue"); }
//                  mesList.add(m);
//               }
//
//               if (mesList.isEmpty())
//               {
//                  // We didn't get any messages doing receiveNoWait, so let's wait. This returns if
//                  // a message is received or by the consumer closing.
//
//                  if (trace) { log.trace(this + " attempting to get message with blocking receive (no timeout)"); }
//
//                  Message m = null;
//                  
//                  try
//                  {
//                     m = cons.receive(0);                  
//                  }
//                  catch (JMSException e)
//                  {
//                     //If the consumer is closed, we will get a JMSException so we ignore
//                     if (!closed)
//                     {
//                        throw e;
//                     }                        
//                  }          
//                  
//                  if (m != null)
//                  {
//                     if (trace) { log.trace("receive (no timeout) got message " + m + " adding to queue"); }
//                     mesList.add(m);
//                  }
//                  else
//                  {
//                     // The consumer must have closed
//                     if (trace) { log.trace("blocking receive returned null, consumer must have closed"); }
//                     break;
//                  }
//               }
//            }
//            
//            if (!mesList.isEmpty())
//            {
//               if (trace) { log.trace("there are " + mesList.size() + " messages to send to session"); }
//
//               ServerSession serverSession = serverSessionPool.getServerSession();
//               JBossSession session = (JBossSession)serverSession.getSession();
//
//               MessageListener listener = session.getMessageListener();
//
//               if (listener == null)
//               {
//                  // Sanity check
//                  if (trace) { log.trace(this + ": session " + session + " did not have a set MessageListener"); }
//               }
//
//               for (int i = 0; i < mesList.size(); i++)
//               {
//                  JBossMessage m = (JBossMessage)mesList.get(i);
//                  session.addAsfMessage(m, consumerID, queueName, maxDeliveries, sess);
//                  if (trace) { log.trace("added " + m + " to session"); }
//               }
//
//               if (trace) { log.trace(this + " starting serverSession " + serverSession); }
//
//               serverSession.start();
//
//               if (trace) { log.trace(this + "'s serverSession processed messages"); }
//
//               mesList.clear();
//            }            
//         }
//         if (trace) { log.trace("ConnectionConsumer run() exiting"); }
//      }
//      catch (Throwable t)
//      {
//         log.debug("Connection consumer closing due to error in listening thread " + this, t);
//         
//         try
//         {
//            //Closing
//            doClose();
//         }
//         catch (JMSException e)
//         {
//            log.error("Failed to close connection consumer", e);
//         }
//      }            
   }
   
   protected synchronized void doClose() throws JMSException
   {
//      if (closed)
//      {
//         return;
//      }
//      
//      closed = true;            
//      
//      sess.closing();
//      sess.close();

      if (trace) { log.trace(this + "Closed message handler"); }
   }

   // Public --------------------------------------------------------

   public String toString()
   {
      return "JBossConnectionConsumer[" + consumerID + ", " + id + "]";
   }

   // Object overrides ----------------------------------------------

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
  
}