/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.wireformat;



/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * @version <tt>$Revision$</tt>
 */
public class SessionXASetTimeoutMessage extends AbstractPacket
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   
   private final int timeoutSeconds;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionXASetTimeoutMessage(final int timeoutSeconds)
   {
      super(PacketType.SESS_XA_SET_TIMEOUT);
      
      this.timeoutSeconds = timeoutSeconds;
   }
   

   // Public --------------------------------------------------------
   
   public int getTimeoutSeconds()
   {
      return this.timeoutSeconds;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
