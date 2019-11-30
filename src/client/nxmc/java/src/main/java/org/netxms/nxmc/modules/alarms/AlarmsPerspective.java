/**
 * 
 */
package org.netxms.nxmc.modules.alarms;

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
   }
}
