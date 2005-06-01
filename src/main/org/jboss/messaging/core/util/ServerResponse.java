/**
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.util;

import org.jgroups.Address;

import java.io.Serializable;

/**
 * A wrapper around a response coming from a <i>single</i> server delegate registered with
 * a RpcServer. Returned by the rpcServerCalls.
 *
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 */
public class ServerResponse
{
   // Attributes ----------------------------------------------------

   protected Address address;
   protected Serializable category;
   protected Serializable serverDelegateID;
   protected Object result;

   // Constructors --------------------------------------------------

   public ServerResponse(Address address, Serializable category,
                         Serializable subServerID, Object result)
   {
      this.address = address;
      this.category = category;
      this.serverDelegateID = subServerID;
      this.result = result;
   }

   // Public --------------------------------------------------------

   /**
    * Can be null.
    */
   public Address getAddress()
   {
      return address;
   }

   public Serializable getCategory()
   {
      return category;
   }

   public Serializable getServerDelegateID()
   {
      return serverDelegateID;
   }


   /**
    * Return the result as it was returned by the remote sub-server (it can be null), or a
    * Throwable, if the remote invocation generated an exception.
    *
    * @return - the result, null or a Throwable.
    */
   public Object getInvocationResult()
   {
      return result;
   }


   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(RpcServer.serverDelegateToString(address, category, serverDelegateID));
      sb.append(" result: ");
      if (result == null)
      {
         sb.append(result);
      }
      else
      {
         sb.append(result.getClass().getName());
      }
      return sb.toString();
   }
}
