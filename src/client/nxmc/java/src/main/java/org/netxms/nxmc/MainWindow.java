/**
 * 
 */
package org.netxms.nxmc;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * @author victor
 *
 */
public class MainWindow extends ApplicationWindow
{
   /**
    * @param parentShell
    */
   public MainWindow(Shell parentShell)
   {
      super(parentShell);
      addStatusLine();
      addCoolBar(SWT.HORIZONTAL | SWT.FLAT);
     // getCoolBarManager().add(item);
   }

   @Override
   protected void configureShell(Shell shell)
   {
      super.configureShell(shell);
      shell.setText("NetXMS Management Console");
      shell.setMaximized(true);
   }
}
