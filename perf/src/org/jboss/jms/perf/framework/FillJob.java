/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.perf.framework;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.jboss.jms.perf.framework.factories.MessageFactory;
import org.jboss.logging.Logger;

/**
 * 
 * A FillJob.
 * 
 * @author <a href="tim.fox@jboss.com">Tim Fox</a>
 * @version $Revision$
 *
 * $Id$
 */
public class FillJob extends BaseJob
{
   private static final long serialVersionUID = 339586193389055268L;
   
   private static final Logger log = Logger.getLogger(FillJob.class);

   public static final String TYPE = "FILL";
   
   protected int numMessages;
   
   protected int deliveryMode;
   
   protected int msgSize;
   
   protected MessageFactory mf;

   public String getType()
   {
      return TYPE;
   }
   
   public ThroughputResult execute() throws PerfException
   {           
      Connection conn = null;
      
      try
      {
         log.info("==============Running fill job");
         
         conn = cf.createConnection();
         
         long count = 0;

         Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         
         MessageProducer prod = sess.createProducer(destination);
         prod.setDeliveryMode(deliveryMode);
         
         for (int i = 0; i < numMessages; i++)
         {
            Message m = mf.getMessage(sess, msgSize);
            prod.send(m);
            count++;
         }
         
         log.info("==========================Finished running job");
         
         return null;
      }
      catch (Exception e)
      {
         log.error("Failed to fill", e);
         throw new PerfException("Failed to fill", e);
      }
      finally
      {
         try
         {
            if (conn != null)
            {
               conn.close();          
            }
         }
         catch (Exception e)
         {
            log.error("Failed to close", e);
            throw new PerfException("Failed to close", e);
         }
      }
      
   }

   public FillJob()
   {
      super();
   }
   
   public FillJob(Properties jndiProperties, String destName, String connectionFactoryJndiName,
         int numMessages, int messageSize, MessageFactory mf,
         int deliveryMode)
   {
      super(jndiProperties, destName, connectionFactoryJndiName);
      this.numMessages = numMessages;
      this.mf = mf;
      this.deliveryMode = deliveryMode;
      
   }
   
   
   /**
    * Set the deliveryMode.
    * 
    * @param deliveryMode The deliveryMode to set.
    */
   public void setDeliveryMode(int deliveryMode)
   {
      this.deliveryMode = deliveryMode;
   }
   
   
   
   /**
    * Set the mf.
    * 
    * @param mf The mf to set.
    */
   public void setMf(MessageFactory mf)
   {
      this.mf = mf;
   }
   
   
   
   /**
    * Set the msgSize.
    * 
    * @param msgSize The msgSize to set.
    */
   public void setMsgSize(int msgSize)
   {
      this.msgSize = msgSize;
   }
   
   
   
   /**
    * Set the numMessages.
    * 
    * @param numMessages The numMessages to set.
    */
   public void setNumMessages(int numMessages)
   {
      this.numMessages = numMessages;
   }
   
}