/*
 * JBoss, Home of Professional Open Source Copyright 2005-2008, Red Hat Middleware LLC, and individual contributors by
 * the @authors tag. See the copyright.txt in the distribution for a full listing of individual contributors. This is
 * free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public License along with this software; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.jboss.messaging.core.server.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.messaging.core.config.Configuration;
import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.exception.MessagingException;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.management.ManagementService;
import org.jboss.messaging.core.management.MessagingServerControlMBean;
import org.jboss.messaging.core.paging.PagingManager;
import org.jboss.messaging.core.paging.PagingStoreFactory;
import org.jboss.messaging.core.paging.impl.PagingManagerFactoryNIO;
import org.jboss.messaging.core.paging.impl.PagingManagerImpl;
import org.jboss.messaging.core.persistence.StorageManager;
import org.jboss.messaging.core.postoffice.PostOffice;
import org.jboss.messaging.core.postoffice.impl.PostOfficeImpl;
import org.jboss.messaging.core.remoting.Channel;
import org.jboss.messaging.core.remoting.ChannelHandler;
import org.jboss.messaging.core.remoting.RemotingConnection;
import org.jboss.messaging.core.remoting.RemotingService;
import org.jboss.messaging.core.remoting.impl.ConnectionRegistryImpl;
import org.jboss.messaging.core.remoting.impl.wireformat.CreateSessionResponseMessage;
import org.jboss.messaging.core.remoting.impl.wireformat.ReattachSessionResponseMessage;
import org.jboss.messaging.core.remoting.spi.ConnectorFactory;
import org.jboss.messaging.core.security.JBMSecurityManager;
import org.jboss.messaging.core.security.Role;
import org.jboss.messaging.core.security.SecurityStore;
import org.jboss.messaging.core.security.impl.SecurityStoreImpl;
import org.jboss.messaging.core.server.MessagingServer;
import org.jboss.messaging.core.server.QueueFactory;
import org.jboss.messaging.core.server.ServerSession;
import org.jboss.messaging.core.settings.HierarchicalRepository;
import org.jboss.messaging.core.settings.impl.HierarchicalObjectRepository;
import org.jboss.messaging.core.settings.impl.QueueSettings;
import org.jboss.messaging.core.transaction.ResourceManager;
import org.jboss.messaging.core.transaction.impl.ResourceManagerImpl;
import org.jboss.messaging.core.version.Version;
import org.jboss.messaging.util.ExecutorFactory;
import org.jboss.messaging.util.GroupIdGenerator;
import org.jboss.messaging.util.JBMThreadFactory;
import org.jboss.messaging.util.OrderedExecutorFactory;
import org.jboss.messaging.util.SimpleString;
import org.jboss.messaging.util.SimpleStringIdGenerator;
import org.jboss.messaging.util.VersionLoader;

/**
 * The messaging server implementation
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:ataylor@redhat.com>Andy Taylor</a>
 * @version <tt>$Revision: 3543 $</tt> <p/> $Id: ServerPeer.java 3543 2008-01-07 22:31:58Z clebert.suconic@jboss.com $
 */
public class MessagingServerImpl implements MessagingServer
{
   // Constants
   // ------------------------------------------------------------------------------------

   private static final Logger log = Logger.getLogger(MessagingServerImpl.class);

   // Static
   // ---------------------------------------------------------------------------------------

   // Attributes
   // -----------------------------------------------------------------------------------

   private final Version version;

   private volatile boolean started;

   // wired components

   private SecurityStore securityStore;

   private final HierarchicalRepository<QueueSettings> queueSettingsRepository = new HierarchicalObjectRepository<QueueSettings>();

   private ScheduledExecutorService scheduledExecutor;

   private QueueFactory queueFactory;

   private PagingManager pagingManager;

   private PostOffice postOffice;

   private final ExecutorService asyncDeliveryPool = Executors.newCachedThreadPool(new JBMThreadFactory("JBM-async-session-delivery-threads"));

   private final ExecutorFactory executorFactory = new OrderedExecutorFactory(asyncDeliveryPool);

   private HierarchicalRepository<Set<Role>> securityRepository;

   private ResourceManager resourceManager;

   private MessagingServerControlMBean serverManagement;

   private final ConcurrentMap<String, ServerSession> sessions = new ConcurrentHashMap<String, ServerSession>();

   private ConnectorFactory backupConnectorFactory;

   private Map<String, Object> backupConnectorParams;
   
   // plugins

   private StorageManager storageManager;

   private RemotingService remotingService;

   private JBMSecurityManager securityManager;

   private Configuration configuration;

   private ManagementService managementService;

   private final SimpleStringIdGenerator simpleStringIdGenerator = new GroupIdGenerator(new SimpleString("AutoGroupId-"));

   // Constructors
   // ---------------------------------------------------------------------------------

   public MessagingServerImpl()
   {
      // We need to hard code the version information into a source file

      version = VersionLoader.getVersion();
   }

   // lifecycle methods
   // ----------------------------------------------------------------

   public synchronized void start() throws Exception
   {
      if (started)
      {
         return;
      }

      /*
       * The following components are pluggable on the messaging server: Configuration, StorageManager, RemotingService,
       * SecurityManager and ManagementRegistration They must already be injected by the time the messaging server
       * starts It's up to the user to make sure the pluggable components are started - their lifecycle will not be
       * controlled here
       */

      // We make sure the pluggable components have been injected
      if (configuration == null)
      {
         throw new IllegalStateException("Must inject Configuration before starting MessagingServer");
      }

      if (storageManager == null)
      {
         throw new IllegalStateException("Must inject StorageManager before starting MessagingServer");
      }

      if (remotingService == null)
      {
         throw new IllegalStateException("Must inject RemotingService before starting MessagingServer");
      }

      if (securityManager == null)
      {
         throw new IllegalStateException("Must inject SecurityManager before starting MessagingServer");
      }

      if (managementService == null)
      {
         throw new IllegalStateException("Must inject ManagementRegistration before starting MessagingServer");
      }

      if (!storageManager.isStarted())
      {
         throw new IllegalStateException("StorageManager must be started before MessagingServer is started");
      }

      if (!remotingService.isStarted())
      {
         throw new IllegalStateException("RemotingService must be started before MessagingServer is started");
      }

      // The rest of the components are not pluggable and created and started
      // here

      securityStore = new SecurityStoreImpl(configuration.getSecurityInvalidationInterval(),
                                            configuration.isSecurityEnabled());
      queueSettingsRepository.setDefault(new QueueSettings());
      scheduledExecutor = new ScheduledThreadPoolExecutor(configuration.getScheduledThreadPoolMaxSize(),
                                                          new JBMThreadFactory("JBM-scheduled-threads"));
      queueFactory = new QueueFactoryImpl(scheduledExecutor, queueSettingsRepository);

      PagingStoreFactory storeFactory = new PagingManagerFactoryNIO(configuration.getPagingDirectory());

      pagingManager = new PagingManagerImpl(storeFactory,
                                            storageManager,
                                            queueSettingsRepository,
                                            configuration.getPagingMaxGlobalSizeBytes());

      storeFactory.setPagingManager(pagingManager);

      resourceManager = new ResourceManagerImpl(0);
      postOffice = new PostOfficeImpl(storageManager,
                                      pagingManager,
                                      queueFactory,
                                      managementService,
                                      configuration.isRequireDestinations(),
                                      resourceManager,
                                      configuration.isWildcardRoutingEnabled(),
                                      configuration.isBackup());

      securityRepository = new HierarchicalObjectRepository<Set<Role>>();
      securityRepository.setDefault(new HashSet<Role>());
      securityStore.setSecurityRepository(securityRepository);
      securityStore.setSecurityManager(securityManager);
      serverManagement = managementService.registerServer(postOffice,
                                                          storageManager,
                                                          configuration,
                                                          securityRepository,
                                                          queueSettingsRepository,
                                                          this);

      postOffice.start();

      TransportConfiguration backupConnector = configuration.getBackupConnectorConfiguration();

      if (backupConnector != null)
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         try
         {
            Class<?> clz = loader.loadClass(backupConnector.getFactoryClassName());
            backupConnectorFactory = (ConnectorFactory)clz.newInstance();
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException("Error instantiating interceptor \"" + backupConnector.getFactoryClassName() +
                                                        "\"",
                                               e);
         }
         backupConnectorParams = backupConnector.getParams();
      }
      remotingService.setMessagingServer(this);

      started = true;
   }

   public synchronized void stop() throws Exception
   {
      if (!started)
      {
         return;
      }

      securityStore = null;
      postOffice.stop();
      postOffice = null;
      securityRepository = null;
      securityStore = null;
      queueSettingsRepository.clear();
      scheduledExecutor.shutdown();
      queueFactory = null;
      resourceManager = null;
      serverManagement = null;

      asyncDeliveryPool.shutdown();

      try
      {
         if (!asyncDeliveryPool.awaitTermination(10000, TimeUnit.MILLISECONDS))
         {
            log.warn("Timed out waiting for pool to terminate");
         }
      }
      catch (InterruptedException e)
      {
         // Ignore
      }

      started = false;
   }

   // MessagingServer implementation
   // -----------------------------------------------------------

   // The plugabble components

   public void setConfiguration(final Configuration configuration)
   {
      if (started)
      {
         throw new IllegalStateException("Cannot set configuration when started");
      }

      this.configuration = configuration;
   }

   public Configuration getConfiguration()
   {
      return configuration;
   }

   public void setRemotingService(final RemotingService remotingService)
   {
      if (started)
      {
         throw new IllegalStateException("Cannot set remoting service when started");
      }
      this.remotingService = remotingService;
   }

   public RemotingService getRemotingService()
   {
      return remotingService;
   }

   public void setStorageManager(final StorageManager storageManager)
   {
      if (started)
      {
         throw new IllegalStateException("Cannot set storage manager when started");
      }
      this.storageManager = storageManager;
   }

   public StorageManager getStorageManager()
   {
      return storageManager;
   }

   public void setSecurityManager(final JBMSecurityManager securityManager)
   {
      if (started)
      {
         throw new IllegalStateException("Cannot set security Manager when started");
      }

      this.securityManager = securityManager;
   }

   public JBMSecurityManager getSecurityManager()
   {
      return securityManager;
   }

   public void setManagementService(final ManagementService managementService)
   {
      if (started)
      {
         throw new IllegalStateException("Cannot set management service when started");
      }
      this.managementService = managementService;
   }

   public ManagementService getManagementService()
   {
      return managementService;
   }

   // This is needed for the security deployer
   public HierarchicalRepository<Set<Role>> getSecurityRepository()
   {
      return securityRepository;
   }

   // This is needed for the queue settings deployer
   public HierarchicalRepository<QueueSettings> getQueueSettingsRepository()
   {
      return queueSettingsRepository;
   }

   public Version getVersion()
   {
      return version;
   }

   public boolean isStarted()
   {
      return started;
   }

   public ReattachSessionResponseMessage reattachSession(final RemotingConnection connection,
                                                         final String name,
                                                         final int lastReceivedCommandID) throws Exception
   {
      ServerSession session = sessions.get(name);

      // Need to activate the connection even if session can't be found - since otherwise response
      // will never get back

      connection.activate();

      postOffice.activate();

      configuration.setBackup(false);

      remotingService.setBackup(false);

      if (session == null)
      {
         return new ReattachSessionResponseMessage(-1, true);
      }
      else
      {
         // Reconnect the channel to the new connection
         int serverLastReceivedCommandID = session.transferConnection(connection, lastReceivedCommandID);
                         
         return new ReattachSessionResponseMessage(serverLastReceivedCommandID, false);
      }
   }
   
   public CreateSessionResponseMessage createSession(final String name,
                                                     final long channelID,
                                                     final String username,
                                                     final String password,
                                                     final int incrementingVersion,
                                                     final RemotingConnection connection,
                                                     final boolean autoCommitSends,
                                                     final boolean autoCommitAcks,
                                                     final boolean xa) throws Exception
   {
      if (version.getIncrementingVersion() < incrementingVersion)
      {
         throw new MessagingException(MessagingException.INCOMPATIBLE_CLIENT_SERVER_VERSIONS,
                                      "client not compatible with version: " + version.getFullVersion());
      }

      // Is this comment relevant any more ?

      // Authenticate. Successful autentication will place a new SubjectContext
      // on thread local,
      // which will be used in the authorization process. However, we need to
      // make sure we clean
      // up thread local immediately after we used the information, otherwise
      // some other people
      // security my be screwed up, on account of thread local security stack
      // being corrupted.

      securityStore.authenticate(username, password);
 
      Channel channel = connection.getChannel(channelID,                                             
                                              configuration.getPacketConfirmationBatchSize(),                                             
                                              false);

      final ServerSessionImpl session = new ServerSessionImpl(name,
                                                              channelID,
                                                              username,
                                                              password,
                                                              autoCommitSends,
                                                              autoCommitAcks,
                                                              xa,
                                                              connection,
                                                              storageManager,
                                                              postOffice,
                                                              queueSettingsRepository,
                                                              resourceManager,
                                                              securityStore,
                                                              executorFactory.getExecutor(),
                                                              channel,
                                                              managementService,
                                                              this,
                                                              simpleStringIdGenerator);

      // If the session already exists that's fine - create session must be idempotent
      // This is because if server failures occurring during a create session call we need to
      // retry it on the backup, but the create session might have and might not have been replicated
      // to the backup, so we need to work in both cases
      if (sessions.putIfAbsent(name, session) == null)
      {
         ChannelHandler handler = new ServerSessionPacketHandler(session, channel, storageManager);

         channel.setHandler(handler);

         connection.addFailureListener(session);
      }

      return new CreateSessionResponseMessage(version.getIncrementingVersion(),
                                              configuration.getPacketConfirmationBatchSize());
   }

   public void removeSession(final String name) throws Exception
   {
      sessions.remove(name);
   }

   public RemotingConnection getReplicatingConnection()
   {
      // Note we must always get a new connection each time - since there must
      // be a one to one correspondence
      // between connections to clients and replicating connections, since we
      // need to preserve channel ids
      // before and after failover

      if (backupConnectorFactory != null)
      {
         // TODO don't hardcode ping interval and code timeout
         RemotingConnection replicatingConnection = ConnectionRegistryImpl.instance.getConnectionNoCache(backupConnectorFactory,
                                                                                                         backupConnectorParams,
                                                                                                         -1,
                                                                                                         2000);
         return replicatingConnection;
      }
      else
      {
         return null;
      }
   }
   
   public MessagingServerControlMBean getServerManagement()
   {
      return serverManagement;
   }

   public int getConnectionCount()
   {
      return remotingService.getConnections().size();
   }

   public PostOffice getPostOffice()
   {
      return postOffice;
   }

   // Public
   // ---------------------------------------------------------------------------------------

   // Package protected
   // ----------------------------------------------------------------------------

   // Protected
   // ------------------------------------------------------------------------------------

   // Private
   // --------------------------------------------------------------------------------------

   // Inner classes
   // --------------------------------------------------------------------------------
}
