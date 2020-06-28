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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.netxms.client.NXCSession;
import org.netxms.client.ScriptCompilationResult;
import org.netxms.client.TextOutputListener;

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
   
   private void executeScript(Path path) throws Exception
   {
      System.out.println("Executing script: " + path.toString());
      String script = new String(Files.readAllBytes(path));
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
   
   public void testNXSL() throws Exception
   {
      session = connect();
      List<Path> paths = Files.walk(Paths.get("./nxslScripts")).filter(Files::isRegularFile).collect(Collectors.toList());
      for(Path p : paths)
      {
         executeScript(p);
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
