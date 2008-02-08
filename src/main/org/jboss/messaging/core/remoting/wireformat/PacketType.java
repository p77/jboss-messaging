/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.remoting.wireformat;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>.
 * 
 * @version <tt>$Revision$</tt>
 */
public enum PacketType
{
   // System
   NULL                                ((byte)1),
   TEXT                                ((byte)2),
   BYTES                               ((byte)3),
   PING                                ((byte)4),
   PONG                                ((byte)5),
   
   // Miscellaneous   
   EXCEPTION                           ((byte)10),
   CLOSE                               ((byte)11),
   
   // Server
   CREATECONNECTION                    ((byte)20),
   CREATECONNECTION_RESP               ((byte)21),    
   
   // Connection
   CONN_CREATESESSION                  ((byte)30),
   CONN_CREATESESSION_RESP             ((byte)31),
   CONN_START                          ((byte)32),
   CONN_STOP                           ((byte)33),

   // Session   
   SESS_SETID                          ((byte)40),   
   SESS_CREATECONSUMER                 ((byte)41),
   SESS_CREATECONSUMER_RESP            ((byte)42),   
   SESS_CREATEBROWSER                  ((byte)43),
   SESS_CREATEBROWSER_RESP             ((byte)44),
   SESS_SEND                           ((byte)45),
   SESS_DELIVER                        ((byte)46),  
   SESS_ACKNOWLEDGE                    ((byte)47),
   SESS_RECOVER                        ((byte)48),
   SESS_COMMIT                         ((byte)49),
   SESS_ROLLBACK                       ((byte)50),
   SESS_CANCEL                         ((byte)51),
   SESS_QUEUEQUERY                     ((byte)52),
   SESS_QUEUEQUERY_RESP                ((byte)53),
   SESS_CREATEQUEUE                    ((byte)54),
   SESS_DELETE_QUEUE                   ((byte)55),   
   SESS_ADD_ADDRESS                    ((byte)56),
   SESS_REMOVE_ADDRESS                 ((byte)57),
   SESS_BINDINGQUERY                   ((byte)58),
   SESS_BINDINGQUERY_RESP              ((byte)59),  
   SESS_BROWSER_RESET                  ((byte)60),
   SESS_BROWSER_HASNEXTMESSAGE         ((byte)61),
   SESS_BROWSER_HASNEXTMESSAGE_RESP    ((byte)62),
   SESS_BROWSER_NEXTMESSAGEBLOCK       ((byte)63),
   SESS_BROWSER_NEXTMESSAGEBLOCK_RESP  ((byte)64),
   SESS_BROWSER_NEXTMESSAGE            ((byte)65),
   SESS_BROWSER_NEXTMESSAGE_RESP       ((byte)66),      
   SESS_XA_START                       ((byte)67),
   SESS_XA_END                         ((byte)68),
   SESS_XA_COMMIT                      ((byte)69),
   SESS_XA_PREPARE                     ((byte)70),
   SESS_XA_RESP                        ((byte)71),
   SESS_XA_ROLLBACK                    ((byte)72),
   SESS_XA_JOIN                        ((byte)73),
   SESS_XA_SUSPEND                     ((byte)74),
   SESS_XA_RESUME                      ((byte)75),
   SESS_XA_FORGET                      ((byte)76),
   SESS_XA_INDOUBT_XIDS                ((byte)77),
   SESS_XA_INDOUBT_XIDS_RESP           ((byte)78),
   SESS_XA_SET_TIMEOUT                 ((byte)79),
   SESS_XA_SET_TIMEOUT_RESP            ((byte)80),
   SESS_XA_GET_TIMEOUT                 ((byte)81),
   SESS_XA_GET_TIMEOUT_RESP            ((byte)82),
       
   // Consumer 
   CONS_FLOWTOKEN                      ((byte)90);
   

   private final byte type;

   PacketType(byte type)
   {
      this.type = type;
   }

   public byte byteValue()
   {
      return type;
   }
}
