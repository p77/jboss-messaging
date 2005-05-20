/**
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.local;

import org.jboss.messaging.core.Channel;
import org.jboss.messaging.core.util.SingleReceiverAcknowledgmentStore;


/**
 * Extends ChannelSupport for single output channels. It assumes that there is only one
 * Receiver that can handle messages, so there is only one source of NACKs.
 *
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 */
public abstract class SingleOutputChannelSupport extends ChannelSupport
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * The default behaviour is synchronous.
    */
   public SingleOutputChannelSupport()
   {
      this(Channel.SYNCHRONOUS);
   }

   public SingleOutputChannelSupport(boolean mode)
   {
      super(mode);
      // the channel uses an AcknowlegmentStore optimized for a single receiver
      localAcknowledgmentStore = new SingleReceiverAcknowledgmentStore("LocalAckStore");
   }

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
