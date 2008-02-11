/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.codec;

import static org.jboss.messaging.core.remoting.wireformat.PacketType.SESS_BROWSER_NEXTMESSAGE_RESP;

import org.jboss.messaging.core.Message;
import org.jboss.messaging.core.remoting.wireformat.SessionBrowserNextMessageResponseMessage;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>.
 */
public class SessionBrowserNextMessageResponseMessageCodec extends AbstractPacketCodec<SessionBrowserNextMessageResponseMessage>
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionBrowserNextMessageResponseMessageCodec()
   {
      super(SESS_BROWSER_NEXTMESSAGE_RESP);
   }

   // Public --------------------------------------------------------

   // AbstractPacketCodec overrides ---------------------------------

   @Override
   protected void encodeBody(SessionBrowserNextMessageResponseMessage response, RemotingBuffer out) throws Exception
   {      
      byte[] encodedMsg = encodeMessage(response.getMessage());

      int bodyLength = INT_LENGTH + encodedMsg.length;

      out.putInt(bodyLength);      
      out.putInt(encodedMsg.length);
      out.put(encodedMsg);
   }

   @Override
   protected SessionBrowserNextMessageResponseMessage decodeBody(RemotingBuffer in)
         throws Exception
   {
      int bodyLength = in.getInt();
      if (in.remaining() < bodyLength)
      {
         return null;
      }

      int msgLength = in.getInt();
      byte[] encodedMsg = new byte[msgLength];
      in.get(encodedMsg);
      Message message = decodeMessage(encodedMsg);

      return new SessionBrowserNextMessageResponseMessage(message);
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private ----------------------------------------------------

   // Inner classes -------------------------------------------------
}