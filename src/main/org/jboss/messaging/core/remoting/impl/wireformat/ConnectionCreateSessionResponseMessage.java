/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.wireformat;

import static org.jboss.messaging.core.remoting.impl.Assert.assertValidID;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>.
 * 
 * @version <tt>$Revision$</tt>
 */
public class ConnectionCreateSessionResponseMessage extends AbstractPacket
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private final String sessionID;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public ConnectionCreateSessionResponseMessage(final String sessionID)
   {
      super(PacketType.CONN_CREATESESSION_RESP);

      assertValidID(sessionID);

      this.sessionID = sessionID;
   }

   // Public --------------------------------------------------------

   public String getSessionID()
   {
      return sessionID;
   }


   @Override
   public String toString()
   {
      return getParentString() + ", sessionID=" + sessionID
            + "]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}