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
package org.jboss.messaging.tests.unit.core.postoffice.impl;

import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.jboss.messaging.core.filter.Filter;
import org.jboss.messaging.core.persistence.StorageManager;
import org.jboss.messaging.core.postoffice.Binding;
import org.jboss.messaging.core.postoffice.PostOffice;
import org.jboss.messaging.core.postoffice.impl.BindingImpl;
import org.jboss.messaging.core.postoffice.impl.PostOfficeImpl;
import org.jboss.messaging.core.server.Queue;
import org.jboss.messaging.core.server.QueueFactory;
import org.jboss.messaging.core.server.ServerMessage;
import org.jboss.messaging.tests.unit.core.server.impl.fakes.FakeQueueFactory;
import org.jboss.messaging.tests.util.UnitTestCase;
import org.jboss.messaging.util.SimpleString;

/**
 * 
 * A PostOfficeTest
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class PostOfficeTest extends UnitTestCase
{
	private QueueFactory queueFactory = new FakeQueueFactory();
   
   public void testAddQueue() throws Exception
   {
      StorageManager pm = EasyMock.createStrictMock(StorageManager.class);
      
      QueueFactory qf = EasyMock.createStrictMock(QueueFactory.class);
      
      PostOffice po = new PostOfficeImpl(pm, qf, false);
      
      final long id = 324;
      final SimpleString name = new SimpleString("wibb22");
      final Filter filter = new FakeFilter();
      final boolean durable = true;
      final boolean temporary = true;
      
      Queue queue = queueFactory.createQueue(id, name, filter, durable, temporary);
      
      EasyMock.expect(qf.createQueue(-1, name, filter, durable, temporary)).andReturn(queue);
            
      final SimpleString condition = new SimpleString("queue.wibble");

      Binding expected = new BindingImpl(condition, queue);
      
      pm.addBinding(EasyMock.eq(expected));
      
      EasyMock.replay(qf);
      
      EasyMock.replay(pm);
      
      po.addBinding(condition, name, filter, durable, temporary);
      
      EasyMock.verify(qf);
      
      EasyMock.verify(pm);
      
      EasyMock.reset(qf);
      
      EasyMock.reset(pm);
      
      final boolean durable2 = false;
      
      queue = queueFactory.createQueue(id, name, filter, durable2, temporary);
      
      EasyMock.expect(qf.createQueue(-1, name, filter, durable2, temporary)).andReturn(queue);
      
      EasyMock.replay(qf);
      
      EasyMock.replay(pm);      
   }   
   
   public void testRemoveQueue() throws Exception
   {
      StorageManager pm = EasyMock.createStrictMock(StorageManager.class);
            
      QueueFactory qf = EasyMock.createStrictMock(QueueFactory.class);
      
      PostOffice po = new PostOfficeImpl(pm, qf, false);
      
      final long id = 324;
      final SimpleString name = new SimpleString("wibb22");
      final Filter filter = new FakeFilter();
      final boolean durable = true;
      final boolean temporary = true;
      
      Queue queue = queueFactory.createQueue(id, name, filter, durable, temporary);
      
      EasyMock.expect(qf.createQueue(-1, name, filter, durable, temporary)).andReturn(queue);
            
      final SimpleString condition = new SimpleString("queue.wibble");
 
      Binding expected = new BindingImpl(condition, queue);
      
      pm.addBinding(EasyMock.eq(expected));
      
      pm.deleteBinding(EasyMock.eq(expected));
      
      EasyMock.replay(qf);
      
      EasyMock.replay(pm);
      
      po.addBinding(condition, name, filter, durable, temporary);
      
      po.removeBinding(name);
      
      EasyMock.verify(qf);
      
      EasyMock.verify(pm);
      
      EasyMock.reset(qf);
      
      EasyMock.reset(pm);
      
      final boolean durable2 = false;
      
      queue = queueFactory.createQueue(id, name, filter, durable2, temporary);
      
      EasyMock.expect(qf.createQueue(-1, name, filter, durable2, temporary)).andReturn(queue);
      
      EasyMock.replay(qf);
      
      EasyMock.replay(pm);
      
      po.addBinding(condition, name, filter, durable2, temporary);
      
      po.removeBinding(name);
      
      EasyMock.verify(qf);
      
      EasyMock.verify(pm);
   }   
   
   public void testAddRemoveMultipleWithDifferentConditions() throws Exception
   {
      StorageManager pm = EasyMock.createStrictMock(StorageManager.class);
            
      QueueFactory qf = new FakeQueueFactory();
      
      PostOffice po = new PostOfficeImpl(pm, qf, false);
      
      final SimpleString condition1 = new SimpleString("queue.wibble");      
                
      SimpleString squeue1 = new SimpleString("queue1");
      SimpleString squeue2 = new SimpleString("queue2");
      SimpleString squeue3 = new SimpleString("queue3");
      SimpleString squeue4 = new SimpleString("queue4");
      SimpleString squeue5 = new SimpleString("queue5");
      SimpleString squeue6 = new SimpleString("queue6");
      
      po.addBinding(condition1, squeue1, null, false, false);      
      Map<SimpleString, List<Binding>> mappings = po.getMappings();      
      assertEquals(1, mappings.size());
      
      po.addBinding(condition1, squeue2, null, false, false);     
      mappings = po.getMappings();      
      assertEquals(1, mappings.size());
      
      po.addBinding(condition1, squeue3, null, false, false); 
      mappings = po.getMappings();      
      assertEquals(1, mappings.size());
      
      List<Binding> bindings = mappings.get(condition1);
      assertNotNull(bindings);
      assertEquals(3, bindings.size());
      
      Binding binding1 = bindings.get(0);
      Queue queue1 = binding1.getQueue();
      assertEquals(squeue1, queue1.getName());
            
      Binding binding2 = bindings.get(1);
      Queue queue2 = binding2.getQueue();
      assertEquals(squeue2, queue2.getName());
      
      Binding binding3 = bindings.get(2);
      Queue queue3 = binding3.getQueue();
      assertEquals(squeue3, queue3.getName());
      
      final SimpleString condition2 = new SimpleString("queue.wibble2"); 
      
      po.addBinding(condition2, squeue4, null, false, false);       
      mappings = po.getMappings();      
      assertEquals(2, mappings.size());
      
      po.addBinding(condition2, squeue5, null, false, false); 
      mappings = po.getMappings();      
      assertEquals(2, mappings.size());
      
      final SimpleString condition3 = new SimpleString("topic.wibblexyz"); 
      
      po.addBinding(condition3, squeue6, null, false, false);       
      mappings = po.getMappings();      
      assertEquals(3, mappings.size());
      
      po.removeBinding(squeue6);
      mappings = po.getMappings();      
      assertEquals(2, mappings.size());
      
      po.removeBinding(squeue4);
      mappings = po.getMappings();      
      assertEquals(2, mappings.size());
      
      po.removeBinding(squeue5);
      mappings = po.getMappings();      
      assertEquals(1, mappings.size());
      
      po.removeBinding(squeue1);
      mappings = po.getMappings();      
      assertEquals(1, mappings.size());
      
      po.removeBinding(squeue2);
      mappings = po.getMappings();      
      assertEquals(1, mappings.size());
      
      po.removeBinding(squeue3);
      mappings = po.getMappings();      
      assertEquals(0, mappings.size());      
   }

   class FakeFilter implements Filter
   {
		public SimpleString getFilterString()
		{
			return new SimpleString("aardvark");
		}

		public boolean match(ServerMessage message)
		{
			return true;
		}
   	
   }
}
