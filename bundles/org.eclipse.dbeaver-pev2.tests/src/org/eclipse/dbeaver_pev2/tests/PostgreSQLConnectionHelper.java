package org.eclipse.dbeaver_pev2.tests;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;
import org.jkiss.dbeaver.model.app.DBPProject;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.runtime.DBWorkbench;

public class PostgreSQLConnectionHelper {

  public static void createConnection(SWTWorkbenchBot bot) throws InterruptedException, ExecutionException {
    bot.sleep(3000);
    Display.getDefault().syncExec(() -> {
      try {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                  .getActivePage().setPerspective(
                      PlatformUI.getWorkbench().getPerspectiveRegistry()
                                .findPerspectiveWithId("org.jkiss.dbeaver.core.perspective"));
      } catch (Exception e) {
        System.out.println("Perspective error: " + e.getMessage());
      }
    });
    bot.sleep(3000);

    createEclipseProject();
    loadDBeaverProjects();
    createPostgresDataSource();

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<SWTBotShell> shellFuture = executor.submit(() -> {
      Thread.sleep(3000);
      return SWTUtils.display().syncCall(() -> {
        bot.waitUntil(Conditions.shellIsActive("Driver settings"), 30_000);
        SWTBotShell shell = bot.shell("Driver settings");
        shell.activate();
        bot.sleep(1000);
        shell.pressShortcut(Keystrokes.LF);
        bot.sleep(2000);
        return shell;
      });
    });
    openSQLEditor();

    while (!shellFuture.isDone()) {
      SWTUtils.display().readAndDispatch();
    }

    bot.sleep(3000);

    selectDatasourceInEditor(bot);
    bot.sleep(1000);

    try {
      bot.styledText(0).setText("select * from person where firstname like '%test'");
    } catch (Exception e) {
      System.out.println("StyledText error: " + e.getMessage());
    }
    bot.sleep(1000);

    try {
      Display.getDefault().asyncExec(() -> {
        try {
          PlatformUI.getWorkbench()
                    .getService(org.eclipse.ui.handlers.IHandlerService.class)
                    .executeCommand("org.eclipse.dbeaver-pev2.open", null);
        } catch (Exception e) {
          System.out.println("Open SQL Editor error: " + e.getMessage());
        }
      });
    } catch (Exception e) {
      System.out.println("Keyboard shortcut error: " + e.getMessage());
    }
    bot.sleep(3000);
  }

  private static void selectDatasourceInEditor(SWTWorkbenchBot bot) {
    try {
      org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo combo = bot.comboBox();
      if (combo.itemCount() > 0) {
        combo.setSelection(0);
      }
    } catch (Exception e) {
      System.out.println("Select datasource error: " + e.getMessage());
    }
  }

  private static void createEclipseProject() {
    try {
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("General");
      if (!project.exists()) {
        NullProgressMonitor monitor = new NullProgressMonitor();
        project.create(monitor);
        project.open(monitor);
        IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription("General");
        desc.setNatureIds(new String[] { "org.jkiss.dbeaver.DBeaverNature" });
        project.setDescription(desc, monitor);
      }
    } catch (Exception e) {
      System.out.println("Eclipse project error: " + e.getMessage());
    }
  }

  private static void loadDBeaverProjects() {
    try {
      Object workspace = DBWorkbench.getPlatform().getWorkspace();
      Method loadProjects = findMethod(workspace.getClass(), "loadWorkspaceProjects");
      if (loadProjects != null) {
        loadProjects.setAccessible(true);
        loadProjects.invoke(workspace);
      }
    } catch (Exception e) {
      System.out.println("DBeaver load error: " + e.getMessage());
    }
  }

  private static void createPostgresDataSource() {
    try {
      DBPProject project = DBWorkbench.getPlatform().getWorkspace().getActiveProject();
      if (project == null) {
        project = DBWorkbench.getPlatform().getWorkspace().getProject("General");
      }
      if (project == null) {
        throw new RuntimeException("No project found");
      }

      DBPDriver driver = DBWorkbench.getPlatform().getDataSourceProviderRegistry()
                                    .findDriver("postgresql");
      if (driver == null) {
        throw new RuntimeException("PostgreSQL driver not found");
      }
      DBPConnectionConfiguration config = new DBPConnectionConfiguration();
      config.setHostName(System.getProperty("db.host", "localhost"));
      config.setHostPort("5432");
      config.setDatabaseName(System.getProperty("db.database", "dbeaver_test"));
      config.setUserName(System.getProperty("db.user", "test"));
      config.setUserPassword(System.getProperty("db.password", "test"));

      DataSourceDescriptor dataSource = (DataSourceDescriptor) project
                                                                      .getDataSourceRegistry()
                                                                      .createDataSource(driver, config);
      dataSource.setName("PostgreSQL Test");
      dataSource.setSavePassword(true);
      project.getDataSourceRegistry().updateDataSource(dataSource);
    } catch (Exception e) {
      System.out.println("DS creation error: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static void openSQLEditor() {
    Display.getDefault().asyncExec(() -> {
      try {
        PlatformUI.getWorkbench()
                  .getService(org.eclipse.ui.handlers.IHandlerService.class)
                  .executeCommand("org.jkiss.dbeaver.core.sql.editor.open", null);
      } catch (Exception e) {
        System.out.println("Open SQL Editor error: " + e.getMessage());
      }
    });
  }

  private static Method findMethod(Class<?> clazz, String name) throws NoSuchMethodException {
    while (clazz != null) {
      try {
        return clazz.getDeclaredMethod(name);
      } catch (NoSuchMethodException e) {
        clazz = clazz.getSuperclass();
      }
    }
    throw new NoSuchMethodException(name);
  }
}
