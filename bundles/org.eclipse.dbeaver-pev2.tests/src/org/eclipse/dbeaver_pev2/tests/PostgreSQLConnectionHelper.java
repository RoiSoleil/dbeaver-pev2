package org.eclipse.dbeaver_pev2.tests;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public class PostgreSQLConnectionHelper {

    public static void createConnection(SWTWorkbenchBot bot) {
        for (int i = 0; i < 10; i++) {
            try {
                bot.menu("Database");
                break;
            } catch (Exception e) {
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
