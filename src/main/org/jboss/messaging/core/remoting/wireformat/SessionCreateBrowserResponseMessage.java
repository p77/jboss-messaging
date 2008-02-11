/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.wireformat;

import static org.jboss.messaging.core.remoting.Assert.assertValidID;
import static org.jboss.messaging.core.remoting.wireformat.PacketType.SESS_CREATEBROWSER_RESP;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * @version <tt>$Revision$</tt>
 * 
 */
public class SessionCreateBrowserResponseMessage extends AbstractPacket
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private final String browserID;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionCreateBrowserResponseMessage(String browserID)
   {
      super(SESS_CREATEBROWSER_RESP);

      assertValidID(browserID);

      this.browserID = browserID;
   }

   // Public --------------------------------------------------------

   public String getBrowserID()
   {
      return browserID;
   }

   @Override
   public String toString()
   {
      return getParentString() + ", browserID=" + browserID + "]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}