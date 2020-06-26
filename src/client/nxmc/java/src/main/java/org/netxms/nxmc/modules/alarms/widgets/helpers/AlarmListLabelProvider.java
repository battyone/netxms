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
package org.netxms.nxmc.modules.alarms.widgets.helpers;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.netxms.client.NXCSession;
import org.netxms.client.events.Alarm;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.ZoneMember;
import org.netxms.client.users.AbstractUserObject;
import org.netxms.nxmc.Registry;
import org.netxms.nxmc.localization.DateFormatFactory;
import org.netxms.nxmc.localization.LocalizationHelper;
import org.netxms.nxmc.modules.alarms.widgets.AlarmList;
import org.netxms.nxmc.modules.objects.widgets.helpers.BaseObjectLabelProvider;
import org.netxms.nxmc.resources.ResourceManager;
import org.netxms.nxmc.resources.SharedIcons;
import org.netxms.nxmc.resources.StatusDisplayInfo;
import org.netxms.nxmc.tools.ViewerElementUpdater;
import org.xnap.commons.i18n.I18n;

/**
 * Label provider for alarm list
 */
public class AlarmListLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider
{
   private static final Color FOREGROUND_COLOR_DARK = new Color(Display.getCurrent(), 0, 0, 0);
   private static final Color FOREGROUND_COLOR_LIGHT = new Color(Display.getCurrent(), 255, 255, 255);
   private static final Color[] FOREGROUND_COLORS =
      { FOREGROUND_COLOR_LIGHT, FOREGROUND_COLOR_DARK, FOREGROUND_COLOR_DARK, FOREGROUND_COLOR_LIGHT, FOREGROUND_COLOR_LIGHT };

   private static final I18n i18n = LocalizationHelper.getI18n(AlarmListLabelProvider.class);
   private static final String[] stateText = { i18n.tr("Outstanding"), i18n.tr("Acknowledged"), i18n.tr("Resolved"), i18n.tr("Terminated") };

	private NXCSession session;
	private Image[] stateImages = new Image[5];
	private boolean blinkState = true;
   private boolean showColor = true;
	private TreeViewer viewer;
   private BaseObjectLabelProvider objectLabelProvier;
	
	/**
	 * Default constructor 
	 */
	public AlarmListLabelProvider(TreeViewer viewer)
	{
	   this.viewer = viewer;
      session = Registry.getSession();
      objectLabelProvier = new BaseObjectLabelProvider();

      stateImages[0] = ResourceManager.getImageDescriptor("icons/alarms/outstanding.png").createImage(); //$NON-NLS-1$
      stateImages[1] = ResourceManager.getImageDescriptor("icons/alarms/acknowledged.png").createImage(); //$NON-NLS-1$
      stateImages[2] = ResourceManager.getImageDescriptor("icons/alarms/resolved.png").createImage(); //$NON-NLS-1$
      stateImages[3] = ResourceManager.getImageDescriptor("icons/alarms/terminated.png").createImage(); //$NON-NLS-1$
      stateImages[4] = ResourceManager.getImageDescriptor("icons/alarms/acknowledged_sticky.png").createImage(); //$NON-NLS-1$
	}

   /**
    * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
    */
	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		switch((Integer)viewer.getTree().getColumn(columnIndex).getData("ID"))
		{
			case AlarmList.COLUMN_SEVERITY:
				return StatusDisplayInfo.getStatusImage(((Alarm)element).getCurrentSeverity());
			case AlarmList.COLUMN_STATE:
				if (((Alarm)element).getState() == Alarm.STATE_OUTSTANDING)
					return blinkState ? stateImages[Alarm.STATE_OUTSTANDING] : SharedIcons.IMG_EMPTY;
				if ((((Alarm)element).getState() == Alarm.STATE_ACKNOWLEDGED) && ((Alarm)element).isSticky())
					return stateImages[4];
				return stateImages[((Alarm)element).getState()];
			case AlarmList.COLUMN_SOURCE:
			   AbstractObject object = session.findObjectById(((Alarm)element).getSourceObjectId());
            return (object != null) ? objectLabelProvier.getImage(object) : null;
			case AlarmList.COLUMN_COMMENTS:
            return (((Alarm)element).getCommentsCount() > 0) ? SharedIcons.IMG_COMMENTS : null;
		}
		return null;
	}

   /**
    * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
    */
	@Override
	public String getColumnText(Object element, int columnIndex)
	{
      switch((Integer)viewer.getTree().getColumn(columnIndex).getData("ID"))
		{
			case AlarmList.COLUMN_SEVERITY:
				return StatusDisplayInfo.getStatusText(((Alarm)element).getCurrentSeverity());
			case AlarmList.COLUMN_STATE:
			   int time = ((Alarm)element).getAckTime();
            String timeString = time > 0
                  ? " (" + DateFormatFactory.getDateTimeFormat().format(System.currentTimeMillis() + time * 1000) + ")" //$NON-NLS-1$ //$NON-NLS-2$
                  : ""; //$NON-NLS-1$
				return stateText[((Alarm)element).getState()] + timeString;
			case AlarmList.COLUMN_SOURCE:
				AbstractObject object = session.findObjectById(((Alarm)element).getSourceObjectId());
				return (object != null) ? object.getObjectName() : ("[" + Long.toString(((Alarm)element).getSourceObjectId()) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
         case AlarmList.COLUMN_ZONE:
            if (session.isZoningEnabled())
            {
               ZoneMember zm = session.findObjectById(((Alarm)element).getSourceObjectId(), ZoneMember.class);
               return (zm != null) ? zm.getZoneName() : "";
            }
            return "";
			case AlarmList.COLUMN_MESSAGE:
				return ((Alarm)element).getMessage();
			case AlarmList.COLUMN_COUNT:
				return Integer.toString(((Alarm)element).getRepeatCount());
			case AlarmList.COLUMN_COMMENTS:
				return (((Alarm)element).getCommentsCount() > 0) ? Integer.toString(((Alarm)element).getCommentsCount()) : null;
			case AlarmList.COLUMN_ACK_BY:
				if (((Alarm)element).getState() == Alarm.STATE_OUTSTANDING)
					return null;
				long userId = (((Alarm)element).getState() == Alarm.STATE_ACKNOWLEDGED) ? ((Alarm)element).getAcknowledgedByUser() : ((Alarm)element).getResolvedByUser();
				AbstractUserObject user = session.findUserDBObjectById(userId, new ViewerElementUpdater(viewer, element)); 
            return (user != null) ? user.getName() : ("[" + Long.toString(userId) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			case AlarmList.COLUMN_CREATED:
				return DateFormatFactory.getDateTimeFormat().format(((Alarm)element).getCreationTime());
			case AlarmList.COLUMN_LASTCHANGE:
				return DateFormatFactory.getDateTimeFormat().format(((Alarm)element).getLastChangeTime());
         case AlarmList.COLUMN_HELPDESK_REF:
            switch(((Alarm)element).getHelpdeskState())
            {
               case Alarm.HELPDESK_STATE_OPEN:
                  return ((Alarm)element).getHelpdeskReference();
               case Alarm.HELPDESK_STATE_CLOSED:
                  return ((Alarm)element).getHelpdeskReference() + i18n.tr(" (closed)");
            }
            return null;
		}
		return null;
	}

   /**
    * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
    */
	@Override
	public void dispose()
	{
		for(int i = 0; i < stateImages.length; i++)
			stateImages[i].dispose();
      objectLabelProvier.dispose();
		super.dispose();
	}

	/**
	 * Toggle blink state
	 */
	public void toggleBlinkState()
	{
		blinkState = !blinkState;
	}

   /**
    * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
    */
   @Override
   public Color getForeground(Object element)
   {
      return showColor ? FOREGROUND_COLORS[((Alarm)element).getCurrentSeverity().getValue()] : null;
   }

   /**
    * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
    */
   @Override
   public Color getBackground(Object element)
   {
      return showColor ? StatusDisplayInfo.getStatusColor(((Alarm)element).getCurrentSeverity()) : null;
   }

   /**
    * @return the showColor
    */
   public boolean isShowColor()
   {
      return showColor;
   }

   /**
    * @param showColor the showColor to set
    */
   public void setShowColor(boolean showColor)
   {
      this.showColor = showColor;
   }
}
