/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.wireformat;

import static org.jboss.messaging.core.remoting.impl.wireformat.PacketType.SESS_REMOVE_DESTINATION;


/**
 * 
 * A SessionRemoveDestinationMessage
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class SessionRemoveDestinationMessage extends AbstractPacket
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   
   private final String address;
   
   private final boolean temporary;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   public SessionRemoveDestinationMessage(final String address, final boolean temporary)
   {
      super(SESS_REMOVE_DESTINATION);
      
      this.address = address;
      
      this.temporary = temporary;
   }

   // Public --------------------------------------------------------
   
   public String getAddress()
   {
      return address;
   }
   
   public boolean isTemporary()
   {
   	return temporary;
   }
   
   @Override
   public String toString()
   {
      return getParentString() + ", address=" + address + ", temp=" + temporary + "]";
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

