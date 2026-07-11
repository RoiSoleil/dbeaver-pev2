package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.dbeaver_pev2.PEV2EditorPart;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

public class IntegrationTest extends AbstractSWTBotTest {

    @Test
    public void testExplainOnPostgreSQL() throws Exception {
        PostgreSQLConnectionHelper.createConnection(bot);

        bot.sleep(5000);

        SWTBotTree tree = bot.viewByTitle("Connections").bot().tree();
        SWTBotTreeItem connection = tree.getAllItems()[0];
        connection.doubleClick();
        bot.sleep(5000);

        // Gérer la popup de téléchargement du driver si elle apparaît
        handleDriverDownload();

        bot.styledText(0).setText("EXPLAIN (FORMAT JSON) SELECT * FROM person");
        bot.sleep(1000);

        bot.toolbarButtonWithTooltip("Execute SQL Statement").click();
        bot.sleep(5000);

        // Gérer la popup de téléchargement du driver si elle apparaît
        handleDriverDownload();

        bot.toolbarButtonWithTooltip("Open explain plan in PEV2").click();
        bot.sleep(3000);

        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = page.getActiveEditor();
        assertNotNull("PEV2 editor should be active", editor);
        assertTrue("Editor should be PEV2EditorPart",
            editor instanceof PEV2EditorPart);
    }

    private void handleDriverDownload() {
        try {
            bot.shell("Driver settings").activate();
            bot.button("Download").click();
            bot.sleep(15000);
            try {
                bot.shell("Driver settings").close();
            } catch (Exception e) {
                // Déjà fermée automatiquement
            }
            bot.sleep(3000);
        } catch (Exception e) {
            // Pas de popup de téléchargement, continuer
        }
    }
}
