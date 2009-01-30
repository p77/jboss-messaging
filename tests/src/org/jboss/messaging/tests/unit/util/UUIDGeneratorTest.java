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

package org.jboss.messaging.tests.unit.util;

import java.net.InetAddress;

import junit.framework.TestCase;

import org.jboss.messaging.util.UUIDGenerator;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 *
 * @version <tt>$Revision$</tt>
 *
 */
public class UUIDGeneratorTest extends TestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testGetHardwareAddress() throws Exception
   {
      String javaVersion = System.getProperty("java.vm.version");
      if (javaVersion.startsWith("1.5"))
      {
         assertNull(UUIDGenerator.getHardwareAddress());
      } else if (javaVersion.startsWith("1.6"))
      {
         assertNotNull(UUIDGenerator.getHardwareAddress());
      }
   }
   
   public void testGetInetAddress() throws Exception
   {
      InetAddress address = UUIDGenerator.getInetAddress();
      if (address != null)
      {
         assertFalse(address.isLoopbackAddress());
         assertFalse(address.isSiteLocalAddress());
      }
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}