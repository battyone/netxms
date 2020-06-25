/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2019 Victor Kirhenshtein
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

import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.netxms.client.events.Alarm;
import org.netxms.client.objects.AbstractObject;
import org.netxms.nxmc.Registry;
import org.netxms.nxmc.localization.LocalizationHelper;
import org.netxms.nxmc.modules.objects.widgets.helpers.BaseObjectLabelProvider;
import org.netxms.nxmc.resources.ResourceManager;
import org.netxms.nxmc.resources.SharedIcons;
import org.netxms.nxmc.resources.StatusDisplayInfo;
import org.xnap.commons.i18n.I18n;

/**
 * Alarm tooltip
 */
public class AlarmToolTip extends ToolTip
{
   private static final I18n i18n = LocalizationHelper.getI18n(AlarmToolTip.class);
   private static final String[] stateImage = { "icons/outstanding.png", "icons/acknowledged.png", "icons/resolved.png", "icons/terminated.png", "icons/acknowledged_sticky.png" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
   private static final String[] stateText = { i18n.tr("Outstanding"), i18n.tr("Acknowledged"), i18n.tr("Resolved"), i18n.tr("Terminated") };
   
   private Alarm alarm;
   private BaseObjectLabelProvider objectLabelProvider = new BaseObjectLabelProvider();

   /**
    * @param control
    * @param alarm
    */
   public AlarmToolTip(Control control, Alarm alarm)
   {
      super(control, NO_RECREATE, true);
      this.alarm = alarm;
   }

   /**
    * @see org.eclipse.jface.window.ToolTip#createToolTipContentArea(org.eclipse.swt.widgets.Event,
    *      org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Composite createToolTipContentArea(Event event, Composite parent)
   {
      final Composite content = new Composite(parent, SWT.NONE);

      GridLayout layout = new GridLayout();
      layout.numColumns = 3;
      content.setLayout(layout);
      
      CLabel alarmSeverity = new CLabel(content, SWT.NONE);
      GridData gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.verticalAlignment = SWT.TOP;
      alarmSeverity.setLayoutData(gd);
      
      Label sep = new Label(content, SWT.VERTICAL | SWT.SEPARATOR);
      gd = new GridData();
      gd.verticalAlignment = SWT.FILL;
      gd.grabExcessVerticalSpace = true;
      gd.verticalSpan = 3;
      sep.setLayoutData(gd);
      
      Text alarmText = new Text(content, SWT.MULTI);
      alarmText.setEditable(false);
      gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.grabExcessHorizontalSpace = true;
      gd.verticalAlignment = SWT.FILL;
      gd.verticalSpan = 3;
      alarmText.setLayoutData(gd);

      CLabel alarmState = new CLabel(content, SWT.NONE);
      gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.verticalAlignment = SWT.TOP;
      alarmState.setLayoutData(gd);
      
      CLabel alarmSource = new CLabel(content, SWT.NONE);
      gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.verticalAlignment = SWT.TOP;
      alarmSource.setLayoutData(gd);
      
      alarmSeverity.setImage(StatusDisplayInfo.getStatusImage(alarm.getCurrentSeverity()));
      alarmSeverity.setText(StatusDisplayInfo.getStatusText(alarm.getCurrentSeverity()));
      
      int state = alarm.getState();
      if ((state == Alarm.STATE_ACKNOWLEDGED) && alarm.isSticky())
         state = Alarm.STATE_TERMINATED + 1;
      final Image image = ResourceManager.getImageDescriptor(stateImage[state]).createImage();
      alarmState.setImage(image);
      alarmState.setText(stateText[alarm.getState()]);
      
      AbstractObject object = Registry.getSession().findObjectById(alarm.getSourceObjectId());
      alarmSource.setImage((object != null) ? objectLabelProvider.getImage(object) : SharedIcons.IMG_UNKNOWN_OBJECT);
      alarmSource.setText((object != null) ? object.getObjectName() : ("[" + Long.toString(alarm.getSourceObjectId()) + "]")); //$NON-NLS-1$ //$NON-NLS-2$
      
      alarmText.setText(alarm.getMessage());
      
      content.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            objectLabelProvider.dispose();
            image.dispose();
         }
      });

      return content;
   }
}
