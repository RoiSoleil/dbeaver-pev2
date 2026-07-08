package org.eclipse.dbeaver_pev2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.DBCStatistics;
import org.jkiss.dbeaver.model.sql.SQLQuery;
import org.jkiss.dbeaver.model.sql.SQLQueryListener;
import org.jkiss.dbeaver.model.sql.SQLQueryParameter;
import org.jkiss.dbeaver.model.sql.SQLQueryResult;
import org.jkiss.dbeaver.model.sql.SQLScriptElement;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetProvider;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorBase;
import org.jkiss.dbeaver.utils.RuntimeUtils;

public class OpenPEV2Handler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent executionEvent) throws ExecutionException {
    SQLEditor editor = RuntimeUtils.getObjectAdapter(HandlerUtil.getActiveEditor(executionEvent), SQLEditor.class);
    final SQLScriptElement scriptElement = editor.extractActiveQuery();
    if (scriptElement instanceof SQLQuery originalSqlQuery) {
      String text = "EXPLAIN (ANALYZE, COSTS, VERBOSE, BUFFERS, FORMAT JSON) " + scriptElement.getText();
      originalSqlQuery.setText(text);
      originalSqlQuery.setOriginalText(text);
      SQLQuery sqlQuery = new SQLQuery(scriptElement.getDataSource(), originalSqlQuery.getOriginalText(), originalSqlQuery);
      sqlQuery.setLength(originalSqlQuery.getOriginalText().length());
      parseQueryParameters(editor, sqlQuery);
      editor.processQueries(List.of(sqlQuery), false, false, false, true, new SQLQueryListener() {
        @Override
        public void onEndQuery(DBCSession session, SQLQueryResult result, DBCStatistics statistics) {
          if (result.hasResultSet()) {
            IResultSetProvider resultSetProvider = editor.getResultSetContainers().get(0);
            String sql = sqlQuery.getText();
            String plan = resultSetProvider.getResultSetController().getCurrentRow().getValues()[0].toString();
            try {
              File pev2File = Files.createTempFile("", ".pev2").toFile();
              PEV2File.write(new FileOutputStream(pev2File), sql, plan);
              Display.getDefault().asyncExec(() -> {
                IWorkbenchPage page = PlatformUI.getWorkbench()
                                                .getActiveWorkbenchWindow()
                                                .getActivePage();
                IFileStore fileStore = EFS.getLocalFileSystem()
                                          .getStore(pev2File.toURI());
                try {
                  IDE.openEditorOnFileStore(page, fileStore);
                } catch (PartInitException e) {
                  Activator.getDefault().getLog().error(e.getMessage(), e);
                }
              });
            } catch (IOException e) {
              Activator.getDefault().getLog().error(e.getMessage(), e);
            }
          }
        }

      }, null);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private void parseQueryParameters(SQLEditor editor, SQLQuery sqlQuery) {
    try {
      Method parseQueryParametersMethod = SQLEditorBase.class.getDeclaredMethod("parseQueryParameters", SQLQuery.class);
      parseQueryParametersMethod.setAccessible(true);
      sqlQuery.setParameters((List<SQLQueryParameter>) parseQueryParametersMethod.invoke(editor, sqlQuery));
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
      Activator.getDefault().getLog().error(e.getMessage(), e);
    }
  }

}
