package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class StartupTest extends AbstractSWTBotTest {

    @Test
    public void shouldStartWorkbench() {
        bot.menu("Database");
        assertNotNull(
            bot.viewByTitle("Database Navigator"));
    }

}
