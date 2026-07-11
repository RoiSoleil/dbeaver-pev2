package org.eclipse.dbeaver_pev2.tests;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public class PostgreSQLConnectionHelper {

    public static void createConnection(SWTWorkbenchBot bot) {
        bot.sleep(10000);

        for (int i = 0; i < 15; i++) {
            try {
                bot.menu("File").menu("New");
                break;
            } catch (Exception ex) {
                bot.sleep(2000);
            }
        }

        bot.menu("File")
           .menu("New")
           .menu("Other...")
           .click();

        bot.sleep(2000);

        bot.tree().select("Database Connection");
        bot.button("Next >").click();

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