/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.wireformat;

import javax.transaction.xa.Xid;


/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * @version <tt>$Revision$</tt>
 */
public class SessionXAForgetMessage extends AbstractPacket
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   
   private final Xid xid;
      
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionXAForgetMessage(final Xid xid)
   {
      super(PacketType.SESS_XA_FORGET);
      
      this.xid = xid;
   }

   // Public --------------------------------------------------------
   
   public Xid getXid()
   {
      return xid;
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

