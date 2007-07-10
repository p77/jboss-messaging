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
package org.jboss.test.messaging.jms.clustering;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jboss.test.messaging.tools.ServerManagement;

/**
 * A test where we kill multiple nodes and make sure the failover works correctly in these condtions
 * too.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MultipleFailoverTest extends ClusteringTestBase
{
   // Constants ------------------------------------------------------------------------------------

   // Static ---------------------------------------------------------------------------------------

   // Attributes -----------------------------------------------------------------------------------

   // Constructors ---------------------------------------------------------------------------------

   public MultipleFailoverTest(String name)
   {
      super(name);
   }

   // Public ---------------------------------------------------------------------------------------

   public void testAllKindsOfServerFailures() throws Exception
   {
      Connection conn = null;
      TextMessage m = null;
      MessageProducer prod = null;
      MessageConsumer cons = null;

      try
      {
         // we start with a cluster of two (server 0 and server 1)

         conn = this.createConnectionOnServer(cf, 0);
         conn.start();

         // send/receive message
         Session s = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         prod = s.createProducer(queue[0]);
         cons = s.createConsumer(queue[0]);
         prod.send(s.createTextMessage("step1"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step1", m.getText());

         log.info("killing node 0 ....");

         ServerManagement.kill(0);

         log.info("########");
         log.info("######## KILLED NODE 0");
         log.info("########");

         // send/receive message
         prod.send(s.createTextMessage("step2"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step2", m.getText());

         log.info("########");
         log.info("######## STARTING NODE 2");
         log.info("########");

         ServerManagement.start(2, "all", false);
         ServerManagement.deployQueue("testDistributedQueue", 2);

         // send/receive message
         prod.send(s.createTextMessage("step3"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step3", m.getText());

         log.info("killing node 1 ....");

         ServerManagement.kill(1);

         log.info("########");
         log.info("######## KILLED NODE 1");
         log.info("########");

         // send/receive message
         prod.send(s.createTextMessage("step4"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step4", m.getText());

         log.info("########");
         log.info("######## STARTING NODE 3");
         log.info("########");

         ServerManagement.start(3, "all", false);
         log.info("deploying queue on3");
         ServerManagement.deployQueue("testDistributedQueue", 3);
         log.info("deployed it");

         // send/receive message
         prod.send(s.createTextMessage("step5"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step5", m.getText());

         log.info("killing node 2 ....");

         ServerManagement.kill(2);

         log.info("########");
         log.info("######## KILLED NODE 2");
         log.info("########");

         // send/receive message
         prod.send(s.createTextMessage("step6"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step6", m.getText());

         log.info("########");
         log.info("######## STARTING NODE 0");
         log.info("########");

         ServerManagement.start(0, "all", false);
         ServerManagement.deployQueue("testDistributedQueue", 0);

         // send/receive message
         prod.send(s.createTextMessage("step7"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step7", m.getText());

         log.info("killing node 3 ....");

         ServerManagement.kill(3);

         log.info("########");
         log.info("######## KILLED NODE 3");
         log.info("########");

         // send/receive message
         prod.send(s.createTextMessage("step8"));
         m = (TextMessage)cons.receive();
         assertNotNull(m);
         assertEquals("step8", m.getText());
         
         log.info("Got to the end");

      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }
   }
   
   
   
   public void testFailoverFloodTwoServers() throws Exception
   {
      Connection conn = null;

      try
      {
         conn = this.createConnectionOnServer(cf, 0);

         Session sessSend = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         Session sessCons = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         MessageConsumer cons = sessCons.createConsumer(queue[0]);

         MyListener list = new MyListener();

         cons.setMessageListener(list);

         conn.start();

         MessageProducer prod = sessSend.createProducer(queue[0]);

         prod.setDeliveryMode(DeliveryMode.PERSISTENT);

         int count = 0;
         
         Killer killer = new Killer();
         
         Thread t = new Thread(killer);
         
         t.start();
         
         while (!killer.isDone())
         {
            TextMessage tm = sessSend.createTextMessage("message " + count);
            tm.setIntProperty("cnt", count);

            prod.send(tm);
            
            if (count % 100 == 0)
            {
               log.info("sent " + count);
            }

            count++;
         }
              
         t.join();
         
         if (killer.failed)
         {
            fail();
         }
         
         //We check that we received all the message
         //we allow for duplicates, see http://jira.jboss.org/jira/browse/JBMESSAGING-604
         
         conn.close();
         conn = null;
         
         if (!list.waitFor(count))
         {
         	fail("Timed out waiting for message");
         }
                  
         count = 0;
         Iterator iter = list.msgs.iterator();
         while (iter.hasNext())
         {
            Integer i = (Integer)iter.next();
            
            if (i.intValue() != count)
            {
               fail("Missing message " + i);
            }
            count++;
         }
         
         if (list.failed)
         {
            fail();
         }
      }
      catch (Exception e)
      {
         log.error("Failed", e);
         throw e;
      }
      finally
      {
         if (!ServerManagement.isStarted(0))
         {
            ServerManagement.start(0, "all", false);
         }
         
         
         if (conn != null)
         {
            log.info("closing connection");
            try
            {
               conn.close();
            }
            catch (Exception ignore)
            {
            }
            log.info("closed connection");
         }
      }
   }

   // Package protected ----------------------------------------------------------------------------

   // Protected ------------------------------------------------------------------------------------

   protected void setUp() throws Exception
   {
      nodeCount = 2;

      super.setUp();

      log.debug("setup done");
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   // Private --------------------------------------------------------------------------------------

   // Inner classes --------------------------------------------------------------------------------

   class Killer implements Runnable
   { 
      volatile boolean failed;
      
      volatile boolean done;
      
      public boolean isDone()
      {
         return done;
      }
      
      public void run()
      {
         try
         {                                     
            Thread.sleep(10000);
               
            log.info("Killing server 0");
            ServerManagement.kill(0);
            
            Thread.sleep(5000);
            
            log.info("starting server 0");
            ServerManagement.start(0, "all", false);
            ServerManagement.deployQueue("testDistributedQueue", 0);
            
            Thread.sleep(5000);
            
            log.info("Killing server 1");
            ServerManagement.kill(1);
            
            Thread.sleep(5000);
            
            log.info("Starting server 1");
            ServerManagement.start(1, "all", false);
            ServerManagement.deployQueue("testDistributedQueue", 1);
            
            Thread.sleep(5000);
            
            log.info("Killing server 0");
            ServerManagement.kill(0);
            
            Thread.sleep(5000);
            
            log.info("Starting server 0");
            ServerManagement.start(0, "all", false);
            ServerManagement.deployQueue("testDistributedQueue", 0);
            
            Thread.sleep(5000);
            
            log.info("Killing server 1");
            ServerManagement.kill(1);
            
            Thread.sleep(5000);
            
            log.info("Starting server 1");
            ServerManagement.start(1, "all", false);
            ServerManagement.deployQueue("testDistributedQueue", 1);
            
            Thread.sleep(5000);
            
            log.info("Killing server 0");
            ServerManagement.kill(0);
            
            Thread.sleep(5000);
            
            log.info("Starting server 0");
            ServerManagement.start(0, "all", false);
            ServerManagement.deployQueue("testDistributedQueue", 0);
            
            log.info("killer DONE");
         }
         catch (Exception e)
         {               
            failed = true;
         }
         
         done = true;
      }
      
   }
   
   class MyListener implements MessageListener
   {
      int count = 0;
          
      volatile boolean failed;
      
      Set msgs = new TreeSet();
      
      int maxcnt = 0;
      
      private Object obj = new Object();
      
      boolean waitFor(int i)
      {
      	synchronized (obj)
      	{
      		long toWait = 30000;
      		while (maxcnt < i && toWait > 0)
      		{
      			long start = System.currentTimeMillis();
      			try
      			{      				
      				obj.wait(30000);
      			}
      			catch (InterruptedException e)
      			{}
      			if (i <= maxcnt)
      			{
      				toWait -= System.currentTimeMillis() - start;
      			}            			      
      		}
      		return maxcnt < i;
      	}
      }
   
      public void onMessage(Message msg)
      {
         try
         {
            TextMessage tm = (TextMessage)msg;
            
            if (count % 100 == 0)
            {
               log.info("Received message " + tm.getText() + " (" + tm + ")");
            }
            
            count++;
            
            /*
            
            IMPORTANT NOTE 
            
            http://jira.jboss.org/jira/browse/JBMESSAGING-604
             
            There will always be the possibility that duplicate messages can be received until
            we implement duplicate message detection.
            Consider the following possibility:
            A message is sent the send succeeds on the server
            The message is delivered to the client and acked.
            The ack removes it from the server
            The server then fails *before* the original send message has written its response
            to the socket
            The client receives a socket exception
            Failover kicks in
            After failover the client resumes the send
            The message gets delivered again
            And yes, this was actually seen to happen in the logs :)
            
            Therefore we only count that the total messages were received
            */      
            
            int cnt = msg.getIntProperty("cnt");
            
            msgs.add(new Integer(cnt));
            
            maxcnt = Math.max(maxcnt, cnt);
            synchronized (obj)
            {
            	obj.notify();
            }                        
         }
         catch (Exception e)
         {
            log.error("Failed to receive", e);
            failed = true;
         }
      }
      
   }
}
