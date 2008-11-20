/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.messaging.core.client.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.jboss.messaging.core.client.ClientMessage;
import org.jboss.messaging.core.client.FileClientMessage;
import org.jboss.messaging.core.exception.MessagingException;
import org.jboss.messaging.core.message.Message;
import org.jboss.messaging.core.remoting.impl.ByteBufferWrapper;
import org.jboss.messaging.core.remoting.spi.MessagingBuffer;

/**
 * A FileClientMessageImpl
 *
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * 
 * Created Oct 13, 2008 4:33:56 PM
 *
 *
 */
public class FileClientMessageImpl extends ClientMessageImpl implements FileClientMessage
{

   File file;

   FileChannel currentChannel;

   /**
    * 
    */
   public FileClientMessageImpl()
   {
      super();
   }

   public FileClientMessageImpl(final boolean durable)
   {
      super(durable, null);
   }

   /**
    * @param type
    * @param durable
    * @param expiration
    * @param timestamp
    * @param priority
    * @param body
    */
   public FileClientMessageImpl(final byte type,
                                final boolean durable,
                                final long expiration,
                                final long timestamp,
                                final byte priority,
                                final MessagingBuffer body)
   {
      super(type, durable, expiration, timestamp, priority, body);
   }

   /**
    * @param type
    * @param durable
    * @param body
    */
   public FileClientMessageImpl(final byte type, final boolean durable, final MessagingBuffer body)
   {
      super(type, durable, body);
   }

   /**
    * @param deliveryCount
    */
   public FileClientMessageImpl(final int deliveryCount)
   {
      super(deliveryCount);
   }

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * @return the file
    */
   public File getFile()
   {
      return file;
   }

   /**
    * @param file the file to set
    */
   public void setFile(final File file)
   {
      this.file = file;
   }

   @Override
   public MessagingBuffer getBody()
   {
      // TODO: Throw an unsuported exception. (Make sure no tests are using this method first)

      FileChannel channel = null;
      try
      {
         // We open a new channel on getBody.
         // for a better performance, users should be using the channels when using file
         channel = newChannel();

         ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());

         channel.position(0);
         channel.read(buffer);

         return new ByteBufferWrapper(buffer);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         try
         {
            channel.close();
         }
         catch (Throwable ignored)
         {

         }
      }
   }

   @Override
   public synchronized void encodeBody(final MessagingBuffer buffer, final long start, final int size)
   {
      try
      {
         FileChannel channel = getChannel();

         ByteBuffer bufferRead = ByteBuffer.allocate(size);

         channel.position(start);
         channel.read(bufferRead);

         buffer.putBytes(bufferRead.array());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }

   }

   @Override
   public void setBody(final MessagingBuffer body)
   {

      throw new RuntimeException("Not supported");
   }

   public synchronized FileChannel getChannel() throws MessagingException
   {
      if (currentChannel == null)
      {
         currentChannel = newChannel();
      }

      return currentChannel;
   }

   public synchronized void closeChannel() throws MessagingException
   {
      if (currentChannel != null)
      {
         try
         {
            currentChannel.close();
         }
         catch (IOException e)
         {
            throw new MessagingException(MessagingException.INTERNAL_ERROR, e.getMessage(), e);
         }
         currentChannel = null;
      }

   }

   @Override
   public synchronized int getBodySize()
   {
      return (int)file.length();
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   /**
    * @return
    * @throws FileNotFoundException
    * @throws IOException
    */
   private FileChannel newChannel() throws MessagingException
   {
      try
      {
         RandomAccessFile randomFile = new RandomAccessFile(getFile(), "rw");
         randomFile.seek(0);

         FileChannel channel = randomFile.getChannel();
         return channel;
      }
      catch (IOException e)
      {
         throw new MessagingException(MessagingException.INTERNAL_ERROR, e.getMessage(), e);
      }
   }

   // Inner classes -------------------------------------------------

}