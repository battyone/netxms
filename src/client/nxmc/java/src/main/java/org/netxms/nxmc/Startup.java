/**
 * 
 */
package org.netxms.nxmc;

import org.eclipse.swt.widgets.Display;
import org.netxms.nxmc.base.windows.MainWindow;

/**
 * @author victor
 *
 */
public class Startup
{
   /**
    * @param args
    */
   public static void main(String[] args)
   {
      MainWindow w = new MainWindow(null);
      w.setBlockOnOpen(true);
      w.open();
      Display.getCurrent().dispose();
   }

}
