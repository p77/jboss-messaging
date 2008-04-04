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
package org.jboss.messaging.core.security.impl;

import org.jboss.messaging.core.security.JBMSecurityManager;
import org.jboss.messaging.core.security.Role;
import org.jboss.messaging.core.security.CheckType;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.exception.MessagingException;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.AuthenticationManager;

import javax.security.auth.Subject;
import javax.naming.InitialContext;
import java.util.HashSet;
import java.util.Set;
import java.security.Principal;

/**
 * This implementation delegates to the a real JAAS Authentication Manager and will typically be used within an appserver
 * and it up via jndi.
 *
 * @author <a href="ataylor@redhat.com">Andy Taylor</a>
 */
public class JAASSecurityManager implements JBMSecurityManager
{
   private static final Logger log = Logger.getLogger(JAASSecurityManager.class);

   // Static --------------------------------------------------------

   // Attributes ----------------------------------------------------

   private boolean trace = log.isTraceEnabled();

   /**
    * the realmmapping
    */
   RealmMapping realmMapping;

   /**
    * the JAAS Authentication Manager
    */
   AuthenticationManager authenticationManager;

   /**
    * The JNDI name of the AuthenticationManager(and RealmMapping since they are the same object).
    */
   private String securityDomainName = "java:/jaas/messaging";

   public boolean validateUser(String user, String password)
   {
      SimplePrincipal principal = new SimplePrincipal(user);

      char[] passwordChars = null;

      if (password != null)
      {
         passwordChars = password.toCharArray();
      }

      Subject subject = new Subject();

      return authenticationManager.isValid(principal, passwordChars, subject);
   }

   public boolean validateUserAndRole(String user, String password, HashSet<Role> roles, CheckType checkType)
   {
      SimplePrincipal principal = user == null? null:new SimplePrincipal(user);

      char[] passwordChars = null;

      if (password != null)
      {
         passwordChars = password.toCharArray();
      }

      Subject subject = new Subject();

      boolean authenticated =  authenticationManager.isValid(principal, passwordChars, subject);
      // Authenticate. Successful authentication will place a new SubjectContext on thread local,
      // which will be used in the authorization process. However, we need to make sure we clean up
      // thread local immediately after we used the information, otherwise some other people
      // security my be screwed up, on account of thread local security stack being corrupted.
      if(authenticated)
      {
         SecurityActions.pushSubjectContext(principal, passwordChars, subject);
         Set rolePrincipals = getRolePrincipals(checkType, roles);

         authenticated = realmMapping.doesUserHaveRole(principal, rolePrincipals);

         if (trace) { log.trace("user " + user + (authenticated ? " is " : " is NOT ") + "authorized"); }
         SecurityActions.popSubjectContext();
      }
      return authenticated;
   }

   private Set getRolePrincipals(CheckType checkType, HashSet<Role> roles)
   {
      Set<SimplePrincipal> principals = new HashSet<SimplePrincipal>();
      for (Role role : roles)
      {
         if((checkType.equals(CheckType.CREATE) && role.isCreate()) ||
                 (checkType.equals(CheckType.WRITE) && role.isWrite()) ||
                 (checkType.equals(CheckType.READ) && role.isRead()))
         {
            principals.add(new SimplePrincipal(role.getName()));
         }
      }
      return principals;
   }

   public void setRealmMapping(RealmMapping realmMapping)
   {
      this.realmMapping = realmMapping;
   }

   public void setAuthenticationManager(AuthenticationManager authenticationManager)
   {
      this.authenticationManager = authenticationManager;
   }

   /**
    * lifecycle method, needs to be called
    * @throws Exception
    */
   public void start() throws Exception
   {
      InitialContext ic = new InitialContext();
      authenticationManager = (AuthenticationManager)ic.lookup(securityDomainName);
      realmMapping = (RealmMapping) authenticationManager;
   }

   public void setSecurityDomainName(String securityDomainName)
   {
      this.securityDomainName = securityDomainName;
   }
}