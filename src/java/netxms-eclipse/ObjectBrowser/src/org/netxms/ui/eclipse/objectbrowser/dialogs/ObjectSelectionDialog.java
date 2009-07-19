/**
 * 
 */
package org.netxms.ui.eclipse.objectbrowser.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.netxms.client.NXCObject;
import org.netxms.client.NXCSession;
import org.netxms.ui.eclipse.objectbrowser.Activator;
import org.netxms.ui.eclipse.objectbrowser.ObjectTree;
import org.netxms.ui.eclipse.shared.NXMCSharedData;

/**
 * @author Victor
 * 
 */
public class ObjectSelectionDialog extends Dialog
{
	private ObjectTree objectTree;
	//private ObjectList objectList;
	protected CTabFolder tabFolder;

	private long[] rootObjects;
	private long[] selectedObjects;
	//private boolean treeActive = true;

	/**
	 * @param parentShell
	 */
	public ObjectSelectionDialog(Shell parentShell, long[] rootObjects)
	{
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.rootObjects = rootObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.getString("ObjectSelectionDialog.title")); //$NON-NLS-1$
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		try
		{
			newShell.setSize(settings.getInt("SelectObject.cx"), settings.getInt("SelectObject.cy")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(NumberFormatException e)
		{
			newShell.setSize(400, 350);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		dialogArea.setLayout(new FormLayout());

		//tabFolder = new CTabFolder(dialogArea, SWT.BOTTOM | SWT.FLAT | SWT.MULTI);

		// Object tree
		/*objectTree = new ObjectTree(tabFolder, SWT.NONE, ObjectTree.CHECKBOXES, rootObjects);
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("Tree");
		tabItem.setControl(objectTree);*/
		objectTree = new ObjectTree(dialogArea, SWT.NONE, ObjectTree.CHECKBOXES, rootObjects);
		
		String text = settings.get("SelectObject.Filter"); //$NON-NLS-1$
		if (text != null)
			objectTree.setFilter(text);

		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		fd.bottom = new FormAttachment(100, 0);
		//tabFolder.setLayoutData(fd);
		objectTree.setLayoutData(fd);

		// Object list
		/*
		objectList = new ObjectList(tabFolder, SWT.NONE, ObjectList.CHECKBOXES);
		tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("List");
		tabItem.setControl(objectList);
		if (text != null)
			objectList.setFilter(text);
		*/

		return dialogArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed()
	{
		saveSettings();
		super.cancelPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
/*		final Control control = tabFolder.getSelection().getControl();

		if (control == objectTree)*/
		{
			//treeActive = true;
			Long[] objects = objectTree.getCheckedObjects();
			selectedObjects = new long[objects.length];
			for(int i = 0; i < objects.length; i++)
				selectedObjects[i] = objects[i].longValue();
		}
		/*else if (control == objectList)
		{
			treeActive = false;
		}*/

		saveSettings();
		super.okPressed();
	}

	/**
	 * Save dialog settings
	 */
	private void saveSettings()
	{
		Point size = getShell().getSize();
		IDialogSettings settings = Activator.getDefault().getDialogSettings();

		settings.put("SelectObject.cx", size.x); //$NON-NLS-1$
		settings.put("SelectObject.cy", size.y); //$NON-NLS-1$
		settings.put("SelectObject.Filter", objectTree.getFilter()); //$NON-NLS-1$
	}

	/**
	 * Retrieve selected objects
	 * 
	 * @return
	 */
	public NXCObject[] getCheckedObjects()
	{
		if (selectedObjects == null)
			return new NXCObject[0];

		return NXMCSharedData.getInstance().getSession().findMultipleObjects(selectedObjects);
	}

	/**
	 * Retrive all selected objects (including childs)
	 * 
	 * @return
	 */
	public NXCObject[] getAllCheckedObjects()
	{
		return getAllCheckedObjects(-1);
	}

	/**
	 * Retrive all selected objects by type
	 * 
	 * @return
	 */
	public NXCObject[] getAllCheckedObjects(int objectType)
	{
		if (selectedObjects == null)
			return new NXCObject[0];

		final NXCSession session = NXMCSharedData.getInstance().getSession();

		final List<NXCObject> resuls = new ArrayList<NXCObject>();
		final NXCObject[] objects = session.findMultipleObjects(selectedObjects);
		for(int i = 0; i < objects.length; i++)
		{
			resuls.addAll(extractAllObjects(session, objects[i]));
		}

		if (objectType != -1)
		{
			final Iterator<NXCObject> it = resuls.iterator();
			while (it.hasNext()) {
				final NXCObject next = it.next();
				if (next.getObjectClass() != objectType) {
					it.remove();
				}
			}
		}

		return resuls.toArray(new NXCObject[] {});
	}

	private List<NXCObject> extractAllObjects(NXCSession session, NXCObject object)
	{
		final List<NXCObject> ret = new ArrayList<NXCObject>(0);

		ret.add(object);

		final Iterator<Long> it = object.getChilds();
		while(it.hasNext())
		{
			final Long childId = it.next();
			final NXCObject child = session.findObjectById(childId);
			if (child != null)
			{
				ret.addAll(extractAllObjects(session, child));
			}
		}

		return ret;
	}

}
