/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2013 Victor Kirhenshtein
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
package org.netxms.ui.eclipse.snmp.views;

import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.netxms.client.NXCSession;
import org.netxms.client.snmp.SnmpUsmCredential;
import org.netxms.ui.eclipse.console.resources.SharedIcons;
import org.netxms.ui.eclipse.jobs.ConsoleJob;
import org.netxms.ui.eclipse.objectbrowser.widgets.ZoneSelector;
import org.netxms.ui.eclipse.shared.ConsoleSharedData;
import org.netxms.ui.eclipse.snmp.Activator;
import org.netxms.ui.eclipse.snmp.Messages;
import org.netxms.ui.eclipse.snmp.dialogs.AddUsmCredDialog;
import org.netxms.ui.eclipse.snmp.views.helpers.SnmpConfig;
import org.netxms.ui.eclipse.snmp.views.helpers.SnmpUsmComparator;
import org.netxms.ui.eclipse.snmp.views.helpers.SnmpUsmLabelProvider;
import org.netxms.ui.eclipse.tools.MessageDialogHelper;
import org.netxms.ui.eclipse.tools.StringComparator;
import org.netxms.ui.eclipse.widgets.SortableTableViewer;

/**
 * Configurator for network discovery
 */
public class SnmpCredentials extends ViewPart implements ISaveablePart
{
	public static final String ID = "org.netxms.ui.eclipse.snmp.views.SnmpCredentials"; //$NON-NLS-1$

	public static final int USM_CRED_USER_NAME = 0;
   public static final int USM_CRED_AUTHENTICATION = 1;
   public static final int USM_CRED_ENCRYPTION = 2;
   public static final int USM_CRED_AUTH_PASSWORD = 3;
   public static final int USM_CRED_ENC_PASSWORD = 4;
   public static final int USM_CRED_COMMENTS = 5;
	
	private NXCSession session;
	private boolean modified = false;
	private FormToolkit toolkit;
	private ScrolledForm form;
	private TableViewer snmpCommunityList;
	private SortableTableViewer snmpUsmCredList;
	private TableViewer snmpPortList;
	private Action actionSave;
	private SnmpConfig config;
	private ZoneSelector zoneSelector;
	private long zoneUIN = SnmpConfig.SNMP_CONFIG_GLOBAL;

	/* (non-Javadoc)
    * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
    */
   @Override
   public void init(IViewSite site) throws PartInitException
   {
      super.init(site);
      session = ConsoleSharedData.getSession();
   }

   /* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		toolkit = new FormToolkit(getSite().getShell().getDisplay());
		form = toolkit.createScrolledForm(parent);
      form.setText(Messages.get().SnmpCredentials_FormTitle);
	
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);

		if (session.isZoningEnabled())
		{
         toolkit.decorateFormHeading(form.getForm());
   		Composite headArea = toolkit.createComposite(form.getForm().getHead());
   		headArea.setLayout(new GridLayout());
         zoneSelector = new ZoneSelector(headArea, SWT.NONE, true);
         zoneSelector.setEmptySelectionText("Global");
         zoneSelector.setLabel("Select zone");
   
         GridData gd = new GridData();
         gd.widthHint = 300;
         zoneSelector.setLayoutData(gd);
         form.setHeadClient(headArea);
         zoneSelector.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e)
            {
               zoneUIN = zoneSelector.getZoneUIN();
               loadSnmpConfig();
            }
         });
		}
      
		createSnmpCommunitySection();
		createSnmpPortList();
      createSnmpUsmCredSection();
		
		createActions();
		contributeToActionBars();

      // Load config
      loadSnmpConfig();
	}
	
	/**
	 * Load SNMP config
	 * @param zoneUIN of config
	 */
	private void loadSnmpConfig()
	{
	   new ConsoleJob(Messages.get().SnmpCredentials_LoadingConfig, this, Activator.PLUGIN_ID, null) {
         @Override
         protected void runInternal(IProgressMonitor monitor) throws Exception
         {
            final SnmpConfig loadedConfig = SnmpConfig.load(session);
            runInUIThread(new Runnable() {
               @Override
               public void run()
               {
                  setConfig(loadedConfig);
               }
            });
         }

         @Override
         protected String getErrorMessage()
         {
            return Messages.get().SnmpCredentials_ErrorLoadingConfig;
         }
      }.start();
	}
	
	/**
	 * Create actions
	 */
	private void createActions()
	{
		actionSave = new Action(Messages.get().SnmpConfigurator_Save, SharedIcons.SAVE) {
			@Override
			public void run()
			{
				save();
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
		manager.add(actionSave);
	}

	/**
	 * Fill local tool bar
	 * 
	 * @param manager
	 *           Menu manager for local toolbar
	 */
	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(actionSave);
	}
	
	/**
	 * Create "SNMP Communities" section
	 */
	private void createSnmpCommunitySection()
	{
		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText(Messages.get().SnmpConfigurator_SectionCommunities);
		section.setDescription(Messages.get().SnmpConfigurator_SectionCommunitiesDescr);
		TableWrapData td = new TableWrapData();
		td.align = TableWrapData.FILL;
		td.grabHorizontal = true;
		section.setLayoutData(td);
		
		Composite clientArea = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		clientArea.setLayout(layout);
		section.setClient(clientArea);
		
		snmpCommunityList = new TableViewer(clientArea, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		toolkit.adapt(snmpCommunityList.getTable());
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalSpan = 2;
		gd.heightHint = 150;
		snmpCommunityList.getTable().setLayoutData(gd);
		snmpCommunityList.getTable().setSortDirection(SWT.UP);
		snmpCommunityList.setContentProvider(new ArrayContentProvider());
		snmpCommunityList.setComparator(new StringComparator());
		
		final ImageHyperlink linkAdd = toolkit.createImageHyperlink(clientArea, SWT.NONE);
		linkAdd.setText(Messages.get().SnmpConfigurator_Add);
		linkAdd.setImage(SharedIcons.IMG_ADD_OBJECT);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		linkAdd.setLayoutData(gd);
		linkAdd.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e)
			{
				addCommunity();
			}
		});
		
		final ImageHyperlink linkRemove = toolkit.createImageHyperlink(clientArea, SWT.NONE);
		linkRemove.setText(Messages.get().SnmpConfigurator_Remove);
		linkRemove.setImage(SharedIcons.IMG_DELETE_OBJECT);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		linkRemove.setLayoutData(gd);
		linkRemove.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e)
			{
				removeCommunity();
			}
		});
	}
	
	/**
	 * Create "Address Filters" section
	 */
	private void createSnmpUsmCredSection()
	{
		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText(Messages.get().SnmpConfigurator_SectionUSM);
		section.setDescription(Messages.get().SnmpConfigurator_SectionUSMDescr);
		TableWrapData td = new TableWrapData();
		td.align = TableWrapData.FILL;
		td.grabHorizontal = true;
		td.colspan = 2;
		section.setLayoutData(td);
		
		Composite clientArea = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		clientArea.setLayout(layout);
		section.setClient(clientArea);
		
		final String[] names = { "User name", "Auth type", "Priv type", "Auth secret", "Priv secret", "Comments" };
		final int[] widths = { 100, 100, 100, 100, 100, 100 };
		snmpUsmCredList = new SortableTableViewer(clientArea, names, widths, 0, SWT.DOWN, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		toolkit.adapt(snmpUsmCredList.getTable());
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalSpan = 3;
		gd.heightHint = 150;
		snmpUsmCredList.getTable().setLayoutData(gd);
		snmpUsmCredList.setContentProvider(new ArrayContentProvider());
		snmpUsmCredList.setLabelProvider(new SnmpUsmLabelProvider());
		snmpUsmCredList.setComparator(new SnmpUsmComparator());
		snmpUsmCredList.addDoubleClickListener(new IDoubleClickListener() {
         
         @Override
         public void doubleClick(DoubleClickEvent event)
         {
            editUsmCredzantial();
         }
      });

		final ImageHyperlink linkAdd = toolkit.createImageHyperlink(clientArea, SWT.NONE);
		linkAdd.setText(Messages.get().SnmpConfigurator_Add);
		linkAdd.setImage(SharedIcons.IMG_ADD_OBJECT);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		linkAdd.setLayoutData(gd);
		linkAdd.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e)
			{
				addUsmCredentials();
			}
		});

      final ImageHyperlink linkEdit = toolkit.createImageHyperlink(clientArea, SWT.NONE);
      linkEdit.setText("Edit...");
      linkEdit.setImage(SharedIcons.IMG_EDIT);
      gd = new GridData();
      gd.verticalAlignment = SWT.TOP;
      linkEdit.setLayoutData(gd);
      linkEdit.addHyperlinkListener(new HyperlinkAdapter() {
         @Override
         public void linkActivated(HyperlinkEvent e)
         {
            editUsmCredzantial();
         }
      });
		
		final ImageHyperlink linkRemove = toolkit.createImageHyperlink(clientArea, SWT.NONE);
		linkRemove.setText(Messages.get().SnmpConfigurator_Remove);
		linkRemove.setImage(SharedIcons.IMG_DELETE_OBJECT);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		linkRemove.setLayoutData(gd);
		linkRemove.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e)
			{
				removeUsmCredentials();
			}
		});
	}
	
	  /**
    * Create "Port List" section
    */
   private void createSnmpPortList()
   {
      Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION | Section.TITLE_BAR);
      section.setText(Messages.get().SnmpCredentials_Ports);
      section.setDescription(Messages.get().SnmpCredentials_PortsDescription);
      TableWrapData td = new TableWrapData();
      td.align = TableWrapData.FILL;
      td.grabHorizontal = true;
      section.setLayoutData(td);
      
      Composite clientArea = toolkit.createComposite(section);
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      clientArea.setLayout(layout);
      section.setClient(clientArea);
      
      snmpPortList = new TableViewer(clientArea, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
      toolkit.adapt(snmpPortList.getTable());
      GridData gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.grabExcessHorizontalSpace = true;
      gd.verticalAlignment = SWT.FILL;
      gd.grabExcessVerticalSpace = true;
      gd.verticalSpan = 2;
      gd.heightHint = 150;
      snmpPortList.getTable().setLayoutData(gd);
      snmpPortList.setContentProvider(new ArrayContentProvider());
      snmpPortList.setComparator(new StringComparator());

      final ImageHyperlink linkAdd = toolkit.createImageHyperlink(clientArea, SWT.NONE);
      linkAdd.setText(Messages.get().SnmpConfigurator_Add);
      linkAdd.setImage(SharedIcons.IMG_ADD_OBJECT);
      gd = new GridData();
      gd.verticalAlignment = SWT.TOP;
      linkAdd.setLayoutData(gd);
      linkAdd.addHyperlinkListener(new HyperlinkAdapter() {
         @Override
         public void linkActivated(HyperlinkEvent e)
         {
            addSnmpPort();
         }
      });
      
      final ImageHyperlink linkRemove = toolkit.createImageHyperlink(clientArea, SWT.NONE);
      linkRemove.setText(Messages.get().SnmpConfigurator_Remove);
      linkRemove.setImage(SharedIcons.IMG_DELETE_OBJECT);
      gd = new GridData();
      gd.verticalAlignment = SWT.TOP;
      linkRemove.setLayoutData(gd);
      linkRemove.addHyperlinkListener(new HyperlinkAdapter() {
         @Override
         public void linkActivated(HyperlinkEvent e)
         {
            removeSnmpPort();
         }
      });
   }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
		form.setFocus();
	}
	
	/**
	 * @param config
	 */
	private void setConfig(SnmpConfig config)
	{
		this.config = config;
		snmpCommunityList.setInput(config.getCommunities(zoneUIN));
		snmpUsmCredList.setInput(config.getUsmCredentials(zoneUIN));
		snmpPortList.setInput(config.getPorts(zoneUIN));
		
		modified = false;
		firePropertyChange(PROP_DIRTY);
	}
	
	/**
	 * Mark view as modified
	 */
	private void setModified()
	{
		if (!modified)
		{
			modified = true;
			firePropertyChange(PROP_DIRTY);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor)
	{
		try
		{
			config.save(session);
		}
		catch(Exception e)
		{
			MessageDialogHelper.openError(getSite().getShell(), Messages.get().SnmpConfigurator_Error, 
			      String.format(Messages.get().SnmpCredentials_CannotSaveConfig, e.getLocalizedMessage()));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	@Override
	public void doSaveAs()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	@Override
	public boolean isDirty()
	{
		return modified;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	@Override
	public boolean isSaveOnCloseNeeded()
	{
		return modified;
	}
	
	/**
	 * Save settings
	 */
	private void save()
	{
		new ConsoleJob(Messages.get().SnmpCredentials_SaveConfig, this, Activator.PLUGIN_ID, null) {
			@Override
			protected void runInternal(IProgressMonitor monitor) throws Exception
			{
				config.save(session);
				runInUIThread(new Runnable() {
					@Override
					public void run()
					{
						modified = false;
						firePropertyChange(PROP_DIRTY);
					}
				});
			}
			
			@Override
			protected String getErrorMessage()
			{
				return Messages.get().SnmpCredentials_ErrorSavingConfig;
			}
		}.start();
	}
	
	/**
	 * Add SNMP community to the list
	 */
	private void addCommunity()
	{
		InputDialog dlg = new InputDialog(getSite().getShell(), Messages.get().SnmpConfigurator_AddCommunity, 
            Messages.get().SnmpConfigurator_AddCommunityDescr, "", null); //$NON-NLS-1$
		if (dlg.open() == Window.OK)
		{
			String s = dlg.getValue();
			config.addCommunityString(s, zoneUIN);
         snmpCommunityList.setInput(config.getCommunities(zoneUIN));
         setModified();
		}
	}
	
	/**
	 * Remove selected SNMP communities
	 */
	private void removeCommunity()
	{
		final List<String> list = config.getCommunities(zoneUIN);
		IStructuredSelection selection = (IStructuredSelection)snmpCommunityList.getSelection();
		if (selection.size() > 0)
		{
			for(Object o : selection.toList())
			{
				list.remove(o);
			}
			snmpCommunityList.setInput(list);
			setModified();
		}
	}

	/**
	 * Add SNMP USM credentials to the list
	 */
	private void addUsmCredentials()
	{
		AddUsmCredDialog dlg = new AddUsmCredDialog(getSite().getShell(), null);
		if (dlg.open() == Window.OK)
		{
			SnmpUsmCredential cred = dlg.getValue();
			cred.setZoneId((int)zoneUIN);
         config.addUsmCredentials(cred, zoneUIN);
         snmpUsmCredList.setInput(config.getUsmCredentials(zoneUIN));
         setModified();
		}
	}
   
   /**
    * Edit SNMP USM credential
    */
   private void editUsmCredzantial()
   {
      IStructuredSelection selection = (IStructuredSelection)snmpUsmCredList.getSelection();
      if (selection.size() != 1)
         return;
      SnmpUsmCredential cred = (SnmpUsmCredential)selection.getFirstElement();
      AddUsmCredDialog dlg = new AddUsmCredDialog(getSite().getShell(), cred);
      if (dlg.open() == Window.OK)
      {
         final List<SnmpUsmCredential> list = config.getUsmCredentials(zoneUIN);
         snmpUsmCredList.setInput(list.toArray());
         setModified();
      }
   }
	
	/**
	 * Remove selected SNMP USM credentials
	 */
	private void removeUsmCredentials()
	{
		final List<SnmpUsmCredential> list = config.getUsmCredentials(zoneUIN);
		IStructuredSelection selection = (IStructuredSelection)snmpUsmCredList.getSelection();
		if (selection.size() > 0)
		{
			for(Object o : selection.toList())
			{
				list.remove(o);
			}
			snmpUsmCredList.setInput(list.toArray());
			setModified();
		}
	}
	
	  /**
    * Add SNMP USM credentials to the list
    */
   private void addSnmpPort()
   {
      InputDialog dlg = new InputDialog(getSite().getShell(), Messages.get().SnmpCredentials_AddPort, 
            Messages.get().SnmpCredentials_PleaseEnterPort, "", null); //$NON-NLS-1$
      if (dlg.open() == Window.OK)
      {
         String value = dlg.getValue();
         config.addPort(value, zoneUIN);
         snmpPortList.setInput(config.getPorts(zoneUIN));
         setModified();
      }
   }
   
   /**
    * Remove selected SNMP USM credentials
    */
   private void removeSnmpPort()
   {
      final List<String> list = config.getPorts(zoneUIN);
      IStructuredSelection selection = (IStructuredSelection)snmpPortList.getSelection();
      if (selection.size() > 0)
      {
         for(Object o : selection.toList())
         {
            list.remove(o);
         }
         snmpPortList.setInput(list.toArray());
         setModified();
      }
   }
}
