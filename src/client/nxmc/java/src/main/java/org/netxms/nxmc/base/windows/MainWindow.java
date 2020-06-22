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
package org.netxms.nxmc.base.windows;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.netxms.nxmc.PreferenceStore;
import org.netxms.nxmc.Registry;
import org.netxms.nxmc.base.views.Perspective;
import org.netxms.nxmc.base.widgets.FlatButton;
import org.netxms.nxmc.resources.ThemeEngine;

/**
 * Main window
 */
public class MainWindow extends ApplicationWindow
{
   private Composite windowContent;
   private Composite topMenu;
   private Composite perspectiveArea;
   private Perspective currentPerspective;

   /**
    * @param parentShell
    */
   public MainWindow(Shell parentShell)
   {
      super(parentShell);
      addStatusLine();
   }

   /**
    * @see org.eclipse.jface.window.ApplicationWindow#configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell shell)
   {
      super.configureShell(shell);
      shell.setText("NetXMS Management Console");

      PreferenceStore ps = PreferenceStore.getInstance();
      shell.setSize(ps.getAsPoint("MainWindow.Size", 600, 400));
      shell.setLocation(ps.getAsPoint("MainWindow.Location", 100, 100));
      shell.setMaximized(ps.getAsBoolean("MainWindow.Maximized", true));

      shell.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            PreferenceStore ps = PreferenceStore.getInstance();
            ps.set("MainWindow.Maximized", getShell().getMaximized());
            ps.set("MainWindow.Size", getShell().getSize());
            ps.set("MainWindow.Location", getShell().getLocation());
         }
      });
   }

   /**
    * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createContents(Composite parent)
   {
      windowContent = new Composite(parent, SWT.NONE);

      GridLayout layout = new GridLayout();
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      windowContent.setLayout(layout);

      topMenu = new Composite(windowContent, SWT.NONE);
      topMenu.setBackground(ThemeEngine.getBackgroundColor("TopMenu"));
      GridData gd = new GridData();
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = SWT.FILL;
      topMenu.setLayoutData(gd);
      topMenu.setLayout(new RowLayout(SWT.HORIZONTAL));

      perspectiveArea = new Composite(windowContent, SWT.NONE);
      perspectiveArea.setLayout(new FillLayout());
      gd = new GridData();
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = SWT.FILL;
      perspectiveArea.setLayoutData(gd);

      setupPerspectiveSwitcher();

      return windowContent;
   }

   /**
    * Setup perspective switcher
    */
   private void setupPerspectiveSwitcher()
   {
      Font font = ThemeEngine.getFont("TopMenu");
      Color background = ThemeEngine.getBackgroundColor("TopMenu");
      Color foreground = ThemeEngine.getForegroundColor("TopMenu");

      FlatButton appMenu = new FlatButton(topMenu, SWT.NONE);
      appMenu.setText("\u2630");
      appMenu.setFont(font);
      appMenu.setBackground(background);
      appMenu.setForeground(foreground);

      for(final Perspective p : Registry.getInstance().getPerspectives())
      {
         FlatButton button = new FlatButton(topMenu, SWT.NONE);
         button.setText(p.getName());
         button.setFont(font);
         button.setBackground(background);
         button.setForeground(foreground);
         button.setAction(new Runnable() {
            @Override
            public void run()
            {
               switchToPerspective(p);
            }
         });
      }
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
