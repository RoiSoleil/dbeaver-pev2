package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.eclipse.dbeaver_pev2.Activator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceListener.PreferenceChangeEvent;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorListener;
import org.junit.Test;

public class ActivatorTest extends AbstractSWTBotTest {

  @Test
  public void testGetDefault() {
    assertNotNull(Activator.getDefault());
  }

  @Test
  public void testError() {
    Activator.error(new RuntimeException("test error"));
  }

  @Test
  public void testListenerOnDataSourceChanged() throws Exception {
    Field f = Activator.class.getDeclaredField("listener");
    f.setAccessible(true);
    SQLEditorListener l = (SQLEditorListener) f.get(null);
    l.onDataSourceChanged(new PreferenceChangeEvent(this, "prop", null, null));
  }

  @Test
  public void testPartListenerBranches() {
    Display.getDefault().syncExec(() -> {
      try {
        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        var view = page.findView("org.eclipse.ui.navigator.ProjectExplorer");
        if (view != null) page.hideView(view);
        page.showView("org.eclipse.ui.navigator.ProjectExplorer");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
}
