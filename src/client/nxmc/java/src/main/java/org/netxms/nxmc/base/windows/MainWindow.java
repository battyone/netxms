/**
 * 
 */
package org.netxms.nxmc.base.windows;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.netxms.nxmc.Registry;
import org.netxms.nxmc.base.views.Perspective;

/**
 * @author victor
 *
 */
public class MainWindow extends ApplicationWindow
{
   private Composite perspectiveArea;
   private ToolBarManager perspectiveSwitcher;
   private Perspective currentPerspective;

   /**
    * @param parentShell
    */
   public MainWindow(Shell parentShell)
   {
      super(parentShell);
      addStatusLine();
      
      addCoolBar(SWT.HORIZONTAL | SWT.FLAT);
      setupPerspectiveSwitcher();
      getCoolBarManager().setLockLayout(true);
   }

   /**
    * @see org.eclipse.jface.window.ApplicationWindow#configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell shell)
   {
      super.configureShell(shell);
      shell.setText("NetXMS Management Console");
      shell.setMaximized(true);
   }

   /**
    * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createContents(Composite parent)
   {
      perspectiveArea = new Composite(parent, SWT.NONE);
      perspectiveArea.setLayout(new FillLayout());
      return perspectiveArea;
   }

   /**
    * Setup perspective switcher
    */
   private void setupPerspectiveSwitcher()
   {
      perspectiveSwitcher = new ToolBarManager();

      for(final Perspective p : Registry.getInstance().getPerspectives())
      {
         perspectiveSwitcher.add(new Action(p.getName()) {
            @Override
            public void run()
            {
               switchToPerspective(p);
            }
         });
      }

      getCoolBarManager().add(perspectiveSwitcher);
   }
   
   /**
    * @param p
    */
   private void switchToPerspective(Perspective p)
   {
      if (currentPerspective != null)
         currentPerspective.disposeWidgets();
      currentPerspective = p;
      currentPerspective.createWidgets(perspectiveArea);
   }
}
