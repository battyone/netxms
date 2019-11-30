package org.netxms.nxmc.base.views;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class Perspective
{
   private String name;
   private PerspectiveConfiguration configuration = new PerspectiveConfiguration();
   private List<ViewWithContext> mainViews = new ArrayList<ViewWithContext>();
   private List<View> supplementaryViews = new ArrayList<View>();
   private List<NavigationView> navigationViews = new ArrayList<NavigationView>();
   private Composite content;
   private SashForm verticalSplitter;
   private SashForm horizontalSpliter;
   private CTabFolder navigationFolder;
   private CTabFolder mainFolder;
   private CTabFolder supplementaryFolder;

   protected Perspective(String name)
   {
      this.name = name;
      configurePerspective(configuration);
   }
   
   /**
    * Called by constructor to allow modification of default configuration.
    */
   protected void configurePerspective(PerspectiveConfiguration configuration)
   {
   }
   
   public void createWidgets(Composite parent)
   {
      content = new Composite(parent, SWT.NONE);
      content.setLayout(new FillLayout());

      if (configuration.hasNavigationArea)
      {
         verticalSplitter = new SashForm(content, SWT.VERTICAL);
         if (configuration.multiViewNavigationArea)
            navigationFolder = new CTabFolder(verticalSplitter, SWT.TOP);
         else
            createNavigationArea(verticalSplitter);
      }
      if (configuration.hasSupplementalArea)
      {
         horizontalSpliter = new SashForm(configuration.hasNavigationArea ? verticalSplitter : content, SWT.HORIZONTAL);
      }
      if (configuration.multiViewMainArea)
      {
         mainFolder = new CTabFolder(configuration.hasSupplementalArea ? horizontalSpliter : (configuration.hasNavigationArea ? verticalSplitter : content), SWT.TOP);
      }
      else
      {
         createMainArea(configuration.hasSupplementalArea ? horizontalSpliter : (configuration.hasNavigationArea ? verticalSplitter : content));
      }
      if (configuration.hasSupplementalArea)
      {
         if (configuration.multiViewSupplementalArea)
            supplementaryFolder = new CTabFolder(horizontalSpliter, SWT.TOP);
         else
            createSupplementalArea(horizontalSpliter);
      }
   }

   public void disposeWidgets()
   {
      if (!content.isDisposed())
         content.dispose();
   }

   /**
    * Create main area for single view perspectives
    *
    * @param parent
    */
   protected void createMainArea(Composite parent)
   {
   }

   /**
    * Create navigation area for single view perspectives
    *
    * @param parent
    */
   protected void createNavigationArea(Composite parent)
   {
   }

   /**
    * Create supplemental area for single view perspectives
    *
    * @param parent
    */
   protected void createSupplementalArea(Composite parent)
   {
   }

   /**
    * Get perspective name
    * 
    * @return perspective name
    */
   public String getName()
   {
      return name;
   }
}
