package org.eclipse.dbeaver_pev2;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IEvaluationService;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceListener.PreferenceChangeEvent;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorListener;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorListenerDefault;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.dbeaver-pev2"; //$NON-NLS-1$

  // The shared instance
  private static Activator plugin;

  private static final SQLEditorListener listener = new SQLEditorListenerDefault() {

    @Override
    public void onDataSourceChanged(PreferenceChangeEvent event) {
      PlatformUI.getWorkbench()
                .getService(IEvaluationService.class)
                .requestEvaluation(
                    "org.eclipse.dbeaver-pev2.isPostgreDataSource");
    }

  };

  /**
   * The constructor
   */
  public Activator() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    Display.getDefault().syncExec(() -> {
      IWorkbenchWindow window = PlatformUI.getWorkbench()
                                          .getActiveWorkbenchWindow();
      window.getPartService().addPartListener(new IPartListener2() {

        @Override
        public void partOpened(IWorkbenchPartReference partRef) {
          if (partRef.getPart(false) instanceof SQLEditor editor) {
            editor.addListener(listener);
          }
        }

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {
          if (partRef.getPart(false) instanceof SQLEditor editor) {
            editor.addListener(listener);
          }
        }

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
          if (partRef.getPart(false) instanceof SQLEditor editor) {
            editor.removeListener(listener);
          }
        }
      });
    });
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  public static void error(Throwable t) {
    getDefault().getLog().error(t.getLocalizedMessage(), t);
  }
}
