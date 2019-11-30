package org.netxms.nxmc;

import java.util.HashSet;
import java.util.Set;
import org.netxms.nxmc.base.views.Perspective;
import org.netxms.nxmc.modules.alarms.AlarmsPerspective;

/**
 * Global registry
 */
public final class Registry
{
   private static Registry instance = new Registry();
   
   public static Registry getInstance()
   {
      return instance;
   }
 
   private Set<Perspective> perspectives = new HashSet<Perspective>();
   
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
}
