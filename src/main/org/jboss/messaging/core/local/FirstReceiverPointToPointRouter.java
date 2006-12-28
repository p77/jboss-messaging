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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.messaging.core.Delivery;
import org.jboss.messaging.core.DeliveryObserver;
import org.jboss.messaging.core.MessageReference;
import org.jboss.messaging.core.Receiver;
import org.jboss.messaging.core.Router;
import org.jboss.messaging.core.SimpleDelivery;
import org.jboss.messaging.core.tx.Transaction;

/**
 * 
 * It will always favour the first receiver in the internal list of receivers, but will retry
 * the next one (and the next one...) if a previous one does not want to accept the message.
 * If the router has several receivers (e.g. the case of multiple consumers on a queue)
 * then if the consumers are fast then the first receiver will tend to get most or all of the references
 * 
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @version <tt>$Revision: 1174 $</tt>
 * $Id: PointToPointRouter.java 1174 2006-08-02 14:14:32Z timfox $
 */
public class FirstReceiverPointToPointRouter implements Router
{
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(FirstReceiverPointToPointRouter.class);

   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   private boolean trace = log.isTraceEnabled();

   List receivers;

   // Constructors --------------------------------------------------

   public FirstReceiverPointToPointRouter()
   {
      receivers = new ArrayList();
   }

   // Router implementation -----------------------------------------

   public Delivery handle(DeliveryObserver observer, MessageReference ref, Transaction tx)
   {
      ArrayList receiversCopy;

      synchronized(receivers)
      {
         if (receivers.isEmpty())
         {
            return null;
         }

         // try to release the lock as quickly as possible and make a copy of the receivers array
         // to avoid deadlock

         receiversCopy = new ArrayList(receivers.size());
         receiversCopy.addAll(receivers);
      }

      Delivery del = null;
      boolean selectorRejected = false;

      for(Iterator i = receiversCopy.iterator(); i.hasNext(); )
      {
         Receiver receiver = (Receiver)i.next();

         try
         {
            Delivery d = receiver.handle(observer, ref, tx);

            if (trace) { log.trace("receiver " + receiver + " handled " + ref + " and returned " + d); }

            if (d != null)
            {
               if (d.isSelectorAccepted())
               {
                  // deliver to the first receiver that accepts
                  del = d;
                  break;
               }
               else
               {
                  selectorRejected = true;
               }
            }
         }
         catch(Throwable t)
         {
            // broken receiver - log the exception and ignore it
            log.error("The receiver " + receiver + " is broken", t);
         }
      }

      if (del == null && selectorRejected)
      {
         del = new SimpleDelivery(null, null, true, false);
      }

      return del;
   }


   public boolean add(Receiver r)
   {
      synchronized(receivers)
      {
         if (receivers.contains(r))
         {
            return false;
         }
         receivers.add(r);
      }
      return true;
   }


   public boolean remove(Receiver r)
   {
      synchronized(receivers)
      {
         return receivers.remove(r);
      }
   }

   public void clear()
   {
      synchronized(receivers)
      {
         receivers.clear();
      }
   }

   public boolean contains(Receiver r)
   {
      synchronized(receivers)
      {
         return receivers.contains(r);
      }
   }

   public Iterator iterator()
   {
      synchronized(receivers)
      {
         return receivers.iterator();
      }
   }
   
   public int numberOfReceivers()
   {
      synchronized(receivers)
      {
         return receivers.size();
      }
   }


   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------   
}
