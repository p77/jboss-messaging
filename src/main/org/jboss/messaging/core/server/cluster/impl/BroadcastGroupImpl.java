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

package org.jboss.messaging.core.server.cluster.impl;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.server.cluster.BroadcastGroup;

/**
 * A BroadcastGroupImpl
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * Created 15 Nov 2008 09:45:32
 *
 */
public class BroadcastGroupImpl implements BroadcastGroup, Runnable
{
   private static final Logger log = Logger.getLogger(BroadcastGroupImpl.class);

   private final InetAddress localBindAddress;

   private final int localPort;

   private final InetAddress groupAddress;

   private final int groupPort;

   private DatagramSocket socket;
   
   private final List<TransportConfiguration> connectors = new ArrayList<TransportConfiguration>();
   
   private volatile boolean started;
   
   private volatile ScheduledFuture<?> future;
   
   public BroadcastGroupImpl(final InetAddress localBindAddress,
                             final int localPort,
                             final InetAddress groupAddress,
                             final int groupPort) throws Exception
   {
      this.localBindAddress = localBindAddress;

      this.localPort = localPort;

      this.groupAddress = groupAddress;

      this.groupPort = groupPort;

      // FIXME - doesn't seem to work when specifying port and address

      // this.socket = new DatagramSocket(localPort, localBindAddress);     
   }
   
   public synchronized void start() throws Exception
   {
      if (started)
      {
         return;
      }
      
      socket = new DatagramSocket();
      
      started = true;
   }
   
   public synchronized void stop()
   {
      if (!started)
      {
         return;
      }
      
      if (future != null)
      {
         future.cancel(false);
      }
            
      socket.close();
      
      started = false;
   }
   
   public boolean isStarted()
   {
      return started;
   }
   
   public void addConnector(final TransportConfiguration connector)
   {
      connectors.add(connector);
   }

   public void removeConnector(final TransportConfiguration connector)
   {
      connectors.remove(connector);
   }

   public int size()
   {
      return connectors.size();
   }
   
   public void broadcastConnectors() throws Exception
   {
      // TODO - for now we just use plain serialization to serialize the transport configs
      // we should use our own format for a tighter representation
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      ObjectOutputStream oos = new ObjectOutputStream(bos);

      oos.writeInt(connectors.size());

      for (TransportConfiguration connector : connectors)
      {
         oos.writeObject(connector);
      }

      oos.flush();

      byte[] data = bos.toByteArray();

      DatagramPacket packet = new DatagramPacket(data, data.length, groupAddress, groupPort);

      log.info("broadcasting packet");

      socket.send(packet);
   }

   public void run()
   {
      if (!started)
      {
         return;
      }
      
      try
      {      
         broadcastConnectors();
      }
      catch (Exception e)
      {
         log.error("Failed to broadcast connector configs");
      }
   }
   
   public void setScheduledFuture(final ScheduledFuture<?> future)
   {
      this.future = future;
   }

}