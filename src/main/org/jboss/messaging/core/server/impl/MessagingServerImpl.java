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
package org.jboss.messaging.core.server.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.jboss.logging.Logger;
import org.jboss.messaging.core.deployers.impl.QueueSettingsDeployer;
import org.jboss.messaging.core.deployers.impl.SecurityDeployer;
import org.jboss.messaging.core.deployers.impl.FileDeploymentManager;
import org.jboss.messaging.core.deployers.DeploymentManager;
import org.jboss.messaging.core.deployers.Deployer;
import org.jboss.messaging.core.filter.Filter;
import org.jboss.messaging.core.memory.MemoryManager;
import org.jboss.messaging.core.memory.impl.SimpleMemoryManager;
import org.jboss.messaging.core.message.MessageReference;
import org.jboss.messaging.core.messagecounter.MessageCounterManager;
import org.jboss.messaging.core.persistence.PersistenceManager;
import org.jboss.messaging.core.persistence.impl.nullpm.NullPersistenceManager;
import org.jboss.messaging.core.postoffice.Binding;
import org.jboss.messaging.core.postoffice.PostOffice;
import org.jboss.messaging.core.postoffice.impl.PostOfficeImpl;
import org.jboss.messaging.core.remoting.Interceptor;
import org.jboss.messaging.core.remoting.RemotingService;
import org.jboss.messaging.core.remoting.impl.RemotingConfiguration;
import org.jboss.messaging.core.remoting.impl.mina.MinaService;
import org.jboss.messaging.core.remoting.impl.wireformat.CreateConnectionResponse;
import org.jboss.messaging.core.security.Role;
import org.jboss.messaging.core.security.SecurityStore;
import org.jboss.messaging.core.security.impl.NullAuthenticationManager;
import org.jboss.messaging.core.security.impl.SecurityStoreImpl;
import org.jboss.messaging.core.server.Configuration;
import org.jboss.messaging.core.server.ConnectionManager;
import org.jboss.messaging.core.server.MessagingServer;
import org.jboss.messaging.core.server.Queue;
import org.jboss.messaging.core.server.QueueFactory;
import org.jboss.messaging.core.server.ServerConnection;
import org.jboss.messaging.core.settings.HierarchicalRepository;
import org.jboss.messaging.core.settings.impl.HierarchicalObjectRepository;
import org.jboss.messaging.core.settings.impl.QueueSettings;
import org.jboss.messaging.core.transaction.ResourceManager;
import org.jboss.messaging.core.transaction.impl.ResourceManagerImpl;
import org.jboss.messaging.core.version.Version;
import org.jboss.messaging.core.version.impl.VersionImpl;
import org.jboss.security.AuthenticationManager;

/**
 * A Messaging Server
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @author <a href="mailto:ataylor@redhat.com>Andy Taylor</a>
 * @version <tt>$Revision: 3543 $</tt>
 *          <p/>
 *          $Id: ServerPeer.java 3543 2008-01-07 22:31:58Z clebert.suconic@jboss.com $
 */
public class MessagingServerImpl implements MessagingServer
{
   // Constants ------------------------------------------------------------------------------------

   private static final Logger log = Logger.getLogger(MessagingServerImpl.class);

   // Static ---------------------------------------------------------------------------------------

   // Attributes -----------------------------------------------------------------------------------

   private Version version;

   private volatile boolean started;

   // wired components

   private SecurityStoreImpl securityStore;
   private ConnectionManagerImpl connectionManager;
   private MemoryManager memoryManager = new SimpleMemoryManager();
   private MessageCounterManager messageCounterManager;
   private PostOffice postOffice;
   private Deployer securityDeployer;
   private Deployer queueSettingsDeployer;
   private AuthenticationManager authenticationManager = new NullAuthenticationManager();
   private DeploymentManager deploymentManager = new FileDeploymentManager();

   // plugins

   private PersistenceManager persistenceManager = new NullPersistenceManager();


   private RemotingService remotingService;
   private boolean createTransport = false;

   private Configuration configuration = new Configuration();
   private HierarchicalRepository<HashSet<Role>> securityRepository = new HierarchicalObjectRepository<HashSet<Role>>();
   private HierarchicalRepository<QueueSettings> queueSettingsRepository = new HierarchicalObjectRepository<QueueSettings>();
   private QueueFactory queueFactory;
   private ResourceManager resourceManager = new ResourceManagerImpl(0);
   private ScheduledExecutorService scheduledExecutor;

   // Constructors ---------------------------------------------------------------------------------
   /**
    * typically called by the MC framework or embedded if the user want to create and start their own RemotingService
    */
   public MessagingServerImpl()
   {
      //We need to hard code the version information into a source file

      version = new VersionImpl("Stilton", 2, 0, 0, 100, "alpha1");

      started = false;
   }

   /**
    * called when the usewr wants the MessagingServer to handle the creation of the RemotingTransport
    *
    * @param remotingConfiguration the RemotingConfiguration
    */
   public MessagingServerImpl(RemotingConfiguration remotingConfiguration)
   {
      this();
      createTransport = true;
      remotingService = new MinaService(remotingConfiguration);
   }
   // lifecycle methods ----------------------------------------------------------------

   public synchronized void start() throws Exception
   {
      log.debug("starting MessagingServer");

      if (started)
      {
         return;
      }

      if (configuration.getMessagingServerID() < 0)
      {
         throw new IllegalStateException("MessagingServer ID not set");
      }

      log.debug(this + " starting");

      // Create the wired components

      securityStore = new SecurityStoreImpl(configuration.getSecurityInvalidationInterval());
      securityRepository.setDefault(new HashSet<Role>());
      securityStore.setSecurityRepository(securityRepository);
      securityStore.setAuthenticationManager(authenticationManager);
      securityDeployer = new SecurityDeployer(securityRepository);
      queueSettingsRepository.setDefault(new QueueSettings());
      scheduledExecutor = new ScheduledThreadPoolExecutor(configuration.getScheduledThreadPoolMaxSize());
      queueFactory = new QueueFactoryImpl(queueSettingsRepository, scheduledExecutor);
      connectionManager = new ConnectionManagerImpl();
      memoryManager = new SimpleMemoryManager();
      messageCounterManager = new MessageCounterManager(configuration.getMessageCounterSamplePeriod());
      configuration.addPropertyChangeListener(new PropertyChangeListener()
      {
         public void propertyChange(PropertyChangeEvent evt)
         {
            if (evt.getPropertyName().equals("messageCounterSamplePeriod"))
               messageCounterManager.reschedule(configuration.getMessageCounterSamplePeriod());
         }
      });
      postOffice = new PostOfficeImpl(configuration.getMessagingServerID(),
              persistenceManager, queueFactory, configuration.isStrictTck());
      queueSettingsDeployer = new QueueSettingsDeployer(postOffice, queueSettingsRepository);

      if (createTransport)
      {
         remotingService.start();
      }
      // Start the wired components
      securityDeployer.start();
      connectionManager.start();
      remotingService.addFailureListener(connectionManager);
      memoryManager.start();
      postOffice.start();
      deploymentManager.start();
      deploymentManager.registerDeployer(securityDeployer);
      deploymentManager.registerDeployer(queueSettingsDeployer);
      MessagingServerPacketHandler serverPacketHandler = new MessagingServerPacketHandler(this);
      getRemotingService().getDispatcher().register(serverPacketHandler);

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      for (String interceptorClass : configuration.getDefaultInterceptors())
      {
         try
         {
            Class clazz = loader.loadClass(interceptorClass);
            getRemotingService().addInterceptor((Interceptor) clazz.newInstance());
         }
         catch (Exception e)
         {
            log.warn("Error instantiating interceptor \"" + interceptorClass + "\"", e);
         }
      }

      started = true;
   }

   public synchronized void stop() throws Exception
   {
      if (!started)
      {
         return;
      }

      log.info(this + " is Stopping. NOTE! Stopping the server peer cleanly will NOT cause failover to occur");

      started = false;

      // Stop the wired components
      securityDeployer.stop();
      queueSettingsDeployer.stop();
      deploymentManager.stop();
      connectionManager.stop();
      remotingService.removeFailureListener(connectionManager);
      connectionManager = null;
      memoryManager.stop();
      memoryManager = null;
      messageCounterManager.stop();
      messageCounterManager = null;
      postOffice.stop();
      postOffice = null;
      scheduledExecutor.shutdown();
      scheduledExecutor = null;
      if (createTransport)
      {
         remotingService.stop();
      }
   }

   // MessagingServer implementation -----------------------------------------------------------

   public Version getVersion()
   {
      return version;
   }

   public Configuration getConfiguration()
   {
      return configuration;
   }

   public boolean isStarted()
   {
      return started;
   }

   public void setConfiguration(Configuration configuration)
   {
      this.configuration = configuration;
   }

   public void setRemotingService(RemotingService remotingService)
   {
      this.remotingService = remotingService;
   }

   public RemotingService getRemotingService()
   {
      return remotingService;
   }

   public DeploymentManager getDeploymentManager()
   {
      return deploymentManager;
   }



   public void createQueue(String address, String name) throws Exception
   {
      if (postOffice.getBinding(name) == null)
      {
         postOffice.addBinding(address, name, null, true, false);
      }

      if (!postOffice.containsAllowableAddress(address))
      {
         postOffice.addAllowableAddress(address);
      }
   }

   public ConnectionManager getConnectionManager()
   {
      return connectionManager;
   }

   public PersistenceManager getPersistenceManager()
   {
      return persistenceManager;
   }

   public void setPersistenceManager(PersistenceManager persistenceManager)
   {
      this.persistenceManager = persistenceManager;
   }

   public PostOffice getPostOffice()
   {
      return postOffice;
   }

   public void setPostOffice(PostOffice postOffice)
   {
      this.postOffice = postOffice;
   }

   public HierarchicalRepository<HashSet<Role>> getSecurityRepository()
   {
      return securityRepository;
   }

   public HierarchicalRepository<QueueSettings> getQueueSettingsRepository()
   {
      return queueSettingsRepository;
   }
   
   public SecurityStore getSecurityStore()
   {
   	return securityStore;
   }

   public void setAuthenticationManager(AuthenticationManager authenticationManager)
   {
      this.authenticationManager = authenticationManager;
   }

   public String toString()
   {
      return "MessagingServer[" + configuration.getMessagingServerID() + "]";
   }

   public CreateConnectionResponse createConnection(final String username, final String password,
                                                    final String remotingClientSessionID, final String clientVMID,
                                                    final int prefetchSize, final String clientAddress)
      throws Exception
   {
      log.trace("creating a new connection for user " + username);

      // Authenticate. Successful autentication will place a new SubjectContext on thread local,
      // which will be used in the authorization process. However, we need to make sure we clean
      // up thread local immediately after we used the information, otherwise some other people
      // security my be screwed up, on account of thread local security stack being corrupted.

      securityStore.authenticate(username, password);
      
      final ServerConnection connection =
         new ServerConnectionImpl(username, password,
                          remotingClientSessionID, clientVMID, clientAddress,
                          prefetchSize, remotingService.getDispatcher(), resourceManager, persistenceManager,
                          postOffice, securityStore, connectionManager);

      remotingService.getDispatcher().register(new ServerConnectionPacketHandler(connection));

      return new CreateConnectionResponse(connection.getID());
   }

   // Public ---------------------------------------------------------------------------------------

   // Package protected ----------------------------------------------------------------------------

   // Protected ------------------------------------------------------------------------------------

   // Private --------------------------------------------------------------------------------------

}