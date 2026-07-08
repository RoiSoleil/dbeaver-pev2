package org.eclipse.dbeaver_pev2;

import org.eclipse.core.expressions.PropertyTester;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;

public class PostgreDataSourcePropertyTester extends PropertyTester {

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (!"isPostgreDataSource".equals(property)) {
      return false;
    }
    if (!(receiver instanceof SQLEditor sqlEditor)) {
      return false;
    }
    DBPDataSourceContainer container = sqlEditor.getDataSourceContainer();
    if (container == null) {
      return false;
    }
    DBPDataSource dataSource = container.getDataSource();
    return dataSource instanceof PostgreDataSource;
  }

}