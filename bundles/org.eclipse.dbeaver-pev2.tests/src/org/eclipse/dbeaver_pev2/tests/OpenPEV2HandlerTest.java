package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.dbeaver_pev2.OpenPEV2Handler;
import org.eclipse.dbeaver_pev2.PEV2EditorPart;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.jkiss.dbeaver.model.sql.SQLQuery;
import org.jkiss.dbeaver.model.sql.SQLQueryListener;
import org.jkiss.dbeaver.model.sql.SQLQueryResult;
import org.jkiss.dbeaver.model.sql.SQLScriptContext;
import org.jkiss.dbeaver.model.sql.SQLScriptElement;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetContainer;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetController;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetRow;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.junit.Test;

public class OpenPEV2HandlerTest extends AbstractSWTBotTest {

  private static final String SQL = "select * from person";
  private static final String PLAN = "[{\"Plan\": {\"Node Type\": \"Result\"}}]";

  /**
   * An SQL editor which does not run the queries: it records them with their listener.
   */
  private static class RecordingSQLEditor extends SQLEditor {

    private final SQLScriptElement activeQuery;
    private List<SQLScriptElement> queries;
    private SQLQueryListener listener;

    RecordingSQLEditor(SQLScriptElement activeQuery) {
      this.activeQuery = activeQuery;
    }

    @Override
    public SQLScriptElement extractActiveQuery() {
      return activeQuery;
    }

    @Override
    public boolean processQueries(List<SQLScriptElement> queries, boolean newTab, boolean export, boolean fetchResults,
        boolean checkSession, SQLQueryListener queryListener, SQLScriptContext context) {
      this.queries = queries;
      this.listener = queryListener;
      return true;
    }

    @Override
    public List<IResultSetContainer> getResultSetContainers() {
      return List.of(resultSetContainer(PLAN));
    }
  }

  @Test
  public void explainWithAnalyzeByDefault() throws Exception {
    RecordingSQLEditor editor = execute(new SQLQuery(null, SQL), Map.of());
    assertEquals("EXPLAIN (ANALYZE, COSTS, VERBOSE, BUFFERS, FORMAT JSON) " + SQL, editor.queries.get(0).getText());
  }

  @Test
  public void explainWithoutAnalyze() throws Exception {
    RecordingSQLEditor editor = execute(new SQLQuery(null, SQL), Map.of(OpenPEV2Handler.ANALYZE_PARAMETER, "false"));
    assertEquals("EXPLAIN (COSTS, VERBOSE, FORMAT JSON) " + SQL, editor.queries.get(0).getText());
  }

  @Test
  public void nothingIsRunWithoutActiveQuery() throws Exception {
    RecordingSQLEditor editor = execute(null, Map.of());
    assertNull(editor.queries);
  }

  @Test
  public void planOpensInPEV2Editor() throws Exception {
    RecordingSQLEditor editor = execute(new SQLQuery(null, SQL), Map.of());
    SQLQueryResult result = new SQLQueryResult((SQLQuery) editor.queries.get(0));
    result.setHasResultSet(true);
    editor.listener.onEndQuery(null, result, null);
    bot.waitUntil(new DefaultCondition() {

      @Override
      public boolean test() throws Exception {
        return activeEditor() instanceof PEV2EditorPart;
      }

      @Override
      public String getFailureMessage() {
        return "The plan did not open in a PEV2 editor";
      }
    }, 10000);
  }

  @Test
  public void nothingOpensWithoutResultSet() throws Exception {
    RecordingSQLEditor editor = execute(new SQLQuery(null, SQL), Map.of());
    SQLQueryResult result = new SQLQueryResult((SQLQuery) editor.queries.get(0));
    result.setHasResultSet(false);
    editor.listener.onEndQuery(null, result, null);
    bot.sleep(500);
    assertFalse(activeEditor() instanceof PEV2EditorPart);
  }

  private static RecordingSQLEditor execute(SQLScriptElement activeQuery, Map<String, String> parameters)
      throws Exception {
    RecordingSQLEditor editor = new RecordingSQLEditor(activeQuery);
    EvaluationContext context = new EvaluationContext(null, new Object());
    context.addVariable(ISources.ACTIVE_EDITOR_NAME, editor);
    new OpenPEV2Handler().execute(new ExecutionEvent(null, parameters, null, context));
    return editor;
  }

  private static IEditorPart activeEditor() {
    return Display.getDefault().syncCall(
        () -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
  }

  private static IResultSetContainer resultSetContainer(String plan) {
    IResultSetController controller = (IResultSetController) Proxy.newProxyInstance(
        IResultSetController.class.getClassLoader(),
        new Class<?>[] { IResultSetController.class },
        (proxy, method, args) -> "getCurrentRow".equals(method.getName()) ? row(plan) : null);
    return (IResultSetContainer) Proxy.newProxyInstance(
        IResultSetContainer.class.getClassLoader(),
        new Class<?>[] { IResultSetContainer.class },
        (proxy, method, args) -> "getResultSetController".equals(method.getName()) ? controller : null);
  }

  private static ResultSetRow row(String plan) throws Exception {
    Constructor<ResultSetRow> constructor = ResultSetRow.class.getDeclaredConstructor(int.class, Object[].class);
    constructor.setAccessible(true);
    return constructor.newInstance(0, new Object[] { plan });
  }
}
