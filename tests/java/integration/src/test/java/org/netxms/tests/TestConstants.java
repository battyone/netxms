package org.netxms.tests;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.netxms.client.NXCSession;

/******************************************************************
 *Documentation about test server configuration
 ******************************************************************
 *Test servers ID should be set as LOCAL_NODE_ID
 *
 *Test server agent configuration should contain:
 *    EnableActions = yes
 *    Action = echo: echo "Hi"
 *    Subagent = filemgr.nsm
 *    
 *    [filemgr]
 *    RootFolder = /var/log/netxms/
 *    
 *Test server configuration should contain:
 *    LogFile = /var/log/netxms/netxmsd
 ******************************************************************/
public class TestConstants
{   
   //Server connection constants
   public static String serverAddress = "127.0.0.1";
   public static int serverPort = NXCSession.DEFAULT_CONN_PORT;
   public static String loginName = "admin";
   public static String password = "";
   //Other constants
   public static int NODE_ID = 4946;
   public static int LOCAL_NODE_ID = 9190;
   public static long SUBNET_ID = 4796;
   public static String FILE_NAME = "/opt/netxms/log/nxagentd";
   public static int FILE_OFFSET = 0;
   public static String ACTION = "netstat";
   public static int EVENT_CODE = 100000;
   public static int CONNECTION_POOL = 100;
   public static int USER_ID = 1;
   //Reinitialize variables from properties file
   public static TestConstants testConstants = new TestConstants();
   
   public TestConstants()
   {
      try
      {
         String propFile = "/etc/test.properties";
         if (System.getProperty("ConfigFile") != null && !System.getProperty("ConfigFile").isEmpty())
            propFile = System.getProperty("ConfigFile");
         InputStream stream = new FileInputStream(propFile);
         Properties properties = new Properties();
         properties.load(stream);
         serverAddress = properties.getProperty("server.address", "127.0.0.1");
         serverPort = Integer.parseInt(properties.getProperty("server.port", Integer.toString(NXCSession.DEFAULT_CONN_PORT)));
         loginName = properties.getProperty("login.name", "admin");
         password = properties.getProperty("password", "");
         NODE_ID = Integer.parseInt(properties.getProperty("node.id", "100"));
         LOCAL_NODE_ID = Integer.parseInt(properties.getProperty("local.node.id", "100"));
         SUBNET_ID = Integer.parseInt(properties.getProperty("subnet", "497"));
         FILE_NAME = properties.getProperty("file.name", "/var/log/netxms/netxmsd");
         FILE_OFFSET = Integer.parseInt(properties.getProperty("file.offset", "1000"));
         ACTION = properties.getProperty("action", "echo");
         EVENT_CODE = Integer.parseInt(properties.getProperty("event.code", "29"));
         CONNECTION_POOL = Integer.parseInt(properties.getProperty("connection.pull", "100"));
         USER_ID = Integer.parseInt(properties.getProperty("user.id", "1"));
      }
      catch (Exception e)
      {
         System.out.println("No properties file found: " + e.getMessage());
      }
   }
}
