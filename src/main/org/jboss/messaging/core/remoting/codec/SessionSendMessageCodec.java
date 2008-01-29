/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.codec;

import static org.jboss.messaging.core.remoting.wireformat.PacketType.MSG_SENDMESSAGE;

import org.jboss.messaging.core.Message;
import org.jboss.messaging.core.remoting.wireformat.SessionSendMessage;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>.
 */
public class SessionSendMessageCodec extends AbstractPacketCodec<SessionSendMessage>
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionSendMessageCodec()
   {
      super(MSG_SENDMESSAGE);
   }

   // Public --------------------------------------------------------

   // AbstractPacketCodec overrides ---------------------------------

   @Override
   protected void encodeBody(SessionSendMessage message, RemotingBuffer out) throws Exception
   {
      byte[] encodedMsg = encodeMessage(message.getMessage());   

      int bodyLength = INT_LENGTH + encodedMsg.length;

      out.putInt(bodyLength);
      out.putInt(encodedMsg.length);
      out.put(encodedMsg);
   }

   @Override
   protected SessionSendMessage decodeBody(RemotingBuffer in)
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
      Message msg = decodeMessage(encodedMsg);

      return new SessionSendMessage(msg);
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private ----------------------------------------------------

   // Inner classes -------------------------------------------------
}