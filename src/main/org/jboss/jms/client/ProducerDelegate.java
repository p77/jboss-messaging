/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.client;

import javax.jms.JMSException;
import javax.jms.Message;

import org.jboss.jms.message.JBossMessage;

/**
 * The implementation of a producer
 * 
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version $Revision$
 */
public interface ProducerDelegate
   extends Lifecycle
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Send a message
    * 
    * @param message the message
    * @throws JMSException for any error
    */
   void send(Message message)
      throws JMSException;

   /**
    * Encapsulate a message
    * 
    * @param message the message
    * @throws JMSException for any error
    */
   JBossMessage encapsulateMessage(Message message)
      throws JMSException;

   // Inner Classes --------------------------------------------------
}
