/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.codec;

import static org.jboss.messaging.core.remoting.impl.wireformat.PacketType.SESS_CREATEBROWSER;

import org.jboss.messaging.core.remoting.impl.wireformat.SessionCreateBrowserMessage;
import org.jboss.messaging.util.MessagingBuffer;
import org.jboss.messaging.util.SimpleString;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 */
public class SessionCreateBrowserMessageCodec extends
      AbstractPacketCodec<SessionCreateBrowserMessage>
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionCreateBrowserMessageCodec()
   {
      super(SESS_CREATEBROWSER);
   }

   // Public --------------------------------------------------------

   // AbstractPacketCodec overrides ---------------------------------

   @Override
   protected void encodeBody(final SessionCreateBrowserMessage request, final MessagingBuffer out) throws Exception
   {
      SimpleString queueName = request.getQueueName();
      SimpleString filterString = request.getFilterString();

      out.putSimpleString(queueName);
      out.putNullableSimpleString(filterString);
   }

   @Override
   protected SessionCreateBrowserMessage decodeBody(final MessagingBuffer in)
         throws Exception
   {
      SimpleString queueName = in.getSimpleString();
      SimpleString filterString = in.getNullableSimpleString();

      return new SessionCreateBrowserMessage(queueName, filterString);
   }

   // Inner classes -------------------------------------------------
}
