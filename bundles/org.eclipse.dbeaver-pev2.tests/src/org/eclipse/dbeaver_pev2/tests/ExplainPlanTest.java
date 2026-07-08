package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.dbeaver_pev2.PEV2TestHook;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.junit.Test;

public class ExplainPlanTest extends AbstractSWTBotTest {

    @Test
    public void explainAnalyzeOpensPEV2() {
        PostgreSQLConnectionHelper.createConnection(bot);

        bot.menu("SQL Editor")
                .menu("New SQL Script")
                .click();

        bot.styledText()
                .setText("""
                    select *
                    from person;
                    """);

        bot.toolbarButton("PEV2").click();

        bot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return bot.activeEditor()
                        .getTitle()
                        .contains("pev2");
            }

            @Override
            public String getFailureMessage() {
                return "PEV2 editor not opened";
            }
        });

        bot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return PEV2TestHook.isLoaded();
            }

            @Override
            public String getFailureMessage() {
                return "pev2.html not loaded";
            }
        });

        bot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return PEV2TestHook.isPlanLoaded();
            }

            @Override
            public String getFailureMessage() {
                return "Plan data not loaded in PEV2";
            }
        });

        assertNotNull(bot.activeEditor());
        assertTrue(PEV2TestHook.isPlanLoaded());
    }

}
