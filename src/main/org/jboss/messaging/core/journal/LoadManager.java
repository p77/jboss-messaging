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

package org.jboss.messaging.core.journal;

/**
 * 
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 * 
 */
public interface LoadManager
{
   void addRecord(RecordInfo info);
   
   void deleteRecord(long id);
   
   void updateRecord(RecordInfo info);
   
   void addPreparedTransaction(PreparedTransactionInfo preparedTransaction);
   
   /** 
    * 
    * This may happen in a rare situation where a transaction commit timed out on AIO,
    * And right after that a rollback was fired but the previous transaction was completed when the TransactionCallback was already forgotten.
    * 
    * This is because libaio's forget method is not working, so we have to come up with this "hack"
    * 
    * */
   void restart();
}
