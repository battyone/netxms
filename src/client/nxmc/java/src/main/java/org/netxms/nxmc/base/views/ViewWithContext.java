/**
 * 
 */
package org.netxms.nxmc.base.views;

/**
 * @author victor
 *
 */
public class ViewWithContext extends View
{
   private Object context = null;

   /**
    * @param name
    */
   public ViewWithContext(String name)
   {
      super(name);
   }

   protected void setContext(Object context)
   {
      this.context = context;
   }

   protected Object getContext()
   {
      return context;
   }
}
