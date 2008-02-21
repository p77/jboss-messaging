/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting;

import java.io.Serializable;


/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * 
 * @version <tt>$Revision$</tt>
 * 
 */
public class RemotingConfiguration implements Serializable
{
   // Constants -----------------------------------------------------

   private static final long serialVersionUID = 94076009399661407L;

   public static final int DEFAULT_KEEP_ALIVE_INTERVAL = 10; // in seconds
   public static final int DEFAULT_KEEP_ALIVE_TIMEOUT = 5; // in seconds
   public static final int DEFAULT_REQRES_TIMEOUT = 5; // in seconds
   public static final boolean DEFAULT_INVM_DISABLED = false;
   public static final boolean DEFAULT_SSL_ENABLED = false;
   
   // Attributes ----------------------------------------------------

   private TransportType transport;
   private String host;
   private int port;

   private int timeout = DEFAULT_REQRES_TIMEOUT;
   private int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL;
   private int keepAliveTimeout = DEFAULT_KEEP_ALIVE_TIMEOUT;
   private boolean invmDisabled = DEFAULT_INVM_DISABLED;
   private boolean sslEnabled = DEFAULT_SSL_ENABLED;
   private String keyStorePath;
   private String keyStorePassword;
   private String trustStorePath;
   private String trustStorePassword;
   

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // for serialization only
   protected RemotingConfiguration()
   {
   }
  
   public RemotingConfiguration(TransportType transport, String host, int port)
   {
      assert transport != null;
      assert host != null;
      assert port > 0;
      
      this.transport = transport;
      this.host = host;
      this.port = port;
   }
   
   public RemotingConfiguration(RemotingConfiguration other)
   {
      assert other != null;
      
      this.transport = other.transport;
      this.host = other.host;
      this.port = other.port;
      
      this.timeout = other.timeout;
      this.keepAliveInterval = other.keepAliveInterval;
      this.keepAliveTimeout = other.keepAliveTimeout;
      this.invmDisabled = other.invmDisabled;
      this.sslEnabled = other.sslEnabled;
      this.keyStorePath = other.keyStorePath;
      this.keyStorePassword = other.keyStorePassword;
      this.trustStorePath = other.trustStorePath;
      this.trustStorePassword = other.trustStorePassword;
   }

   // Public --------------------------------------------------------

   public TransportType getTransport()
   {
      return transport;
   }

   public String getHost()
   {
      return host;
   }

   public int getPort()
   {
      return port;
   }
   
   public void setKeepAliveInterval(int keepAliveInterval)
   {
      this.keepAliveInterval = keepAliveInterval;
   }

   public int getKeepAliveInterval()
   {
      return keepAliveInterval ;
   }

   public void setKeepAliveTimeout(int keepAliveTimeout)
   {
      this.keepAliveTimeout = keepAliveTimeout;
   }

   public int getKeepAliveTimeout()
   {
      return keepAliveTimeout;
   }
   
   public void setTimeout(int timeout)
   {
      this.timeout = timeout;
   }
   
   public int getTimeout()
   {
      return timeout;
   }

   public void setInvmDisabled(boolean disabled)
   {
      this.invmDisabled = disabled;
   }
   
   public boolean isInvmDisabled()
   {
      return invmDisabled;
   }
   
   public void setSSLEnabled(boolean sslEnabled)
   {
      this.sslEnabled = sslEnabled;
   }
   
   public boolean isSSLEnabled()
   {
      return sslEnabled;
   }

   public String getKeyStorePath()
   {
      return keyStorePath;
   }

   public void setKeyStorePath(String keyStorePath)
   {
      this.keyStorePath = keyStorePath;
   }

   public String getKeyStorePassword()
   {
      return keyStorePassword;
   }

   public void setKeyStorePassword(String keyStorePassword)
   {
      this.keyStorePassword = keyStorePassword;
   }

   public String getTrustStorePath()
   {
      return trustStorePath;
   }

   public void setTrustStorePath(String trustStorePath)
   {
      this.trustStorePath = trustStorePath;
   }

   public String getTrustStorePassword()
   {
      return trustStorePassword;
   }

   public void setTrustStorePassword(String trustStorePassword)
   {
      this.trustStorePassword = trustStorePassword;
   }

   public String getURI()
   {
      StringBuffer buff = new StringBuffer();
      buff.append(transport + "://" + host + ":" + port);
      buff.append("?").append("timeout=").append(timeout);
      buff.append("&").append("keepAliveInterval=").append(keepAliveInterval);
      buff.append("&").append("keepAliveTimeout=").append(keepAliveTimeout);
      buff.append("&").append("invmDisabled=").append(invmDisabled);
      buff.append("&").append("sslEnabled=").append(sslEnabled);
      buff.append("&").append("keyStorePath=").append(keyStorePath);
      buff.append("&").append("trustStorePath=").append(trustStorePath);
      return buff.toString();
   }

   @Override
   public String toString()
   {
      return "RemotingConfiguration[uri=" + getURI() + "]";
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((host == null) ? 0 : host.hashCode());
      result = prime * result + port;
      result = prime * result
            + ((transport == null) ? 0 : transport.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final RemotingConfiguration other = (RemotingConfiguration) obj;
      if (host == null)
      {
         if (other.host != null)
            return false;
      } else if (!host.equals(other.host))
         return false;
      if (port != other.port)
         return false;
      if (transport == null)
      {
         if (other.transport != null)
            return false;
      } else if (!transport.equals(other.transport))
         return false;
      return true;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
