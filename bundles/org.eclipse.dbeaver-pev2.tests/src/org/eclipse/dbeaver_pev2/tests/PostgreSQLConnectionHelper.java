package org.eclipse.dbeaver_pev2.tests;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.ui.PlatformUI;

public class PostgreSQLConnectionHelper {

    public static void createConnection(SWTWorkbenchBot bot) {
        bot.sleep(8000);

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
        bot.sleep(5000);

        for (int i = 0; i < 15; i++) {
            try {
                bot.menu("Database");
                break;
            } catch (Exception ex) {
                bot.sleep(2000);
            }
        }

        bot.menu("Database")
           .menu("New Database Connection")
           .click();

        bot.table().select("PostgreSQL");
        bot.button("Next >").click();

        bot.textWithLabel("Host").setText(
            System.getProperty("db.host", "localhost"));

        bot.textWithLabel("Database").setText(
            System.getProperty("db.database", "dbeaver_test"));

        bot.textWithLabel("User name").setText(
            System.getProperty("db.user", "test"));

        bot.textWithLabel("Password").setText(
            System.getProperty("db.password", "test"));

        bot.button("Finish").click();
    }

}
