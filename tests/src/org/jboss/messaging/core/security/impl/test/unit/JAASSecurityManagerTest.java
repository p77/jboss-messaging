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
package org.jboss.messaging.core.security.impl.test.unit;

import junit.framework.TestCase;
import org.jboss.messaging.core.security.impl.JAASSecurityManager;
import org.jboss.messaging.core.security.Role;
import org.jboss.messaging.core.security.CheckType;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.RealmMapping;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

import javax.security.auth.Subject;
import java.util.HashSet;
import java.util.Set;

/**
 * tests the JAASSecurityManager
 * @author <a href="ataylor@redhat.com">Andy Taylor</a>
 */
public class JAASSecurityManagerTest extends TestCase
{
   JAASSecurityManager securityManager;
   protected void setUp() throws Exception
   {
      securityManager = new JAASSecurityManager();
   }

   protected void tearDown() throws Exception
   {
      securityManager = null;
   }

   public void testValidatingUser()
   {
      AuthenticationManager authenticationManager = EasyMock.createStrictMock(AuthenticationManager.class);
      securityManager.setAuthenticationManager(authenticationManager);
      SimplePrincipal principal = new SimplePrincipal("newuser1");
      char[] passwordChars = "newpassword1".toCharArray();
      Subject subject = new Subject();
      EasyMock.expect(authenticationManager.isValid(principal(principal), EasyMock.aryEq(passwordChars), subject(subject))).andReturn(true);
      EasyMock.replay(authenticationManager);
      
      securityManager.validateUser("newuser1", "newpassword1");
   }

   public void testValidatingUserAndRole()
   {
      AuthenticationManager authenticationManager = EasyMock.createStrictMock(AuthenticationManager.class);
      securityManager.setAuthenticationManager(authenticationManager);
      RealmMapping realmMapping = EasyMock.createStrictMock(RealmMapping.class);
      securityManager.setRealmMapping(realmMapping);
      SimplePrincipal principal = new SimplePrincipal("newuser1");
      char[] passwordChars = "newpassword1".toCharArray();
      Subject subject = new Subject();
      EasyMock.expect(authenticationManager.isValid(principal(principal), EasyMock.aryEq(passwordChars), subject(subject))).andReturn(true);
      EasyMock.replay(authenticationManager);
      EasyMock.expect(realmMapping.doesUserHaveRole(principal(principal), EasyMock.isA(Set.class))).andReturn(true);
      EasyMock.replay(realmMapping);
      securityManager.validateUserAndRole("newuser1", "newpassword1", new HashSet<Role>(), CheckType.CREATE );   
   }

   public static SimplePrincipal principal(SimplePrincipal principal)
   {
      EasyMock.reportMatcher(new SimplePrincipalMatcher(principal));
      return principal;
   }

   public static Subject subject(Subject subject)
   {
      EasyMock.reportMatcher(new SubjectMatcher(subject));
      return subject;
   }

   static class SimplePrincipalMatcher implements IArgumentMatcher
   {
      SimplePrincipal principal;

      public SimplePrincipalMatcher(SimplePrincipal principal)
      {
         this.principal = principal;
      }

      public boolean matches(Object o)
      {
         if(o instanceof SimplePrincipal)
         {
            SimplePrincipal that = (SimplePrincipal) o;
            return that.getName().equals(principal.getName());
         }
         return false;
      }

      public void appendTo(StringBuffer stringBuffer)
      {
         stringBuffer.append("Invalid Principal created");
      }
   }

   static class SubjectMatcher implements IArgumentMatcher
   {
      Subject subject;

      public SubjectMatcher(Subject subject)
      {
         this.subject = subject;
      }

      public boolean matches(Object o)
      {
         if(o instanceof Subject)
         {
            Subject that = (Subject) o;
            return true;
         }
         return false;
      }

      public void appendTo(StringBuffer stringBuffer)
      {
         stringBuffer.append("Invalid Subject created");
      }
   }
}