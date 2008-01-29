/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.mina.integration.test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.jboss.messaging.core.remoting.TransportType.TCP;
import static org.jboss.messaging.core.remoting.impl.mina.MinaService.KEEP_ALIVE_INTERVAL_KEY;
import static org.jboss.messaging.core.remoting.impl.mina.MinaService.KEEP_ALIVE_TIMEOUT_KEY;
import static org.jboss.messaging.core.remoting.impl.mina.integration.test.TestSupport.KEEP_ALIVE_INTERVAL;
import static org.jboss.messaging.core.remoting.impl.mina.integration.test.TestSupport.KEEP_ALIVE_TIMEOUT;
import static org.jboss.messaging.core.remoting.impl.mina.integration.test.TestSupport.PORT;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.jboss.messaging.core.remoting.ConnectionExceptionListener;
import org.jboss.messaging.core.remoting.KeepAliveFactory;
import org.jboss.messaging.core.remoting.NIOSession;
import org.jboss.messaging.core.remoting.impl.mina.MinaConnector;
import org.jboss.messaging.core.remoting.impl.mina.MinaService;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * @version <tt>$Revision$</tt>
 * 
 */
public class ServerKeepAliveTest extends TestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private MinaService service;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   @Override
   protected void setUp() throws Exception
   {
   }

   @Override
   protected void tearDown() throws Exception
   {
      service.stop();
      service = null;
   }

   public void testKeepAliveWithServerNotResponding() throws Exception
   {
      KeepAliveFactory factory = createMock(KeepAliveFactory.class);

      // server does not send ping
      expect(factory.ping()).andStubReturn(null);
      // no pong -> server is not responding
      expect(factory.pong()).andReturn(null).atLeastOnce();

      replay(factory);
      
      service = new MinaService(TCP, "localhost", PORT, factory);
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put(KEEP_ALIVE_INTERVAL_KEY, Integer.toString(KEEP_ALIVE_INTERVAL));
      parameters.put(KEEP_ALIVE_TIMEOUT_KEY, Integer.toString(KEEP_ALIVE_TIMEOUT));
      service.setParameters(parameters);
      service.start();

      MinaConnector connector = new MinaConnector(service.getLocator());
      final String[] sessionIDNotResponding = new String[1];
      final CountDownLatch latch = new CountDownLatch(1);
 
      connector.setConnectionExceptionListener(new ConnectionExceptionListener()
      {
         public void handleConnectionException(Exception e, String sessionID)
         {
            sessionIDNotResponding[0] = sessionID;
            latch.countDown();
         }
      });
      
      NIOSession session = connector.connect();

      boolean firedKeepAliveNotification = latch.await(KEEP_ALIVE_INTERVAL
            + KEEP_ALIVE_TIMEOUT + 1, SECONDS);
      assertTrue(firedKeepAliveNotification);
      assertEquals(session.getID(), sessionIDNotResponding[0]);
      
      connector.disconnect();
      
      verify(factory);
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}