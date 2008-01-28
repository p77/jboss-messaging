/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.impl.mina;

import static org.apache.mina.filter.keepalive.KeepAlivePolicy.EXCEPTION;
import static org.apache.mina.filter.logging.LogLevel.TRACE;
import static org.apache.mina.filter.logging.LogLevel.WARN;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.filter.reqres.RequestResponseFilter;
import org.jboss.messaging.core.remoting.KeepAliveFactory;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * @version <tt>$Revision$</tt>
 * 
 */
public class FilterChainSupport
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public static void addKeepAliveFilter(DefaultIoFilterChainBuilder filterChain,
         KeepAliveFactory factory, int keepAliveInterval, int keepAliveTimeout)
   {
      assert filterChain != null;
      assert factory != null;
      
      if (keepAliveTimeout > keepAliveInterval)
      {
         throw new IllegalArgumentException("timeout must be greater than the interval: "
               + "keepAliveTimeout= " + keepAliveTimeout
               + ", keepAliveInterval=" + keepAliveInterval);
      }

      filterChain.addLast("keep-alive", new KeepAliveFilter(
            new MinaKeepAliveFactory(factory), EXCEPTION, keepAliveInterval,
            keepAliveTimeout));
   }

   // Package protected ---------------------------------------------

   static void addCodecFilter(DefaultIoFilterChainBuilder filterChain)
   {
      assert filterChain != null;

      filterChain.addLast("codec", new ProtocolCodecFilter(
            new PacketCodecFactory()));
   }

   static void addMDCFilter(DefaultIoFilterChainBuilder filterChain)
   {
      assert filterChain != null;

      MdcInjectionFilter mdcInjectionFilter = new MdcInjectionFilter();
      filterChain.addLast("mdc", mdcInjectionFilter);
   }

   static void addLoggingFilter(DefaultIoFilterChainBuilder filterChain)
   {
      assert filterChain != null;

      LoggingFilter filter = new LoggingFilter();

      filter.setSessionCreatedLogLevel(TRACE);
      filter.setSessionOpenedLogLevel(TRACE);
      filter.setSessionIdleLogLevel(TRACE);
      filter.setSessionClosedLogLevel(TRACE);

      filter.setMessageReceivedLogLevel(TRACE);
      filter.setMessageSentLogLevel(TRACE);

      filter.setExceptionCaughtLogLevel(WARN);

      filterChain.addLast("logger", filter);
   }

   static void addExecutorFilter(DefaultIoFilterChainBuilder filterChain)
   {
      ExecutorFilter executorFilter = new ExecutorFilter();
      filterChain.addLast("executor", executorFilter);
   }

   static ScheduledExecutorService addBlockingRequestResponseFilter(
         DefaultIoFilterChainBuilder filterChain)
   {
      assert filterChain != null;

      ScheduledExecutorService executorService = Executors
            .newScheduledThreadPool(1);
      RequestResponseFilter filter = new RequestResponseFilter(
            new MinaInspector(), executorService);
      filterChain.addLast("reqres", filter);

      return executorService;
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
