/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2011 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.switchmanager.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.GenericObject;
import org.netxms.client.objects.Interface;
import org.netxms.client.objects.Node;
import org.netxms.ui.eclipse.actions.RefreshAction;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.switchmanager.Messages;
import org.netxms.ui.eclipse.switchmanager.views.helpers.Dot1xPortComparator;
import org.netxms.ui.eclipse.switchmanager.views.helpers.Dot1xPortListLabelProvider;
import org.netxms.ui.eclipse.switchmanager.views.helpers.Dot1xPortSummary;
import org.netxms.ui.eclipse.widgets.SortableTableViewer;

/**
 * View 802.1x status of ports
 */
public class Dot1xStatusView extends ViewPart
{
	public static final String ID = "org.netxms.ui.eclipse.switchmanager.views.Dot1xStatusView"; //$NON-NLS-1$
	
	public static final int COLUMN_NODE = 0;
	public static final int COLUMN_PORT = 1;
	public static final int COLUMN_INTERFACE = 2;
	public static final int COLUMN_PAE_STATE = 3;
	public static final int COLUMN_BACKEND_STATE = 4;
	
	private NXCSession session;
	private long rootObject;
	private SortableTableViewer viewer;
	private Action actionRefresh;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		try
		{
			rootObject = Long.parseLong(site.getSecondaryId());
		}
		catch(NumberFormatException e)
		{
			rootObject = 0;
		}

		session = (NXCSession)ConsoleSharedData.getSession();
		GenericObject object = session.findObjectById(rootObject);
		setPartName(Messages.Dot1xStatusView_PartNamePrefix + ((object != null) ? object.getObjectName() : ("<" + rootObject + ">"))); //$NON-NLS-2$ //$NON-NLS-3$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		final String[] names = { Messages.Dot1xStatusView_ColDevice, Messages.Dot1xStatusView_ColSlotPort, Messages.Dot1xStatusView_ColInterface, Messages.Dot1xStatusView_ColPAE, Messages.Dot1xStatusView_ColBackend };
		final int[] widths = { 250, 60, 180, 150, 150 };
		viewer = new SortableTableViewer(parent, names, widths, 1, SWT.UP, SWT.FULL_SELECTION | SWT.MULTI);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new Dot1xPortListLabelProvider());
		viewer.setComparator(new Dot1xPortComparator());

		createActions();
		contributeToActionBars();
		
		refresh();
	}

	/**
	 * Create actions
	 */
	private void createActions()
	{
		actionRefresh = new RefreshAction() {
			@Override
			public void run()
			{
				refresh();
			}
		};
	}

	/**
	 * Contribute actions to action bar
	 */
	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fill local pull-down menu
	 * 
	 * @param manager
	 *           Menu manager for pull-down menu
	 */
	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(actionRefresh);
	}

	/**
	 * Fill local tool bar
	 * 
	 * @param manager
	 *           Menu manager for local toolbar
	 */
	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(actionRefresh);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
		viewer.getTable().setFocus();
	}

	/**
	 * Refresh content
	 */
	private void refresh()
	{
		List<Dot1xPortSummary> portList = new ArrayList<Dot1xPortSummary>();
		Set<Long> nodeList = new HashSet<Long>();
		fillPortList(portList, nodeList, rootObject);
		viewer.setInput(portList.toArray());
	}

	/**
	 * @param list
	 * @param nodeList 
	 * @param session
	 * @param rootObject2
	 */
	private void fillPortList(List<Dot1xPortSummary> portList, Set<Long> nodeList, long root)
	{
		GenericObject object = session.findObjectById(root);
		if (object == null)
			return;
		
		if ((object instanceof Node) && !nodeList.contains(object.getObjectId()))
		{
			for(GenericObject o : object.getAllChilds(GenericObject.OBJECT_INTERFACE))
			{
				if ((((Interface)o).getFlags() & Interface.IF_PHYSICAL_PORT) != 0)
					portList.add(new Dot1xPortSummary((Node)object, (Interface)o));
			}
			nodeList.add(object.getObjectId());
		}
		else
		{
			for(long id : object.getChildIdList())
				fillPortList(portList, nodeList, id);
		}
	}

}
