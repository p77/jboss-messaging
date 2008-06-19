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

package org.jboss.messaging.tests.unit.core.remoting.impl;


import junit.framework.TestCase;

import org.jboss.messaging.core.client.ClientConnection;
import org.jboss.messaging.core.client.ClientConnectionFactory;
import org.jboss.messaging.core.client.impl.ClientConnectionFactoryImpl;
import org.jboss.messaging.core.client.impl.LocationImpl;
import org.jboss.messaging.core.config.impl.ConfigurationImpl;
import org.jboss.messaging.core.remoting.TransportType;
import org.jboss.messaging.core.server.ConnectionManager;
import org.jboss.messaging.core.server.MessagingService;
import org.jboss.messaging.core.server.impl.MessagingServerImpl;
import org.jboss.messaging.core.server.impl.MessagingServiceImpl;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * @version <tt>$Revision$</tt>
 * 
 */
public class ConnectionManagerTest extends TestCase
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   // Attributes ----------------------------------------------------
   private MessagingService messagingService;
   // Constructors --------------------------------------------------

   public ConnectionManagerTest(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      ConfigurationImpl config = new ConfigurationImpl();
      config.setTransport(TransportType.INVM);
      messagingService = MessagingServiceImpl.newNullStorageMessagingServer(config);
      messagingService.start();
   }

   protected void tearDown() throws Exception
   {
      if (messagingService != null)
      {
         messagingService.stop();
         messagingService = null;
      }
   }

   // TestCase overrides -------------------------------------------

   // Public --------------------------------------------------------
   
   public void testTwoConnectionsFromTheSameVM() throws Exception
   {
      
      assertActiveConnectionsOnTheServer(0);
      ClientConnectionFactory cf = new ClientConnectionFactoryImpl(new LocationImpl(0));

      ClientConnection conn_1 = cf.createConnection();
      
      assertActiveConnectionsOnTheServer(1);
      
      ClientConnection conn_2 = cf.createConnection();
      
      assertActiveConnectionsOnTheServer(2);
      
      assertFalse(conn_1.equals(conn_2));
      
      conn_1.close();
      
      assertActiveConnectionsOnTheServer(1);
      
      conn_2.close();
      
      assertActiveConnectionsOnTheServer(0);
   }

   private void assertActiveConnectionsOnTheServer(int expectedSize) throws Exception
   {      
      assertEquals(expectedSize, messagingService.getServer().getServerManagement().getConnectionCount());
   }

}
