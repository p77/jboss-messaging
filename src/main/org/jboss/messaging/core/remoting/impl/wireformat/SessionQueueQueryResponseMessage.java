package org.jboss.messaging.core.remoting.impl.wireformat;

import static org.jboss.messaging.core.remoting.impl.wireformat.PacketType.SESS_QUEUEQUERY_RESP;

import org.jboss.messaging.util.SimpleString;

/**
 * 
 * A SessionQueueQueryResponseMessage
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class SessionQueueQueryResponseMessage extends PacketImpl
{
   private final boolean exists;
   
   private final boolean durable;
   
   private final boolean temporary;
   
   private final int maxSize;
   
   private final int consumerCount;
   
   private final int messageCount;
   
   private final SimpleString filterString;
   
   private final SimpleString address;
   
   public SessionQueueQueryResponseMessage(final boolean durable, final boolean temporary, final int maxSize, 
   		final int consumerCount, final int messageCount, final SimpleString filterString, final SimpleString address)
   {
   	this(durable, temporary, maxSize, consumerCount, messageCount, filterString, address, true);
   }
   
   public SessionQueueQueryResponseMessage()
   {
      this(false, false, 0, 0, 0, null, null, false);
   }
   
   private SessionQueueQueryResponseMessage(final boolean durable, final boolean temporary, final int maxSize, 
   		final int consumerCount, final int messageCount, final SimpleString filterString, final SimpleString address,
   		final boolean exists)
   {
      super(SESS_QUEUEQUERY_RESP);
       
      this.durable = durable;
      
      this.temporary = temporary;
      
      this.maxSize = maxSize;
      
      this.consumerCount = consumerCount;
      
      this.messageCount = messageCount;
      
      this.filterString = filterString;
      
      this.address = address;
      
      this.exists = exists;      
   }
      
   public boolean isExists()
   {
      return exists;
   }
   
   public boolean isDurable()
   {
      return durable;
   }
   
   public boolean isTemporary()
   {
      return temporary;
   }
   
   public int getMaxSize()
   {
      return maxSize;
   }
   
   public int getConsumerCount()
   {
      return consumerCount;
   }
   
   public int getMessageCount()
   {
      return messageCount;
   }
   
   public SimpleString getFilterString()
   {
      return filterString;
   }
   
   public SimpleString getAddress()
   {
      return address;
   }
   
}
