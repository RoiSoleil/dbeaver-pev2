package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.dbeaver_pev2.PostgreDataSourcePropertyTester;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.junit.Test;

public class PostgreDataSourcePropertyTesterTest {

  private final PostgreDataSourcePropertyTester tester = new PostgreDataSourcePropertyTester();

  @Test
  public void testReturnsFalseWhenPropertyIsWrong() {
    assertFalse(tester.test("anything", "wrongProperty", null, null));
  }

  @Test
  public void testReturnsFalseWhenReceiverIsNotSQLEditor() {
    assertFalse(tester.test("anything", "isPostgreDataSource", null, null));
  }

  @Test
  public void testReturnsFalseWhenContainerIsNull() {
    SQLEditor editor = new SQLEditor() {
      @Override
      public DBPDataSourceContainer getDataSourceContainer() {
        return null;
      }
    };
    assertFalse(tester.test(editor, "isPostgreDataSource", null, null));
  }

  @Test
  public void testReturnsFalseWhenDataSourceIsNotPostgres() {
    DBPDataSource nonPg = (DBPDataSource) Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class<?>[]{DBPDataSource.class},
        (proxy, method, args) -> defaultReturn(method));
    SQLEditor editor = createEditorWithDataSource(nonPg);
    assertFalse(tester.test(editor, "isPostgreDataSource", null, null));
  }

  @Test
  public void testReturnsTrueWhenDataSourceIsPostgres() throws Exception {
    AtomicReference<DBPDataSource> dsRef = new AtomicReference<>();
    DBPDataSourceContainer container = createContainer(dsRef);
    dsRef.set(new PostgreDataSource(container, "16", "16"));
    SQLEditor editor = createEditorWithContainer(container);
    assertTrue(tester.test(editor, "isPostgreDataSource", null, null));
  }

  private static SQLEditor createEditorWithDataSource(DBPDataSource ds) {
    return createEditorWithContainer(createContainer(new AtomicReference<>(ds)));
  }

  private static SQLEditor createEditorWithContainer(DBPDataSourceContainer container) {
    return new SQLEditor() {
      @Override
      public DBPDataSourceContainer getDataSourceContainer() {
        return container;
      }
    };
  }

  private static DBPDataSourceContainer createContainer(AtomicReference<DBPDataSource> ref) {
    return (DBPDataSourceContainer) Proxy.newProxyInstance(
        DBPDataSourceContainer.class.getClassLoader(),
        new Class<?>[]{DBPDataSourceContainer.class},
        (proxy, method, args) -> {
          String name = method.getName();
          if ("getDataSource".equals(name) && method.getParameterCount() == 0) {
            return ref.get();
          }
          if ("getPreferenceStore".equals(name)) {
            return (DBPPreferenceStore) Proxy.newProxyInstance(
                DBPPreferenceStore.class.getClassLoader(),
                new Class<?>[]{DBPPreferenceStore.class},
                (p, m, a) -> defaultReturn(m));
          }
          if ("getDriver".equals(name)) {
            return (DBPDriver) Proxy.newProxyInstance(
                DBPDriver.class.getClassLoader(),
                new Class<?>[]{DBPDriver.class},
                (p, m, a) -> "getName".equals(m.getName()) ? "test" : defaultReturn(m));
          }
          return defaultReturn(method);
        });
  }

  private static Object defaultReturn(Method method) {
    Class<?> t = method.getReturnType();
    if (t == boolean.class || t == Boolean.class) return false;
    if (t == int.class || t == Integer.class) return 0;
    if (t == long.class || t == Long.class) return 0L;
    if (t == double.class || t == Double.class) return 0.0;
    if (t == float.class || t == Float.class) return 0.0f;
    if (t == short.class || t == Short.class) return (short) 0;
    if (t == byte.class || t == Byte.class) return (byte) 0;
    if (t == char.class || t == Character.class) return (char) 0;
    return null;
  }
}
