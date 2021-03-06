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

package org.jboss.messaging.tests.timing.core.journal.impl;

import java.util.ArrayList;

import org.jboss.messaging.core.asyncio.impl.AsynchronousFileImpl;
import org.jboss.messaging.core.journal.PreparedTransactionInfo;
import org.jboss.messaging.core.journal.RecordInfo;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.tests.unit.core.journal.impl.JournalImplTestBase;
import org.jboss.messaging.tests.unit.core.journal.impl.fakes.ByteArrayEncoding;

/**
 * 
 * A RealJournalImplTest
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
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
      
      setup(10, 10 * 1024 * 1024, true);
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
      
      setup(10, 10 * 1024, true);
      createJournal();
      startJournal();
      load();
      add(adds);
      update(updates);
      delete(deletes);

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
      
      long start = System.currentTimeMillis();
      
                  
      byte[] record = generateRecord(recordLength);
      
      int NUMBER_OF_RECORDS = 1000;

      for (int count = 0; count < NUMBER_OF_RECORDS; count++)
      {
         journal.appendAddRecord(count, (byte)0, new ByteArrayEncoding(record));
         
         if (count >= NUMBER_OF_RECORDS / 2)
         {
            journal.appendDeleteRecord(count - NUMBER_OF_RECORDS / 2);
         }
         
         if (count % 100 == 0)
         {
            log.debug("Done: " + count);
         }
      }
      
      long end = System.currentTimeMillis();
      
      double rate = 1000 * ((double)NUMBER_OF_RECORDS) / (end - start);
      
      log.debug("Rate of " + rate + " adds/removes per sec");
      
      log.debug("Reclaim status = " + debugJournal());
               
      stopJournal();
      createJournal();
      startJournal();
      journal.load(new ArrayList<RecordInfo>(), new ArrayList<PreparedTransactionInfo>());
      
      assertEquals(NUMBER_OF_RECORDS / 2, journal.getIDMapSize());
      
      stopJournal();
   }
   
   
}


