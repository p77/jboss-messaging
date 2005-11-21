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
package org.jboss.messaging.core.distributed;

import org.jboss.messaging.core.local.Queue;
import org.jboss.messaging.core.local.Topic;
import org.jboss.messaging.core.MessageStore;
import org.jboss.messaging.core.PersistenceManager;
import org.jboss.messaging.core.Filter;
import org.jboss.messaging.util.SelectiveIterator;
import org.jboss.messaging.util.Util;
import org.jboss.messaging.util.NotYetImplementedException;
import org.jboss.logging.Logger;
import org.jgroups.blocks.RpcDispatcher;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * A distributed topic.
 *
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class DistributedTopic extends Topic implements DistributedDestination
 {
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(DistributedTopic.class);

   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------

   protected TopicPeer peer;
   protected ViewKeeper viewKeeper;

   // Constructors --------------------------------------------------

   public DistributedTopic(String name, RpcDispatcher dispatcher)
   {
      super(name);
      viewKeeper = new TopicViewKeeper();
      peer = new TopicPeer(this, dispatcher);
   }

   // Topic overrides -----------------------------------------------

   public Iterator iterator()
   {
      //return new SelectiveIterator(super.iterator(), RemoteReceiver.class);
      throw new NotYetImplementedException();
   }

   // DistributedDestination implementation --------------------------

   public void join() throws DistributedException
   {
      peer.join();
   }

   public void leave() throws DistributedException
   {
      peer.leave();
   }

   public void close() throws DistributedException
   {
      leave();
      // TODO - additional cleanup
   }


   public Peer getPeer()
   {
      return peer;
   }

   // Public --------------------------------------------------------

   public String toString()
   {
      return "DistributedTopic[" + getName() + ":" + Util.guidToString(peer.getID()) + "]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected ViewKeeper getViewKeeper()
   {
      return viewKeeper;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   /**
    * The inner class that manages the local representation of the distributed destination view.
    */
   private class TopicViewKeeper implements ViewKeeper
   {
      // Constants -----------------------------------------------------

      private final Logger log = Logger.getLogger(TopicViewKeeper.class);

      // Static --------------------------------------------------------

      // Attributes ----------------------------------------------------

      // Constructors --------------------------------------------------

      // ViewKeeper implementation -------------------------------------

      public Serializable getGroupID()
      {
         return getName();
      }

      public void removeRemotePeer(PeerIdentity remotePeerIdentity)
      {
         if (log.isTraceEnabled()) { log.trace(this + " removing remote peer " + remotePeerIdentity); }

         throw new NotYetImplementedException();
//         // TODO synchronization
//         for(Iterator i = router.iterator(); i.hasNext(); )
//         {
//            Object receiver = i.next();
//            if (receiver instanceof RemoteReceiver)
//            {
//               RemoteReceiver rr = (RemoteReceiver)receiver;
//               if (rr.getPeerIdentity().equals(remotePeerIdentity))
//               {
//                  i.remove();
//                  break;
//               }
//            }
//         }
      }

      public Set getRemotePeers()
      {
//         Set result = new HashSet();
//         for(Iterator i = router.iterator(); i.hasNext(); )
//         {
//            Object receiver = i.next();
//            if (receiver instanceof RemoteReceiver)
//            {
//               RemoteReceiver rr = (RemoteReceiver)receiver;
//               result.add(rr.getPeerIdentity());
//            }
//         }
//         return result;
         throw new NotYetImplementedException();
      }

      // Public --------------------------------------------------------

      public String toString()
      {
         return "DistributedTopic[" + getName() + ":" +
                Util.guidToString(peer.getID()) + "].ViewKeeper";
      }

      // Package protected ---------------------------------------------

      // Protected -----------------------------------------------------

      protected ViewKeeper getViewKeeper()
      {
         return viewKeeper;
      }

      // Private -------------------------------------------------------

      // Inner classes -------------------------------------------------
   }
}
