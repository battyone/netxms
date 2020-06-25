/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2020 Victor Kirhenshtein
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
package org.netxms.nxmc.modules.alarms;

import org.eclipse.swt.widgets.Composite;
import org.netxms.nxmc.base.views.Perspective;
import org.netxms.nxmc.base.views.PerspectiveConfiguration;

/**
 * Alarm browser perspective
 */
public class AlarmsPerspective extends Perspective
{
   /**
    * @param name
    */
   public AlarmsPerspective()
   {
      super("Alarms");
   }

   /**
    * @see org.netxms.nxmc.base.views.Perspective#configurePerspective(org.netxms.nxmc.base.views.PerspectiveConfiguration)
    */
   @Override
   protected void configurePerspective(PerspectiveConfiguration configuration)
   {
      super.configurePerspective(configuration);
      configuration.hasNavigationArea = false;
      configuration.hasSupplementalArea = false;
      configuration.multiViewNavigationArea = false;
      configuration.multiViewMainArea = false;
   }

   /**
    * @see org.netxms.nxmc.base.views.Perspective#createMainArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected void createMainArea(Composite parent)
   {
      // TODO Auto-generated method stub
      super.createMainArea(parent);
   }
}
