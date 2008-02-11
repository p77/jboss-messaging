/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.wireformat;


/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 * @version <tt>$Revision$</tt>
 */
public class SessionCreateConsumerMessage extends AbstractPacket
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private String queueName;
   private String filterString;
   private boolean noLocal;
   private boolean autoDeleteQueue;
      
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionCreateConsumerMessage(String queueName, String filterString, boolean noLocal, boolean autoDeleteQueue)
   {
      super(PacketType.SESS_CREATECONSUMER);

      this.queueName = queueName;
      this.filterString = filterString;
      this.noLocal = noLocal;
      this.autoDeleteQueue = autoDeleteQueue;
   }

   // Public --------------------------------------------------------

   @Override
   public String toString()
   {
      StringBuffer buff = new StringBuffer(getParentString());
      buff.append(", queueName=" + queueName);
      buff.append(", filterString=" + filterString);
      buff.append(", noLocal=" + noLocal);
      buff.append(", autoDeleteQueue=" + autoDeleteQueue);
      buff.append("]");
      return buff.toString();
   }

   public String getQueueName()
   {
      return queueName;
   }

   public String getFilterString()
   {
      return filterString;
   }

   public boolean isNoLocal()
   {
      return noLocal;
   }
   
   public boolean isAutoDeleteQueue()
   {
      return autoDeleteQueue;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}