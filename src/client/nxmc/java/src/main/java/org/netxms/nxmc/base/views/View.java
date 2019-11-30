/**
 * 
 */
package org.netxms.nxmc.base.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author victor
 *
 */
public abstract class View
{
   private String name;

   /**
    * @param parent
    * @param style
    */
   public View(String name)
   {
   }
   
   /**
    * Create view content
    *
    * @param parent parent control
    */
   public void createContent(Composite parent)
   {
   }
   
   /**
    * Dispose view 
    */
   public void dispose()
   {
   }
   
   /**
    * Get view's name
    * 
    * @return view's name
    */
   public String getName()
   {
      return name;
   }
}
