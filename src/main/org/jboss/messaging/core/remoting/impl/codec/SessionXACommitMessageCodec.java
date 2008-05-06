/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.codec;

import static org.jboss.messaging.util.DataConstants.SIZE_BOOLEAN;

import javax.transaction.xa.Xid;

import org.jboss.messaging.core.remoting.impl.wireformat.PacketType;
import org.jboss.messaging.core.remoting.impl.wireformat.SessionXACommitMessage;
import org.jboss.messaging.util.DataConstants;

/**
 * 
 * A SessionXACommitMessageCodec
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class SessionXACommitMessageCodec extends AbstractPacketCodec<SessionXACommitMessage>
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionXACommitMessageCodec()
   {
      super(PacketType.SESS_XA_COMMIT);
   }

   // Public --------------------------------------------------------

   // AbstractPacketCodec overrides ---------------------------------

   public int getBodyLength(final SessionXACommitMessage packet) throws Exception
   {   	
   	int bodyLength = getXidLength(packet.getXid()) + SIZE_BOOLEAN;
   	return bodyLength;
   }
   
   @Override
   protected void encodeBody(final SessionXACommitMessage message, final RemotingBuffer out) throws Exception
   {      
      encodeXid(message.getXid(), out);      
      out.putBoolean(message.isOnePhase());      
   }

   @Override
   protected SessionXACommitMessage decodeBody(final RemotingBuffer in)
         throws Exception
   {
      Xid xid = decodeXid(in);
      boolean onePhase = in.getBoolean();
                  
      return new SessionXACommitMessage(xid, onePhase);
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private ----------------------------------------------------

   // Inner classes -------------------------------------------------
}

