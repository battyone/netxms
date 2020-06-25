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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.netxms.client.events.Alarm;

/**
 * Content provider for alarm tree
 */
public class AlarmTreeContentProvider implements ITreeContentProvider
{
   private Map<Long, Alarm> alarms;
   
   /**
    * Create alarm tree content provider 
    */
   public AlarmTreeContentProvider()
   {
      alarms = new HashMap<Long, Alarm>();
   }
   
   /**
    * Get alarm object from local cache
    * 
    * @param id alarm ID
    * @return alarm object
    */
   private Alarm getAlarm(long id)
   {
      return alarms.get(id);
   }

   /**
    * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object[] getElements(Object inputElement)
   {
      alarms.clear();
      for(Alarm a : (List<Alarm>)inputElement)
      {
         alarms.put(a.getId(), a);
      }
      List<Alarm> rootElements = new ArrayList<Alarm>(alarms.size());
      for(Alarm a : (List<Alarm>)inputElement)
      {
         if ((a.getParentId() == 0) || !alarms.containsKey(a.getParentId()))
            rootElements.add(a);
      }
      return rootElements.toArray();
   }

   /**
    * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
    */
   @Override
   public Object[] getChildren(Object parentElement)
   {
      List<Alarm> children = new ArrayList<Alarm>();
      for(long id : ((Alarm)parentElement).getSubordinateAlarms())
      {
         Alarm a = getAlarm(id);
         if (a != null)
            children.add(a);
      }
      return children.toArray();
   }

   /**
    * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
    */
   @Override
   public Object getParent(Object element)
   {
      return (((Alarm)element).getParentId() != 0) ? getAlarm(((Alarm)element).getParentId()) : null;
   }

   /**
    * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
    */
   @Override
   public boolean hasChildren(Object element)
   {
      return ((Alarm)element).hasSubordinatedAlarms();
   }

   /**
    * @see org.eclipse.jface.viewers.IContentProvider#dispose()
    */
   @Override
   public void dispose()
   {
   }

   /**
    * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
    */
   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {
   }
}
