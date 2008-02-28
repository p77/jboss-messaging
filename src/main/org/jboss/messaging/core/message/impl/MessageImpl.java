/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.messaging.core.message.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.message.Message;
import org.jboss.messaging.core.message.MessageReference;
import org.jboss.messaging.core.server.Queue;
import org.jboss.messaging.util.StreamUtils;

/**
 * A concrete implementation of a message
 * 
 * All messages handled by JBM core are of this type
 * 
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @version <tt>$Revision: 2740 $</tt>
 * 
 * For normal message transportation serialization is not used
 * 
 * $Id: MessageSupport.java 2740 2007-05-30 11:36:28Z timfox $
 */
public class MessageImpl implements Message
{
   // Constants -----------------------------------------------------
   
   private static final Logger log = Logger.getLogger(MessageImpl.class);

   // Attributes ----------------------------------------------------

   private long messageID;
   
   private int type;
   
   private boolean durable;

   /** GMT milliseconds at which this message expires. 0 means never expires * */
   private long expiration;

   private long timestamp;

   private Map<String, Object> headers;
   
   private byte priority;

   //The payload of MessageImpl instances is opaque
   private byte[] payload;
   
   //We keep track of the persisted references for this message
   private final List<MessageReference> references = new ArrayList<MessageReference>();
   
   private String connectionID;
   
   private volatile int numDurableReferences;
   
   private int deliveryCount;
   
   /*
    * We use a semaphore of size 1, i.e. a mutex to synchronize access to the refs rather than
    * use the synchronized keyword since java.util.Semaphore uses compareAndSet to maintain the semaphore
    * i.e. no locking which should give better performance under high contention
    */
   private Semaphore refsLock = new Semaphore(1);
   
   // Constructors --------------------------------------------------

   /*
    * Construct a message for deserialization or streaming
    */
   public MessageImpl()
   {
      this.headers = new HashMap<String, Object>();
   }

   /*
    * Construct a message before sending
    */
   public MessageImpl(final int type, final boolean durable, final long expiration,
                      final long timestamp, final byte priority)
   {
      this();
      this.type = type;
      this.durable = durable;
      this.expiration = expiration;
      this.timestamp = timestamp;
      this.priority = priority;            
   }

   /*
    * Construct a MessageImpl from storage
    */
   public MessageImpl(final long messageID, final int type, final boolean durable, final long expiration,
                      final long timestamp, final byte priority, final byte[] headers, final byte[] payload)
      throws Exception
   {
      this.messageID = messageID;
      this.type = type;
      this.durable = durable;
      this.expiration = expiration;
      this.timestamp = timestamp;
      this.priority = priority;
      
      if (headers == null)
      {
         this.headers = new HashMap<String, Object>();
      }
      else
      {
         //TODO keep headers opaque on server
         ByteArrayInputStream bis = new ByteArrayInputStream(headers);

         DataInputStream dais = new DataInputStream(bis);

         this.headers = StreamUtils.readMap(dais, true);

         dais.close();
      }
      this.payload = payload;
   }
   
   /**
    * Copy constructor
    * 
    * @param other
    */
   public MessageImpl(final MessageImpl other)
   {
      this.messageID = other.messageID;
      this.type = other.type;
      this.durable = other.durable;
      this.expiration = other.expiration;
      this.timestamp = other.timestamp;
      this.priority = other.priority;
      this.headers = new HashMap<String, Object>(other.headers);
      this.payload = other.payload;
   }
   
   // Message implementation ----------------------------------------

   public long getMessageID()
   {
      return messageID;
   }
   
   public void setMessageID(final long id)
   {
      this.messageID = id;
   }
   
   public int getType()
   {
      return type;
   }

   public boolean isDurable()
   {
      return durable;
   }
   
   public void setDurable(final boolean durable)
   {
      this.durable = durable;
   }

   public long getExpiration()
   {
      return expiration;
   }

   public void setExpiration(final long expiration)
   {
      this.expiration = expiration;
   }

   public long getTimestamp()
   {
      return timestamp;
   }
   
   public void setTimestamp(final long timestamp)
   {
      this.timestamp = timestamp;
   }

   public Object putHeader(final String name, final Object value)
   {
      return headers.put(name, value);
   }

   public Object getHeader(final String name)
   {
      return headers.get(name);
   }

   public Object removeHeader(final String name)
   {
      return headers.remove(name);
   }

   public boolean containsHeader(final String name)
   {
      return headers.containsKey(name);
   }

   public Map<String, Object> getHeaders()
   {
      return headers;
   }

   public byte getPriority()
   {
      return priority;
   }

   public void setPriority(final byte priority)
   {
      this.priority = priority;
   }

   // TODO - combine with getPayloadAsByteArray to get one big blob
   public byte[] getHeaderBytes() throws Exception
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

      DataOutputStream oos = new DataOutputStream(bos);

      StreamUtils.writeMap(oos, headers, true);

      oos.close();

      return bos.toByteArray();
   }
         
   public byte[] getPayload()
   {     
      return payload;
   }
   
   public void setPayload(final byte[] payload)
   {
      this.payload = payload;
   }
   
   public String getConnectionID()
   {
      return connectionID;
   }
   
   public void setConnectionID(final String connectionID)
   {
      this.connectionID = connectionID;
   }
   
   public void setDeliveryCount(final int deliveryCount)
   {
      this.deliveryCount = deliveryCount;
   }
   
   public int getDeliveryCount()
   {
      return this.deliveryCount;
   }

   public boolean isExpired()
   {
      if (expiration == 0)
      {
         return false;
      }
      
      return System.currentTimeMillis() - expiration >= 0;
   }
   
   public MessageReference createReference(final Queue queue)
   {
      MessageReference ref = new MessageReferenceImpl(this, queue);
      
      references.add(ref);
      
      if (durable && queue.isDurable())
      {
         numDurableReferences++;
      }
      
      return ref;
   }
   
   public int getNumDurableReferences()
   {
      return numDurableReferences;
   }
   
   public List<MessageReference> getReferences()
   {
      return references;
   }
   
   public void removeDurableReference(final MessageReference reference, final int pos) throws Exception
   {
      refsLock.acquire();
      
      try
      {      
         references.remove(pos);
               
         numDurableReferences--;                     
      }
      finally
      {
         refsLock.release();
      }
   }
   
   //TODO optimise to avoid scan
   public int getDurableReferencePos(final MessageReference reference)
   {
      int pos = 0;
      
      for (MessageReference ref: references)
      {
         if (ref == reference)
         {
            break;
         }
         
         if (ref.getQueue().isDurable())
         {
            pos++;
         }         
      }
      
      return pos;
   }
   
   public void addBackDurableReference(final MessageReference reference)
   {
      references.add(reference);
      
      numDurableReferences++;
   }
   
   public Message copy()
   {
      return new MessageImpl(this);
   }

   public void send() throws Exception
   {
      refsLock.acquire();
      
      try
      {      
         for (MessageReference ref: references)
         {
            ref.getQueue().addLast(ref);
         }
      }
      finally
      {
         refsLock.release();
      }
   }
    
   // Public --------------------------------------------------------

   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      
      if (!(o instanceof MessageImpl))
      {
         return false;
      }
      
      MessageImpl that = (MessageImpl) o;
      
      return that.messageID == this.messageID;
   }

   public int hashCode()
   {
      return (int) ((this.messageID >>> 32) ^ this.messageID);
   }

   public String toString()
   {
      return "M[" + messageID + "]@" + System.identityHashCode(this);
   }

   // Streamable implementation ---------------------------------

   public void write(final DataOutputStream out) throws Exception
   {
      out.writeLong(messageID);
      
      out.writeInt(type);

      out.writeBoolean(durable);

      out.writeLong(expiration);

      out.writeLong(timestamp);

      StreamUtils.writeMap(out, headers, true);

      out.writeByte(priority);
      
      out.writeInt(deliveryCount);

      if (payload != null)
      {
         out.writeInt(payload.length);

         out.write(payload);
      }
      else
      {
         out.writeInt(0);
      }
   }

   public void read(final DataInputStream in) throws Exception
   {
      messageID = in.readLong();
      
      type = in.readInt();

      durable = in.readBoolean();

      expiration = in.readLong();

      timestamp = in.readLong();

      headers = StreamUtils.readMap(in, true);

      priority = in.readByte();

      deliveryCount = in.readInt();
      
      int length = in.readInt();

      if (length == 0)
      {
         // no payload
         payload = null;
      }
      else
      {
         payload = new byte[length];

         in.readFully(payload);
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------  
}