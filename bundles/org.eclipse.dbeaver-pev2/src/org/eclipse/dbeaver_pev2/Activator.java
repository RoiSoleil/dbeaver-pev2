package org.eclipse.dbeaver_pev2;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IEvaluationService;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceListener.PreferenceChangeEvent;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.model.sql.SQLQuery;
import org.jkiss.dbeaver.model.sql.SQLQueryResult;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetModel;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorListener;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.dbeaver-pev2"; //$NON-NLS-1$

  // The shared instance
  private static Activator plugin;

  private static final SQLEditorListener listener = new SQLEditorListener() {

    @Override
    public void onDataSourceChanged(PreferenceChangeEvent event) {
      PlatformUI.getWorkbench()
                .getService(IEvaluationService.class)
                .requestEvaluation(
                    "org.eclipse.dbeaver-pev2.isPostgreDataSource");
    }

    @Override
    public void afterQueryExecute(boolean var1, boolean var2) {
    }

    @Override
    public void beforeQueryExecute(boolean var1, boolean var2) {
    }

    @Override
    public void beforeQueryPlanExplain() {
    }

    @Override
    public void onConnect(DBPDataSourceContainer var1) {
    }

    @Override
    public void onDataReceived(DBPPreferenceStore var1, ResultSetModel var2, String var3) {
    }

    @Override
    public void onDisconnect(DBPDataSourceContainer var1) {
    }

    @Override
    public void onQueryChange(SQLQuery var1, SQLQuery var2) {
    }

    @Override
    public void onQueryResult(DBPPreferenceStore var1, SQLQueryResult var2) {
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
