package org.eclipse.dbeaver_pev2.tests;

import org.junit.Test;

public class ConnectionTest extends AbstractSWTBotTest {

    @Test
    public void createPostgreSQLConnection() {
        PostgreSQLConnectionHelper.createConnection(bot);
        bot.tree().expandNode("dbeaver_test");
    }

}
