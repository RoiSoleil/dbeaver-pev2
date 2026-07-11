package org.eclipse.dbeaver_pev2.tests;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.ui.PlatformUI;

public class PostgreSQLConnectionHelper {

    public static void createConnection(SWTWorkbenchBot bot) {
        bot.sleep(10000);

        switchToDBeaverPerspective();
        bot.sleep(3000);

        // Ouvrir le wizard de nouvelle connexion
        Display.getDefault().asyncExec(() -> {
            try {
                PlatformUI.getWorkbench()
                    .getService(org.eclipse.ui.handlers.IHandlerService.class)
                    .executeCommand("org.jkiss.dbeaver.core.new.connection", null);
            } catch (Exception e) {
                System.out.println("Command error: " + e.getMessage());
            }
        });
        bot.sleep(5000);

        // Wizard: sélection du driver PostgreSQL
        bot.shell("Connect to a database").activate();
        bot.list().select("PostgreSQL");
        bot.sleep(1000);
        bot.button("Next >").click();
        bot.sleep(3000);

        // Gérer la popup de téléchargement du driver
        handleDriverDownload(bot);

        // Wizard: page de configuration
        bot.textWithLabel("Host").setText(
            System.getProperty("db.host", "localhost"));
        bot.textWithLabel("Database").setText(
            System.getProperty("db.database", "dbeaver_test"));
        bot.textWithLabel("User name").setText(
            System.getProperty("db.user", "test"));
        bot.textWithLabel("Password").setText(
            System.getProperty("db.password", "test"));

        bot.button("Finish").click();
        bot.sleep(5000);

        // Gérer la popup de téléchargement si elle apparaît au finish
        handleDriverDownload(bot);
    }

    private static void handleDriverDownload(SWTWorkbenchBot bot) {
        try {
            bot.shell("Driver settings").activate();
            bot.button("Download").click();
            bot.sleep(15000);
            try {
                bot.button("Finish").click();
            } catch (Exception e) {
                // Pas de bouton Finish, fermer la popup
            }
            bot.sleep(3000);
        } catch (Exception e) {
            // Pas de popup
        }
    }

    private static void switchToDBeaverPerspective() {
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
    }
}
