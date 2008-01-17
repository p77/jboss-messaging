/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting;

/**
 * The transport types supported by JBoss Messaging.
 * 
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>.
 * 
 * @version <tt>$Revision$</tt>
 */
public enum TransportType
{
   TCP, HTTP, INVM;
   
   @Override
   public String toString()
   {
      return super.toString().toLowerCase();
   }
}