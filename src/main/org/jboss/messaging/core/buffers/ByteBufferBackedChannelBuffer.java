/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.messaging.core.buffers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.UnsupportedCharsetException;

/**
 * A NIO {@link ByteBuffer} based buffer.  It is recommended to use {@link ChannelBuffers#directBuffer(int)}
 * and {@link ChannelBuffers#wrappedBuffer(ByteBuffer)} instead of calling the
 * constructor explicitly.
 *
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Trustin Lee (tlee@redhat.com)
 *
 * @version $Rev: 486 $, $Date: 2008-11-16 22:52:47 +0900 (Sun, 16 Nov 2008) $
 *
 */
public class ByteBufferBackedChannelBuffer extends AbstractChannelBuffer
{

   private final ByteBuffer buffer;

   private final int capacity;

   /**
    * Creates a new buffer which wraps the specified buffer's slice.
    */
   ByteBufferBackedChannelBuffer(final ByteBuffer buffer)
   {
      if (buffer == null)
      {
         throw new NullPointerException("buffer");
      }

      this.buffer = buffer;
      capacity = buffer.remaining();
   }

   public int capacity()
   {
      return capacity;
   }

   public byte getByte(final int index)
   {
      return buffer.get(index);
   }

   public short getShort(final int index)
   {
      return buffer.getShort(index);
   }

   public int getUnsignedMedium(final int index)
   {
      return (getByte(index) & 0xff) << 16 | (getByte(index + 1) & 0xff) << 8 | (getByte(index + 2) & 0xff) << 0;
   }

   public int getInt(final int index)
   {
      return buffer.getInt(index);
   }

   public long getLong(final int index)
   {
      return buffer.getLong(index);
   }

   public void getBytes(final int index, final ChannelBuffer dst, final int dstIndex, final int length)
   {
      if (dst instanceof ByteBufferBackedChannelBuffer)
      {
         ByteBufferBackedChannelBuffer bbdst = (ByteBufferBackedChannelBuffer)dst;
         ByteBuffer data = bbdst.buffer.duplicate();

         data.limit(dstIndex + length).position(dstIndex);
         getBytes(index, data);
      }
      else if (buffer.hasArray())
      {
         dst.setBytes(dstIndex, buffer.array(), index + buffer.arrayOffset(), length);
      }
      else
      {
         dst.setBytes(dstIndex, this, index, length);
      }
   }

   public void getBytes(final int index, final byte[] dst, final int dstIndex, final int length)
   {
      ByteBuffer data = buffer.duplicate();
      try
      {
         data.limit(index + length).position(index);
      }
      catch (IllegalArgumentException e)
      {
         throw new IndexOutOfBoundsException();
      }
      data.get(dst, dstIndex, length);
   }

   public void getBytes(final int index, final ByteBuffer dst)
   {
      ByteBuffer data = buffer.duplicate();
      int bytesToCopy = Math.min(capacity() - index, dst.remaining());
      try
      {
         data.limit(index + bytesToCopy).position(index);
      }
      catch (IllegalArgumentException e)
      {
         throw new IndexOutOfBoundsException();
      }
      dst.put(data);
   }

   public void setByte(final int index, final byte value)
   {
      buffer.put(index, value);
   }

   public void setShort(final int index, final short value)
   {
      buffer.putShort(index, value);
   }

   public void setMedium(final int index, final int value)
   {
      setByte(index, (byte)(value >>> 16));
      setByte(index + 1, (byte)(value >>> 8));
      setByte(index + 2, (byte)(value >>> 0));
   }

   public void setInt(final int index, final int value)
   {
      buffer.putInt(index, value);
   }

   public void setLong(final int index, final long value)
   {
      buffer.putLong(index, value);
   }

   public void setBytes(final int index, final ChannelBuffer src, final int srcIndex, final int length)
   {
      if (src instanceof ByteBufferBackedChannelBuffer)
      {
         ByteBufferBackedChannelBuffer bbsrc = (ByteBufferBackedChannelBuffer)src;
         ByteBuffer data = bbsrc.buffer.duplicate();

         data.limit(srcIndex + length).position(srcIndex);
         setBytes(index, data);
      }
      else if (buffer.hasArray())
      {
         src.getBytes(srcIndex, buffer.array(), index + buffer.arrayOffset(), length);
      }
      else
      {
         src.getBytes(srcIndex, this, index, length);
      }
   }

   public void setBytes(final int index, final byte[] src, final int srcIndex, final int length)
   {
      ByteBuffer data = buffer.duplicate();
      data.limit(index + length).position(index);
      data.put(src, srcIndex, length);
   }

   public void setBytes(final int index, final ByteBuffer src)
   {
      ByteBuffer data = buffer.duplicate();
      data.limit(index + src.remaining()).position(index);
      data.put(src);
   }

   public void getBytes(final int index, final OutputStream out, final int length) throws IOException
   {
      if (length == 0)
      {
         return;
      }

      if (!buffer.isReadOnly() && buffer.hasArray())
      {
         out.write(buffer.array(), index + buffer.arrayOffset(), length);
      }
      else
      {
         byte[] tmp = new byte[length];
         ((ByteBuffer)buffer.duplicate().position(index)).get(tmp);
         out.write(tmp);
      }
   }

   public int getBytes(final int index, final GatheringByteChannel out, final int length) throws IOException
   {
      if (length == 0)
      {
         return 0;
      }

      return out.write((ByteBuffer)buffer.duplicate().position(index).limit(index + length));
   }

   public int setBytes(int index, final InputStream in, int length) throws IOException
   {

      int readBytes = 0;

      if (!buffer.isReadOnly() && buffer.hasArray())
      {
         index += buffer.arrayOffset();
         do
         {
            int localReadBytes = in.read(buffer.array(), index, length);
            if (localReadBytes < 0)
            {
               if (readBytes == 0)
               {
                  return -1;
               }
               else
               {
                  break;
               }
            }
            readBytes += localReadBytes;
            index += localReadBytes;
            length -= localReadBytes;
         }
         while (length > 0);
      }
      else
      {
         byte[] tmp = new byte[length];
         int i = 0;
         do
         {
            int localReadBytes = in.read(tmp, i, tmp.length - i);
            if (localReadBytes < 0)
            {
               if (readBytes == 0)
               {
                  return -1;
               }
               else
               {
                  break;
               }
            }
            readBytes += localReadBytes;
            i += readBytes;
         }
         while (i < tmp.length);
         ((ByteBuffer)buffer.duplicate().position(index)).put(tmp);
      }

      return readBytes;
   }

   public int setBytes(final int index, final ScatteringByteChannel in, final int length) throws IOException
   {

      ByteBuffer slice = (ByteBuffer)buffer.duplicate().limit(index + length).position(index);
      int readBytes = 0;

      while (readBytes < length)
      {
         int localReadBytes;
         try
         {
            localReadBytes = in.read(slice);
         }
         catch (ClosedChannelException e)
         {
            localReadBytes = -1;
         }
         if (localReadBytes < 0)
         {
            if (readBytes == 0)
            {
               return -1;
            }
            else
            {
               return readBytes;
            }
         }
         else if (localReadBytes == 0)
         {
            break;
         }
         readBytes += localReadBytes;
      }

      return readBytes;
   }

   public ByteBuffer toByteBuffer(final int index, final int length)
   {
      if (index == 0 && length == capacity())
      {
         return buffer.duplicate();
      }
      else
      {
         return ((ByteBuffer)buffer.duplicate().position(index).limit(index + length)).slice();
      }
   }

   @Override
   public ByteBuffer toByteBuffer()
   {
      return buffer;
   }

   public String toString(final int index, final int length, final String charsetName)
   {
      if (!buffer.isReadOnly() && buffer.hasArray())
      {
         try
         {
            return new String(buffer.array(), index + buffer.arrayOffset(), length, charsetName);
         }
         catch (UnsupportedEncodingException e)
         {
            throw new UnsupportedCharsetException(charsetName);
         }
      }
      else
      {
         byte[] tmp = new byte[length];
         ((ByteBuffer)buffer.duplicate().position(index)).get(tmp);
         try
         {
            return new String(tmp, charsetName);
         }
         catch (UnsupportedEncodingException e)
         {
            throw new UnsupportedCharsetException(charsetName);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.messaging.core.buffers.ChannelBuffer#array()
    */
   public byte[] array()
   {
      return buffer.array();
   }
}
