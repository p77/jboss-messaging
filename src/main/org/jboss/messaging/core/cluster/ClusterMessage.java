package org.jboss.messaging.core.cluster;

/**
 * 
 * A ClusterRequest
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public interface ClusterMessage
{
   void execute();
}