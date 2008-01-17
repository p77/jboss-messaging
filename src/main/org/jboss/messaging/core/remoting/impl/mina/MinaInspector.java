/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.mina;

import static org.apache.mina.filter.reqres.ResponseType.WHOLE;
import static org.jboss.messaging.core.remoting.wireformat.AbstractPacket.NO_CORRELATION_ID;

import org.apache.mina.filter.reqres.ResponseInspector;
import org.apache.mina.filter.reqres.ResponseType;
import org.jboss.messaging.core.remoting.wireformat.AbstractPacket;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>.
 * 
 * @version <tt>$Revision$</tt>
 */
public class MinaInspector implements ResponseInspector
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // ResponseInspector implementation ------------------------------

   public Object getRequestId(Object message)
   {
      if (!(message instanceof AbstractPacket))
      {
         return null;
      }
      AbstractPacket packet = (AbstractPacket) message;
      if (packet.getCorrelationID() != NO_CORRELATION_ID)
         return packet.getCorrelationID();
      else
         return null;
   }

   public ResponseType getResponseType(Object message)
   {
      if (!(message instanceof AbstractPacket))
      {
         return null;
      }

      return WHOLE;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}