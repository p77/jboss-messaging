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
package org.jboss.test.messaging;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;

import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.security.Role;
import org.jboss.messaging.core.server.MessagingServer;
import org.jboss.messaging.jms.server.JMSServerManager;
import org.jboss.test.messaging.tools.ServerManagement;
import org.jboss.test.messaging.tools.container.Server;
import org.jboss.test.messaging.util.ProxyAssertSupport;

/**
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @author <a href="mailto:tim.fox@jboss.org">Tim Fox</a>
 * @author <a href="ataylor@redhat.com">Andy Taylor</a>
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 */
public class JBMServerTestCase extends ProxyAssertSupport
{
   // Constants -----------------------------------------------------

   public final static int MAX_TIMEOUT = 1000 * 10 /* seconds */;

   public final static int MIN_TIMEOUT = 1000 * 1 /* seconds */;

   protected final Logger log = Logger.getLogger(getClass());
   
   // Static --------------------------------------------------------
   
   /** Some testcases are time sensitive, and we need to make sure a GC would happen before certain scenarios*/
   public static void forceGC()
   {
      WeakReference<Object> dumbReference = new WeakReference<Object>(new Object());
      // A loop that will wait GC, using the minimal time as possible
      while (dumbReference.get() != null)
      {
         System.gc();
         try
         {
            Thread.sleep(500);
         } catch (InterruptedException e)
         {
         }
      }
   }
   
   // Attributes ----------------------------------------------------

   protected static List<Server> servers = new ArrayList<Server>();

   protected static Topic topic1;

   protected static Topic topic2;

   protected static Topic topic3;

   protected static Queue queue1;

   protected static Queue queue2;

   protected static Queue queue3;

   protected static Queue queue4;

   protected void setUp() throws Exception
   {
      super.setUp();
      
      System.setProperty("java.naming.factory.initial", getContextFactory());

      String banner =
         "####################################################### Start " +
         " test: " + getName();

      log.info(banner);


      try
      {
         //create any new server we need
         servers.add(ServerManagement.create(0));

         //start the servers if needed
         boolean started = false;
         try
         {
            started = servers.get(0).isStarted();
         }
         catch (Exception e)
         {
            //ignore, incase its a remote server
         }
         if (!started)
         {
            //                  try
            //                  {
            servers.get(0).start(getContainerConfig(), getConfiguration(), true);
            //                  }
            //                  catch (Exception e)
            //                  {
            //                     //if we are remote we will need recreating if we get here
            //                     servers.set(i, ServerManagement.create(i));
            //                     servers.get(i).start(getContainerConfig(), getConfiguration(), getClearDatabase() && i == 0);
            //                  }
         }
         //deploy the objects for this test
         deployAdministeredObjects(0);
         lookUp();
      }
      catch (Exception e)
      {
         //if we get here we need to clean up for the next test
         e.printStackTrace();
         servers.get(0).stop();
         throw e;
      }
      //empty the queues
      checkEmpty(queue1);
      checkEmpty(queue2);
      checkEmpty(queue3);
      checkEmpty(queue4);

      // Check no subscriptions left lying around

      checkNoSubscriptions(topic1);
      checkNoSubscriptions(topic2);
      checkNoSubscriptions(topic3);
   }

   public void stop() throws Exception
   {
      servers.get(0).stop();
   }

   public String getContextFactory()
   {
      return "org.jboss.test.messaging.tools.container.InVMInitialContextFactory";
   }

   public void start() throws Exception
   {
      System.setProperty("java.naming.factory.initial", getContextFactory());
         servers.get(0).start(getContainerConfig(), getConfiguration(), false);
      //deployAdministeredObjects();
   }
   
   public void startNoDelete() throws Exception
   {
      System.setProperty("java.naming.factory.initial", getContextFactory());
      servers.get(0).start(getContainerConfig(), getConfiguration(), false);
      //deployAdministeredObjects();
   }

   public void stopServerPeer() throws Exception
   {
      servers.get(0).stopServerPeer();
   }

   public void startServerPeer() throws Exception
   {
      System.setProperty("java.naming.factory.initial", getContextFactory());
      servers.get(0).startServerPeer(0);
      //deployAdministeredObjects();
   }

   protected HashMap<String, Object> getConfiguration()
   {
      return new HashMap<String, Object>();
   }

   protected void deployAndLookupAdministeredObjects() throws Exception
   {
      createTopic("Topic1");
      createTopic("Topic2");
      createTopic("Topic3");
      createQueue("Queue1");
      createQueue("Queue2");
      createQueue("Queue3");
      createQueue("Queue4");

      lookUp();
   }

   protected void deployAdministeredObjects(int i) throws Exception
   {
      createTopic("Topic1", i);
      createTopic("Topic2", i);
      createTopic("Topic3", i);
      createQueue("Queue1", i);
      createQueue("Queue2", i);
      createQueue("Queue3", i);
      createQueue("Queue4", i);
   }

   private void lookUp()
           throws Exception
   {
      InitialContext ic = getInitialContext();
      topic1 = (Topic) ic.lookup("/topic/Topic1");
      topic2 = (Topic) ic.lookup("/topic/Topic2");
      topic3 = (Topic) ic.lookup("/topic/Topic3");
      queue1 = (Queue) ic.lookup("/queue/Queue1");
      queue2 = (Queue) ic.lookup("/queue/Queue2");
      queue3 = (Queue) ic.lookup("/queue/Queue3");
      queue4 = (Queue) ic.lookup("/queue/Queue4");
   }

   protected void undeployAdministeredObjects() throws Exception
   {
      removeAllMessages("Topic1", false);
      removeAllMessages("Topic2", false);
      removeAllMessages("Topic3", false);
      removeAllMessages("Queue1", true);
      removeAllMessages("Queue2", true);
      removeAllMessages("Queue3", true);
      removeAllMessages("Queue4", true);

      destroyTopic("Topic1");
      destroyTopic("Topic2");
      destroyTopic("Topic3");
      destroyQueue("Queue1");
      destroyQueue("Queue2");
      destroyQueue("Queue3");
      destroyQueue("Queue4");
   }


   public String[] getContainerConfig()
   {
         return new String[]{ "invm-beans.xml", "jbm-jboss-beans.xml"};
   }

   protected MessagingServer getJmsServer() throws Exception
   {
      return servers.get(0).getMessagingServer();
   }

   protected JMSServerManager getJmsServerManager() throws Exception
   {
      return servers.get(0).getJMSServerManager();
   }
   /*protected void tearDown() throws Exception
   {
      super.tearDown();
      //undeployAdministeredObjects();
      for (int i = 0; i < getServerCount(); i++)
      {
         servers.get(i).stopServerPeer();
      }
   }*/

   protected void checkNoSubscriptions(Topic topic) throws Exception
   {

   }

   protected void checkNoSubscriptions(Topic topic, int server) throws Exception
   {

   }

   protected void drainDestination(ConnectionFactory cf, Destination dest) throws Exception
   {
      Connection conn = null;
      try
      {
         conn = cf.createConnection();
         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageConsumer cons = sess.createConsumer(dest);
         Message m = null;
         conn.start();
         log.trace("Draining messages from " + dest);
         while (true)
         {
            m = cons.receive(500);
            if (m == null) break;
            log.trace("Drained message");
         }
      }
      finally
      {
         if (conn!= null) conn.close();
      }
   }

   public InitialContext getInitialContext() throws Exception
   {
      return getInitialContext(0);
   }

   public ConnectionFactory getConnectionFactory() throws Exception
   {
      return (ConnectionFactory) getInitialContext().lookup("/ConnectionFactory");
   }

   public TopicConnectionFactory getTopicConnectionFactory() throws Exception
   {
      return (TopicConnectionFactory) getInitialContext().lookup("/ConnectionFactory");
   }

   public XAConnectionFactory getXAConnectionFactory() throws Exception
   {
      return (XAConnectionFactory) getInitialContext().lookup("/ConnectionFactory");
   }
   
   public InitialContext getInitialContext(int serverid) throws Exception
   {
      return new InitialContext(ServerManagement.getJNDIEnvironment(serverid));
   }

   protected TransactionManager getTransactionManager()
   {
      return new TransactionManagerImple();
   }


   public void configureSecurityForDestination(String destName, boolean isQueue, HashSet<Role> roles) throws Exception
   {
      servers.get(0).configureSecurityForDestination(destName, isQueue, roles);
   }
   
   public void createQueue(String name) throws Exception
   {
      servers.get(0).createQueue(name, null);
   }
   
   public void createTopic(String name) throws Exception
   {
      servers.get(0).createTopic(name, null);
   }
   
   public void destroyQueue(String name) throws Exception
   {
      servers.get(0).destroyQueue(name, null);
   }
   
   public void destroyTopic(String name) throws Exception
   {
      servers.get(0).destroyTopic(name, null);
   }
   
   
   public void createQueue(String name, int i) throws Exception
   {     
      servers.get(i).createQueue(name, null);      
   }
   
   public void createTopic(String name, int i) throws Exception
   {
      servers.get(i).createTopic(name, null);      
   }
   
   public void destroyQueue(String name, int i) throws Exception
   {
      servers.get(i).destroyQueue(name, null);      
   }

//   public void deployQueue(String name) throws Exception
//   {
//      for (int i = 0; i < getServerCount(); i++)
//      {
//         servers.get(i).deployQueue(name, null, i != 0);
//      }
//   }
//
//   public void deployQueue(String name, String jndiName) throws Exception
//   {
//      for (int i = 0; i < getServerCount(); i++)
//      {
//         servers.get(i).deployQueue(name, jndiName, i != 0);
//      }
//   }
//
//   public void deployQueue(String name, int server) throws Exception
//   {
//      servers.get(server).deployQueue(name, null, true);
//
//   }
//
//   public void deployQueue(String name, int fullSize, int pageSize, int downCacheSize) throws Exception
//   {
//      for (int i = 0; i < getServerCount(); i++)
//      {
//         servers.get(i).deployQueue(name, null, fullSize, pageSize, downCacheSize, i != 0);
//      }
//   }
//
//   public void deployQueue(String name, String jndiName, int fullSize, int pageSize,
//                           int downCacheSize, int serverIndex, boolean manageConfirmations) throws Exception
//   {
//      servers.get(serverIndex).deployQueue(name, jndiName, fullSize, pageSize, downCacheSize, manageConfirmations);
//
//   }
//
//   public void deployTopic(String name) throws Exception
//   {
//      for (int i = 0; i < getServerCount(); i++)
//      {
//         servers.get(i).deployTopic(name, null, i != 0);
//      }
//   }
//
//   public void deployTopic(String name, String jndiName) throws Exception
//   {
//      for (int i = 0; i < getServerCount(); i++)
//      {
//         servers.get(i).deployTopic(name, jndiName, i != 0);
//      }
//   }
//
//   public void deployTopic(String name, int server) throws Exception
//   {
//      servers.get(server).deployTopic(name, null, server != 0);
//
//   }
//
//   public void deployTopic(String name, int fullSize, int pageSize, int downCacheSize) throws Exception
//   {
//      for (int i = 0; i < getServerCount(); i++)
//      {
//         servers.get(i).deployTopic(name, null, fullSize, pageSize, downCacheSize, i != 0);
//      }
//   }
//
//   public void deployTopic(String name, String jndiName, int fullSize, int pageSize,
//                           int downCacheSize, int serverIndex, boolean manageConfirmations) throws Exception
//   {
//      servers.get(serverIndex).deployTopic(name, jndiName, fullSize, pageSize, downCacheSize, manageConfirmations);
//
//   }
//
//   public void undeployQueue(String name) throws Exception
//   {
//      for (int i = 0; i < getServerCount(); i++)
//      {
//         try
//         {
//            servers.get(i).undeployDestination(true, name);
//         }
//         catch (Exception e)
//         {
//            log.info("did not undeploy " + name);
//         }
//      }
//   }
//
//   public void undeployQueue(String name, int server) throws Exception
//   {
//      servers.get(server).undeployDestination(true, name);
//
//   }
//
//   public void undeployTopic(String name) throws Exception
//   {
//      for (int i = 0; i < getServerCount(); i++)
//      {
//         try
//         {
//            servers.get(i).undeployDestination(false, name);
//         }
//         catch (Exception e)
//         {
//            log.info("did not undeploy " + name);
//         }
//      }
//   }
//
//   public void undeployTopic(String name, int server) throws Exception
//   {
//      servers.get(server).undeployDestination(false, name);
//
//   }
//
//   public static boolean destroyTopic(String name) throws Exception
//   {
//      return servers.get(0).undeployDestinationProgrammatically(false, name);
//   }
//
//   public static boolean destroyQueue(String name) throws Exception
//   {
//      return servers.get(0).undeployDestinationProgrammatically(true, name);
//   }
//

   public boolean checkNoMessageData()
   {
      return false;
   }

   public boolean checkEmpty(Queue queue) throws Exception
   {
      Integer messageCount = servers.get(0).getMessageCountForQueue(queue.getQueueName());
      if (messageCount > 0)
      {
         removeAllMessages(queue.getQueueName(), true);
      }
      return true;
   }

   public boolean checkEmpty(Queue queue, int i)
   {
      return true;
   }

   public boolean checkEmpty(Topic topic)
   {
      return true;
   }

   public boolean checkEmpty(Topic topic, int i)
   {
      return true;
   }

   protected void removeAllMessages(String destName, boolean isQueue) throws Exception
   {
      try
      {
         removeAllMessages(destName, isQueue, 0);
      }
      catch (Exception e)
      {
         log.info("did not clear messages for " + destName);
      }
   }

   protected void removeAllMessages(String destName, boolean isQueue, int server) throws Exception
   {
      servers.get(server).removeAllMessages(destName, isQueue);
   }

   protected boolean assertRemainingMessages(int expected) throws Exception
   {
      Integer messageCount = servers.get(0).getMessageCountForQueue("Queue1");

      assertEquals(expected, messageCount.intValue());
      return expected == messageCount.intValue();
   }

   protected static void assertActiveConnectionsOnTheServer(int expectedSize)
   throws Exception
   {
      assertEquals(expectedSize, servers.get(0).getMessagingServer().getServerManagement().getConnectionCount());
   }

   public static void deployConnectionFactory(String clientId, String objectName,
                                              List<String> jndiBindings)
           throws Exception
   {
      servers.get(0).deployConnectionFactory(clientId, objectName, jndiBindings);
   }

   public static void deployConnectionFactory(String objectName,
                                              List<String> jndiBindings,
                                              int prefetchSize)
           throws Exception
   {
      servers.get(0).deployConnectionFactory(objectName, jndiBindings, prefetchSize);
   }

   public static void deployConnectionFactory(String objectName,
                                              List<String> jndiBindings)
           throws Exception
   {
      servers.get(0).deployConnectionFactory(objectName, jndiBindings);
   }

   public static void deployConnectionFactory(int server,
                                              String objectName,
                                              List<String> jndiBindings,
                                              int prefetchSize)
           throws Exception
   {
      servers.get(server).deployConnectionFactory(objectName, jndiBindings, prefetchSize);
   }

   public static void deployConnectionFactory(int server,
                                              String objectName,
                                              List<String> jndiBindings)
           throws Exception
   {
      servers.get(server).deployConnectionFactory(objectName, jndiBindings);
   }

   public void deployConnectionFactory(String clientId,
                                       String objectName,
                                       List<String> jndiBindings,
                                       int prefetchSize,
                                       int defaultTempQueueFullSize,
                                       int defaultTempQueuePageSize,
                                       int defaultTempQueueDownCacheSize,
                                       boolean supportsFailover,
                                       boolean supportsLoadBalancing,              
                                       int dupsOkBatchSize,
                                       boolean blockOnAcknowledge) throws Exception
   {
      servers.get(0).deployConnectionFactory(clientId, objectName, jndiBindings, prefetchSize, defaultTempQueueFullSize,
              defaultTempQueuePageSize, defaultTempQueueDownCacheSize, supportsFailover, supportsLoadBalancing, dupsOkBatchSize, blockOnAcknowledge);
   }

   public static void deployConnectionFactory(String objectName,
                                              List<String> jndiBindings,
                                              int prefetchSize,
                                              int defaultTempQueueFullSize,
                                              int defaultTempQueuePageSize,
                                              int defaultTempQueueDownCacheSize)
           throws Exception
   {
      servers.get(0).deployConnectionFactory(objectName,
              jndiBindings,
              prefetchSize,
              defaultTempQueueFullSize,
              defaultTempQueuePageSize,
              defaultTempQueueDownCacheSize);
   }

   public static void undeployConnectionFactory(String objectName) throws Exception
   {
      servers.get(0).undeployConnectionFactory(objectName);
   }

   public static void undeployConnectionFactory(int server, String objectName) throws Exception
   {
      servers.get(server).undeployConnectionFactory(objectName);
   }

   protected List<String> listAllSubscribersForTopic(String s) throws Exception
   {
      return servers.get(0).listAllSubscribersForTopic(s);
   }

   protected Integer getMessageCountForQueue(String s) throws Exception
   {
      return servers.get(0).getMessageCountForQueue(s);
   }

   protected Integer getMessageCountForQueue(String s, int server) throws Exception
   {
      return servers.get(server).getMessageCountForQueue(s);
   }

   protected Set<Role> getSecurityConfig() throws Exception
   {
      return servers.get(0).getSecurityConfig();
   }

   protected void setSecurityConfig(Set<Role> defConfig) throws Exception
   {
      servers.get(0).setSecurityConfig(defConfig);
   }

   protected void setSecurityConfigOnManager(boolean b, String s, HashSet<Role> lockedConf) throws Exception
   {
      servers.get(0).configureSecurityForDestination(s, b, lockedConf);
   }

   protected void kill(int i) throws Exception
   {
      log.info("Attempting to kill server " + i);

      if (i == 0)
      {
         //Cannot kill server 0 if there are any other servers since it has the rmi registry in it
         for (int j = 1; j < servers.size(); j++)
         {
            if (servers.get(j) != null)
            {
               throw new IllegalStateException("Cannot kill server 0, since server[" + j + "] still exists");
            }
         }
      }

      if (i >= servers.size())
      {
         log.info("server " + i + " has not been created or has already been killed, so it cannot be killed");
      }
      else
      {
         Server server = servers.get(i);
         log.info("invoking kill() on server " + i);
         try
         {
            server.kill();
         }
         catch (Throwable t)
         {
            // This is likely to throw an exception since the server dies before the response is received
         }

         log.info("Waiting for server to die");

         try
         {
            while (true)
            {
               server.ping();
               log.debug("server " + i + " still alive ...");
               Thread.sleep(100);
            }
         }
         catch (Throwable e)
         {
            //Ok
         }

         Thread.sleep(300);

         log.info("server " + i + " killed and dead");
      }
   }   
}
