/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2013 Victor Kirhenshtein
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.netxms.tests;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.netxms.base.InetAddressEx;
import org.netxms.base.MacAddress;
import org.netxms.client.NXCObjectCreationData;
import org.netxms.client.NXCSession;
import org.netxms.client.ScriptCompilationResult;
import org.netxms.client.TextOutputListener;
import org.netxms.client.objects.AbstractObject;

/**
 * Tests for scripting functions
 */
public class ScriptTest extends AbstractSessionTest implements TextOutputListener
{
   private NXCSession session;
   private Boolean scriptFailed;
   
   public void testAddressMap() throws Exception
   {
      final NXCSession session = connect();

      ScriptCompilationResult r = session.compileScript("a = 1; b = 2; return a / b;", true);
      assertTrue(r.success);
      assertNotNull(r.code);
      assertNull(r.errorMessage);
      
      r = session.compileScript("a = 1; b = 2; return a / b;", false);
      assertTrue(r.success);
      assertNull(r.code);
      assertNull(r.errorMessage);
      
      r = session.compileScript("a = 1*; b = 2; return a / b;", false);
      assertFalse(r.success);
      assertNull(r.code);
      assertNotNull(r.errorMessage);
      System.out.println("Compilation error message: \"" + r.errorMessage + "\"");
      
      session.disconnect();
   }
   
   private void executeScript(String script) throws Exception
   {
      ScriptCompilationResult r = session.compileScript(script, true);
      if (r.errorMessage != null)
         System.out.println("Compilation error message: \"" + r.errorMessage + "\"");
      assertTrue(r.success);
      assertNotNull(r.code);
      assertNull(r.errorMessage);
      
      scriptFailed = false;
      session.executeScript(2, script, ScriptTest.this);
      assertFalse(scriptFailed);         
   }
   
   public void testNXSLObjectFunctions() throws Exception
   {
      session = connect();
      String script = new String(Files.readAllBytes(Paths.get("./nxslScripts/objectFunctions.nxsl")));

      session.syncObjects();
      AbstractObject object = session.findObjectById(2);
      assertNotNull(object);
      
      NXCObjectCreationData cd = new NXCObjectCreationData(AbstractObject.OBJECT_NODE, "TestNode", 2);
      cd.setCreationFlags(NXCObjectCreationData.CF_CREATE_UNMANAGED);
      cd.setIpAddress(new InetAddressEx(InetAddress.getByName("192.168.10.1"), 0));
      long nodeId = session.createObject(cd);
      assertFalse(nodeId == 0);
      
      object = session.findObjectById(3);
      assertNotNull(object);      

      cd = new NXCObjectCreationData(AbstractObject.OBJECT_TEMPLATE, "TestTemplate", 3);
      long templateId = session.createObject(cd);
      assertFalse(templateId == 0);

      Thread.sleep(1000);  // Object update should be received from server

      object = session.findObjectById(nodeId);
      assertNotNull(object);
      object = session.findObjectById(templateId);
      assertNotNull(object);  
      
      session.applyTemplate(templateId, nodeId);

      String interfaceName1 = "lo";
      cd = new NXCObjectCreationData(AbstractObject.OBJECT_INTERFACE, interfaceName1, nodeId);
      cd.setMacAddress(MacAddress.parseMacAddress("00:10:FA:23:11:7A"));
      cd.setIpAddress(new InetAddressEx(InetAddress.getLoopbackAddress(), 0));
      cd.setIfIndex(0);
      session.createObject(cd);

      String interfaceName2 = "eth0";
      cd = new NXCObjectCreationData(AbstractObject.OBJECT_INTERFACE, interfaceName2, nodeId);
      cd.setMacAddress(MacAddress.parseMacAddress("01 02 fa c4 10 dc"));
      cd.setIpAddress(new InetAddressEx(InetAddress.getByName("192.168.10.1"), 0));
      cd.setIfIndex(1);
      session.createObject(cd);
      
      script = String.format(script, nodeId, templateId, interfaceName1, interfaceName2);
      
      executeScript(script);
      
      session.deleteObject(nodeId);
      session.deleteObject(templateId);      
      session.disconnect();
   }
   
   public void testNXSL() throws Exception
   {
      session = connect();
      List<Path> paths = Files.walk(Paths.get("./nxslScripts")).filter(Files::isRegularFile).collect(Collectors.toList());
      for(Path p : paths)
      {
         System.out.println("Executing script: " + p.toString());
         String script = new String(Files.readAllBytes(p));
         executeScript(script);
      }
      
      session.disconnect();
   }

   /* (non-Javadoc)
    * @see org.netxms.client.ActionExecutionListener#messageReceived(java.lang.String)
    */
   @Override
   public void messageReceived(final String text)
   {
      System.out.println(text);
   }

   @Override
   public void setStreamId(long streamId)
   {
   }

   @Override
   public void onError()
   {
      scriptFailed = true;
   }
}
