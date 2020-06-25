/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2020 Raden Solutions
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
package org.netxms.nxmc;

import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import org.netxms.client.NXCSession;
import org.netxms.nxmc.base.views.Perspective;
import org.netxms.nxmc.modules.alarms.AlarmsPerspective;

/**
 * Global registry
 */
public final class Registry
{
   private static Registry instance = new Registry();
   
   /**
    * Get registry instance
    *
    * @return registry instance
    */
   public static Registry getInstance()
   {
      return instance;
   }
 
   /**
    * Get current NetXMS client library session
    * 
    * @return Current session
    */
   public static NXCSession getSession()
   {
      return getInstance().session;
   }

   /**
    * Get client timezone
    * 
    * @return
    */
   public static TimeZone getTimeZone()
   {
      return getInstance().timeZone;
   }

   private Set<Perspective> perspectives = new HashSet<Perspective>();
   private NXCSession session = null;
   private TimeZone timeZone = null;

   /**
    * Default constructor
    */
   private Registry()
   {
      perspectives.add(new AlarmsPerspective());
   }

   /**
    * Get registered perspectives
    *
    * @return the perspectives
    */
   public Set<Perspective> getPerspectives()
   {
      return perspectives;
   }

   /**
    * Set current NetXMS client library session
    * 
    * @param session Current session
    */
   public void setSession(NXCSession session)
   {
      this.session = session;
   }

   /**
    * Set server time zone
    */
   public void setServerTimeZone()
   {
      if (session != null)
      {
         String tz = session.getServerTimeZone();
         timeZone = TimeZone.getTimeZone(tz.replaceAll("[A-Za-z]+([\\+\\-][0-9]+).*", "GMT$1")); //$NON-NLS-1$ //$NON-NLS-2$
      }
   }

   /**
    * Reset time zone to default
    */
   public void resetTimeZone()
   {
      timeZone = null;
   }
}
