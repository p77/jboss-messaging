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
package org.jboss.messaging.tests.performance.journal.impl;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.jboss.messaging.core.asyncio.impl.AsynchronousFileImpl;
import org.jboss.messaging.core.journal.IOCallback;
import org.jboss.messaging.core.journal.Journal;
import org.jboss.messaging.core.journal.PreparedTransactionInfo;
import org.jboss.messaging.core.journal.RecordInfo;
import org.jboss.messaging.core.journal.impl.JournalImpl;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.tests.unit.core.journal.impl.JournalImplTestBase;

/**
 * 
 * A RealJournalImplTest
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public abstract class JournalImplTestUnit extends JournalImplTestBase
{
   private static final Logger log = Logger.getLogger(JournalImplTestUnit.class);
   
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      assertEquals(0, AsynchronousFileImpl.getTotalMaxIO());
   }
   
   public void testAddUpdateDeleteManyLargeFileSize() throws Exception
   {
      final int numberAdds = 10000;
      
      final int numberUpdates = 5000;
      
      final int numberDeletes = 3000;
                  
      long[] adds = new long[numberAdds];
      
      for (int i = 0; i < numberAdds; i++)
      {
         adds[i] = i;
      }
      
      long[] updates = new long[numberUpdates];
      
      for (int i = 0; i < numberUpdates; i++)
      {
         updates[i] = i;
      }
      
      long[] deletes = new long[numberDeletes];
      
      for (int i = 0; i < numberDeletes; i++)
      {
         deletes[i] = i;
      }
      
      // This would take a long time with sync=true, and still validates the file. 
      setup(10, 10 * 1024 * 1024, false);
      createJournal();
      startJournal();
      load();
      add(adds);
      update(updates);
      delete(deletes);
      stopJournal();
      createJournal();
      startJournal();
      loadAndCheck();
      
   }
   
   public void testAddUpdateDeleteManySmallFileSize() throws Exception
   {
      final int numberAdds = 10000;
      
      final int numberUpdates = 5000;
      
      final int numberDeletes = 3000;
                  
      long[] adds = new long[numberAdds];
      
      for (int i = 0; i < numberAdds; i++)
      {
         adds[i] = i;
      }
      
      long[] updates = new long[numberUpdates];
      
      for (int i = 0; i < numberUpdates; i++)
      {
         updates[i] = i;
      }
      
      long[] deletes = new long[numberDeletes];
      
      for (int i = 0; i < numberDeletes; i++)
      {
         deletes[i] = i;
      }
      
      setup(10, 10 * 1024, false);
      createJournal();
      startJournal();
      load();
      add(adds);
      update(updates);
      delete(deletes);

      log.info("Debug journal:" + debugJournal());
      stopJournal(false);
      createJournal();
      startJournal();
      loadAndCheck();
      
   }
   
   public void testReclaimAndReload() throws Exception
   {
      setup(2, 10 * 1024 * 1024, false);
      createJournal();
      startJournal();
      load();
      
      journal.startReclaimer();
      
      long start = System.currentTimeMillis();
      
                  
      byte[] record = generateRecord(recordLength);
      
      int NUMBER_OF_RECORDS = 1000;

      for (int count = 0; count < NUMBER_OF_RECORDS; count++)
      {
         journal.appendAddRecord(count, (byte)0, record);
         
         if (count >= NUMBER_OF_RECORDS / 2)
         {
            journal.appendDeleteRecord(count - NUMBER_OF_RECORDS / 2);
         }
         
         if (count % 100 == 0)
         {
            log.info("Done: " + count);
         }
      }
      
      long end = System.currentTimeMillis();
      
      double rate = 1000 * ((double)NUMBER_OF_RECORDS) / (end - start);
      
      log.info("Rate of " + rate + " adds/removes per sec");
      
      log.info("Reclaim status = " + debugJournal());
               
      stopJournal();
      createJournal();
      startJournal();
      journal.load(new ArrayList<RecordInfo>(), new ArrayList<PreparedTransactionInfo>());
      
      assertEquals(NUMBER_OF_RECORDS / 2, journal.getIDMapSize());
      
      stopJournal();
   }
   
   public void testSpeedNonTransactional() throws Exception
   {
      for (int i=0;i<1;i++)
      {
         this.setUp();
         System.gc(); Thread.sleep(500);
         internaltestSpeedNonTransactional();
         this.tearDown();
      }
   }
   
   public void internaltestSpeedNonTransactional() throws Exception
   {      
      final long numMessages = 10000;
      
      int numFiles =  (int)(((numMessages * 1024 + 512) / (10 * 1024 * 1024)) * 1.3);
      
      if (numFiles<2) numFiles = 2;
      
      log.info("num Files=" + numFiles);

      Journal journal =
         new JournalImpl(10 * 1024 * 1024,  numFiles, true, true, getFileFactory(),
               5000, "jbm-data", "jbm", 5000, 120);
      
      journal.start();
      
      journal.load(new ArrayList<RecordInfo>(), null);
      

      final CountDownLatch latch = new CountDownLatch((int)numMessages);
      
      
      class LocalCallback implements IOCallback
      {

         int i=0;
         String message = null;
         boolean done = false;
         CountDownLatch latch;
         
         public LocalCallback(int i, CountDownLatch latch)
         {
            this.i = i;
            this.latch = latch;
         }
         public void done()
         {
            synchronized (this)
            {
               if (done)
               {
                  message = "done received in duplicate";
               }
               done = true;
               this.latch.countDown();
            }
         }

         public void onError(int errorCode, String errorMessage)
         {
            synchronized (this)
            {
               System.out.println("********************** Error = " + (i++));
               message = errorMessage;
               latch.countDown();
            }
         }
         
      }
      
      
      log.info("Adding data");
      byte[] data = new byte[700];
      
      long start = System.currentTimeMillis();
      
      for (int i = 0; i < numMessages; i++)
      {
         journal.appendAddRecord(i, (byte)0, data);
      }
      
      long end = System.currentTimeMillis();
      
      double rate = 1000 * (double)numMessages / (end - start);
      
      boolean failed = false;
      
      // If this fails it is probably because JournalImpl it is closing the files without waiting all the completes to arrive first
      assertFalse(failed);
      
      
      log.info("Rate " + rate + " records/sec");

      journal.stop();
      
      journal =
         new JournalImpl(10 * 1024 * 1024,  numFiles, true, true, getFileFactory(),
               5000, "jbm-data", "jbm", 5000, 120);
      
      journal.start();
      journal.load(new ArrayList<RecordInfo>(), null);
      journal.stop();
      
   }
   
   public void testSpeedTransactional() throws Exception
   {
      Journal journal =
         new JournalImpl(10 * 1024 * 1024, 10, true, true, getFileFactory(),
               5000, "jbm-data", "jbm", 5000, 120);
      
      journal.start();
      
      journal.load(new ArrayList<RecordInfo>(), null);
      
      try
      {
         final int numMessages = 50050;
         
         byte[] data = new byte[1024];
         
         long start = System.currentTimeMillis();
         
         int count = 0;
         double rates[] = new double[50];
         for (int i = 0; i < 50; i++)
         {
            long startTrans = System.currentTimeMillis();
            for (int j=0; j<1000; j++)
            {
               journal.appendAddRecordTransactional(i, (byte)0, count++, data);
            }
            
            journal.appendCommitRecord(i);
            
            long endTrans = System.currentTimeMillis();
   
            rates[i] = 1000 * (double)1000 / (endTrans - startTrans);
         }
         
         long end = System.currentTimeMillis();
         
         for (double rate: rates)
         {
            log.info("Transaction Rate = " + rate + " records/sec");
            
         }
         
         double rate = 1000 * (double)numMessages / (end - start);
         
         log.info("Rate " + rate + " records/sec");
      }
      finally
      {
         journal.stop();
      }

   }
   
   
}

