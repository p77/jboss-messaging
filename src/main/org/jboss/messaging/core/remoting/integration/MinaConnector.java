/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.integration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.filter.reqres.RequestResponseFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.jboss.jms.client.remoting.ConsolidatedRemotingConnectionListener;
import org.jboss.logging.Logger;
import org.jboss.messaging.core.remoting.NIOConnector;
import org.jboss.messaging.core.remoting.NIOSession;
import org.jboss.messaging.core.remoting.PacketDispatcher;
import org.jboss.messaging.core.remoting.TransportType;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 *
 * @version <tt>$Revision$</tt>
 *
 */
public class MinaConnector implements NIOConnector
{
   // Constants -----------------------------------------------------

   private final Logger log = Logger.getLogger(MinaConnector.class);

   // Attributes ----------------------------------------------------

   private NioSocketConnector connector;

   private ScheduledExecutorService blockingScheduler;

   private IoSession session;

   private Map<ConsolidatedRemotingConnectionListener, IoServiceListener> listeners = new HashMap<ConsolidatedRemotingConnectionListener, IoServiceListener>();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public MinaConnector() throws Exception
   {
      this.connector = new NioSocketConnector();
      
      MdcInjectionFilter mdcInjectionFilter = new MdcInjectionFilter();
      connector.getFilterChain().addLast("mdc", mdcInjectionFilter);

      connector.getFilterChain().addLast("codec",
            new ProtocolCodecFilter(new PacketCodecFactory()));

      addBlockingRequestResponseFilter(connector.getFilterChain());

      connector.getFilterChain().addLast("logger", new LoggingFilter());

      connector.setHandler(new MinaHandler(PacketDispatcher.client));
      connector.getSessionConfig().setKeepAlive(true);
      connector.getSessionConfig().setReuseAddress(true);
}
   
   /* (non-Javadoc)
    * @see org.jboss.messaging.core.remoting.integration.NIOConnector#connect(java.lang.String, int, org.jboss.messaging.core.remoting.TransportType)
    */
   public NIOSession connect(String host, int port, TransportType transport) throws IOException {
      assert host != null;
      assert port > 0;
      assert transport != null;

      InetSocketAddress address = new InetSocketAddress(host, port);
      ConnectFuture future = connector.connect(address);
      connector.setDefaultRemoteAddress(address);

      future.awaitUninterruptibly();
      if (!future.isConnected())
      {
         throw new IOException("Cannot connect to " + address.toString());
      }
      this.session = future.getSession();
      return new MinaSession(session);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.messaging.core.remoting.integration.NIOConnector#disconnect()
    */
   public boolean disconnect()
   {
      if (session == null)
      {
         return false;
      }

      CloseFuture closeFuture = session.close().awaitUninterruptibly();
      boolean closed = closeFuture.isClosed();

      connector.dispose();
      blockingScheduler.shutdown();

      connector = null;
      blockingScheduler = null;
      session = null;

      return closed;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.messaging.core.remoting.integration.NIOConnector#addConnectionListener(org.jboss.jms.client.remoting.ConsolidatedRemotingConnectionListener)
    */
   public void addConnectionListener(
         final ConsolidatedRemotingConnectionListener listener)
   {
      assert listener != null;
      assert connector != null;

      IoServiceListener ioListener = new IoServiceListenerAdapter(listener);
      connector.addListener(ioListener);
      listeners.put(listener, ioListener);

      if (log.isTraceEnabled())
         log.trace("added listener " + listener + " to " + this);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.messaging.core.remoting.integration.NIOConnector#removeConnectionListener(org.jboss.jms.client.remoting.ConsolidatedRemotingConnectionListener)
    */
   public void removeConnectionListener(ConsolidatedRemotingConnectionListener listener)
   {
      assert listener != null;
      assert connector != null;

      connector.removeListener(listeners.get(listener));
      listeners.remove(listener);

      if (log.isTraceEnabled())
         log.trace("removed listener " + listener + " from " + this);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.messaging.core.remoting.integration.NIOConnector#getServerURI()
    */
   public String getServerURI()
   {
      if (connector == null)
      {
         return null;
      }
      InetSocketAddress address = connector.getDefaultRemoteAddress();
      if (address != null)
      {
         return address.toString();
      } else {
         return null;
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private void addBlockingRequestResponseFilter(
         DefaultIoFilterChainBuilder chain)
   {
      blockingScheduler = Executors.newScheduledThreadPool(1);
      RequestResponseFilter filter = new RequestResponseFilter(
            new MinaInspector(), blockingScheduler);
      chain.addLast("reqres", filter);
   }

   // Inner classes -------------------------------------------------

   private final class IoServiceListenerAdapter implements IoServiceListener
   {
      private final Logger log = Logger
            .getLogger(IoServiceListenerAdapter.class);

      private final ConsolidatedRemotingConnectionListener listener;

      private IoServiceListenerAdapter(
            ConsolidatedRemotingConnectionListener listener)
      {
         this.listener = listener;
      }

      public void serviceActivated(IoService service)
      {
         if (log.isTraceEnabled())
            log.trace("activated " + service);
      }

      public void serviceDeactivated(IoService service)
      {
         if (log.isTraceEnabled())
            log.trace("deactivated " + service);
      }

      public void serviceIdle(IoService service, IdleStatus idleStatus)
      {
         if (log.isTraceEnabled())
            log.trace("idle " + service + ", status=" + idleStatus);
      }

      public void sessionCreated(IoSession session)
      {
         if (log.isInfoEnabled())
            log.info("created session " + session);
      }

      public void sessionDestroyed(IoSession session)
      {
         log.warn("destroyed session " + session);

         Throwable t = new Throwable("MINA session has been destroyed");
         listener.handleConnectionException(t);
      }
   }
}
