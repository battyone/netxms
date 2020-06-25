/**
 * 
 */
package org.netxms.nxmc.base.views;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

/**
 * @author victor
 *
 */
public abstract class View
{
   private String name;
   private Window window;
   private Perspective perspective;

   /**
    * @param parent
    * @param style
    */
   public View(String name)
   {
   }
   
   /**
    * Create view. Intended to be called only by framework.
    * 
    * @param window owning window
    * @param perspective owning perspective
    * @param parent parent composite
    */
   public void create(Window window, Perspective perspective, Composite parent)
   {
      this.window = window;
      this.perspective = perspective;
      createContent(parent);
   }

   /**
    * Create view content
    *
    * @param parent parent control
    */
   protected void createContent(Composite parent)
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

   /**
    * Get owning window.
    * 
    * @return owning window
    */
   public Window getWindow()
   {
      return window;
   }

   /**
    * Check if this view is currently visible.
    * 
    * @return true if visible
    */
   public boolean isVisible()
   {
      return true; // FIXME: return actual flag
   }
}
