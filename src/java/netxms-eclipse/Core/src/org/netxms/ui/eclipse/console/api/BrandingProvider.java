/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2014 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.console.api;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

/**
 * Branding provider interface
 */
public interface BrandingProvider
{
	/**
	 * Get common product name.
	 * 
	 * @return
	 */
	public String getProductName();
	
	/**
    * Get product name for management console.
    * 
    * @return
    */
   public String getConsoleProductName();
   
	/**
	 * Get default perspective. Should return null to use default (or defined by another branding manager)
	 * 
	 * @return default perspective ID or null to use default.
	 */
	public String getDefaultPerspective();
	
	/**
	 * Get image to be displayed in login dialog.
	 * 
	 * @return image descriptor for image to be displayed in login dialog or null to use default
	 */
	public ImageDescriptor getLoginTitleImage();
	
	/**
	 * Get login dialog title.
	 * 
	 * @return login dialog title or null to use default
	 */
	public String getLoginTitle();
	
	/**
	 * Get custom "About" dialog. New dialog must be returned on each call.
	 * 
	 * @param parentShell parent shell for dialog
	 * @return custom "About" dialog or null to use default
	 */
	public Dialog getAboutDialog(Shell parentShell);
	
	/**
	 * Get redirection URL for web console. Has no effect on RCP console.
	 * 
	 * @return redirection URL or null to use default
	 */
	public String getRedirectionURL();
}
