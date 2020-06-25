/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2020 Victor Kirhenshtein
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.netxms.client.NXCSession;
import org.netxms.client.events.Alarm;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.users.AbstractUserObject;
import org.netxms.nxmc.Registry;
import org.netxms.nxmc.localization.LocalizationHelper;
import org.netxms.nxmc.resources.StatusDisplayInfo;
import org.xnap.commons.i18n.I18n;

/**
 * Filter for alarm list
 */
public class AlarmListFilter extends ViewerFilter
{
   private static final I18n i18n = LocalizationHelper.getI18n(AlarmListFilter.class);
   private static final String[] stateText = { i18n.tr("Outstanding"), i18n.tr("Acknowledged"), i18n.tr("Resolved"), i18n.tr("Terminated") };
   
   private Set<Long> rootObjects = new HashSet<Long>();
   private int stateFilter = -1;
   private int severityFilter = 0xFF;
   private NXCSession session = Registry.getSession();
   private String filterString = null;

   /**
    * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
    */
   @Override
   public boolean select(Viewer viewer, Object parentElement, Object element)
   {
      if ((filterString == null) || (filterString.isEmpty()))
         return true;
      else if (checkSeverity(element))
         return true;
      else if (checkState(element))
         return true;
      else if (checkSource(element))
         return true;
      else if (checkMessage(element))
         return true;
      else if (checkCount(element))
         return true;
      else if (checkComments(element))
         return true;
      else if (checkHelpdeskId(element))
         return true;
      else if (checkResolvedBy(element))
         return true;
      return false;
   }

   /**
    * @param element Alarm selected
    * @return true if matches filter string
    */
   private boolean checkSeverity(Object element)
   {
      return StatusDisplayInfo.getStatusText(((Alarm)element).getCurrentSeverity()).toLowerCase().contains(filterString);
   }

   /**
    * @param element Alarm selected
    * @return true if matches filter string
    */
   private boolean checkState(Object element)
   {
      return stateText[((Alarm)element).getState()].toLowerCase().contains(filterString);
   }

   /**
    * @param element Alarm selected
    * @return true if matches filter string
    */
   private boolean checkSource(Object element)
   {
      AbstractObject object = session.findObjectById(((Alarm)element).getSourceObjectId());
      return (object != null) ? object.getObjectName().toLowerCase().contains(filterString) : false;
   }
   
   /**
    * @param element Alarm selected
    * @return true if matches filter string
    */
   private boolean checkMessage(Object element)
   {
      return ((Alarm)element).getMessage().toLowerCase().contains(filterString);
   }
   
   /**
    * @param element Alarm selected
    * @return true if matches filter string
    */
   private boolean checkCount(Object element)
   {
      return Integer.toString(((Alarm)element).getRepeatCount()).contains(filterString);
   }
   
   /**
    * @param element Alarm selected
    * @return true if matches filter string
    */
   private boolean checkComments(Object element)
   {
      return Integer.toString(((Alarm)element).getCommentsCount()).contains(filterString);
   }
   
   /**
    * @param element Alarm selected
    * @return true if matches filter string
    */
   private boolean checkHelpdeskId(Object element)
   {            
      switch(((Alarm)element).getHelpdeskState())
      {
         case Alarm.HELPDESK_STATE_OPEN:
            return ((Alarm)element).getHelpdeskReference().toLowerCase().contains(filterString);
         case Alarm.HELPDESK_STATE_CLOSED:
            return (((Alarm)element).getHelpdeskReference() + i18n.tr(" (closed)")).toLowerCase().contains(filterString);
         default:
            return false;
      }
   }

   /**
    * @param element Alarm selected
    * @return true if matches filter string
    */
   private boolean checkResolvedBy(Object element)
   {
      AbstractUserObject user = session.findUserDBObjectById(((Alarm)element).getAcknowledgedByUser(), null);
      return (user != null) ? user.getName().toLowerCase().contains(filterString) : false;
   }

   /**
    * @return true if alarm should be displayed
    */
   public boolean filter(Alarm alarm)
   {
      if ((stateFilter != -1) && (alarm.getState() != stateFilter))
         return false;

      if (((1 << alarm.getCurrentSeverity().getValue()) & severityFilter) == 0)
         return false;

      synchronized(this.rootObjects)
      {
         if (rootObjects.isEmpty() || (rootObjects.contains(((Alarm)alarm).getSourceObjectId())))
            return true; // No filtering by object ID or root object is a source

         AbstractObject object = session.findObjectById(alarm.getSourceObjectId());
         if (object != null)
         {
            // convert List of Longs to array of longs
            long[] rootObjectsArray = new long[rootObjects.size()];
            int i = 0;
            for(long objectId : rootObjects)
            {
               rootObjectsArray[i++] = objectId;
            }
            return object.isChildOf(rootObjectsArray);
         }
      }
      return false;
   }

   /**
    * @param rootObject the rootObject to set
    */
   public final void setRootObject(long rootObject)
   {
      synchronized(this.rootObjects)
      {
         this.rootObjects.clear();
         this.rootObjects.add(rootObject);
      }
   }

   /**
    * @param selectedObjects
    */
   public void setRootObjects(List<Long> selectedObjects)
   {
      synchronized(this.rootObjects)
      {
         this.rootObjects.clear();
         this.rootObjects.addAll(selectedObjects);
      }
   }

   /**
    * @param stateFilter the stateFilter to set
    */
   public void setStateFilter(int stateFilter)
   {
      this.stateFilter = stateFilter;
   }

   /**
    * @param severityFilter the severityFilter to set
    */
   public void setSeverityFilter(int severityFilter)
   {
      this.severityFilter = severityFilter;
   }
   
   /**
    * @return the filterString
    */
   public String getFilterString()
   {
      return filterString;
   }

   /**
    * @param filterString the filterString to set
    */
   public void setFilterString(String filterString)
   {
      this.filterString = filterString.toLowerCase();
   }
}
