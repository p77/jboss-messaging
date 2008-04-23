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
package org.jboss.messaging.util;

import org.jboss.messaging.core.version.Version;
import org.jboss.messaging.core.version.impl.VersionImpl;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * This loads the version info in from a version.properties file.
 *
 * @author <a href="ataylor@redhat.com">Andy Taylor</a>
 */
public class VersionLoader
{
   public static Version load()
   {
      Properties versionProps = new Properties();
      InputStream in = VersionImpl.class.getClassLoader().getResourceAsStream("version.properties");
      if (in == null)
      {
         throw new RuntimeException("version.properties is not available");
      }
      try
      {
         versionProps.load(in);
         String versionName = versionProps.getProperty("messaging.version.versionName");
         int majorVersion = Integer.valueOf(versionProps.getProperty("messaging.version.majorVersion"));
         int minorVersion = Integer.valueOf(versionProps.getProperty("messaging.version.minorVersion"));
         int microVersion = Integer.valueOf(versionProps.getProperty("messaging.version.microVersion"));
         int incrementingVersion = Integer.valueOf(versionProps.getProperty("messaging.version.incrementingVersion"));
         String versionSuffix = versionProps.getProperty("messaging.version.versionSuffix");
         return new VersionImpl(versionName, majorVersion, minorVersion, microVersion, incrementingVersion, versionSuffix);
      }
      catch (IOException e)
      {
         //if we get here then the messaging hasnt been built properly and the version.properties is skewed in some way
         throw new RuntimeException("unable to load version.properties", e);
      }

   }
}