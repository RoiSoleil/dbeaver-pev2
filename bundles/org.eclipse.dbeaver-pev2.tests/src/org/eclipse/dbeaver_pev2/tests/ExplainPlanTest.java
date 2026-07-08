package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.dbeaver_pev2.PEV2EditorPart;
import org.eclipse.dbeaver_pev2.PEV2TestHook;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;

public class ExplainPlanTest extends AbstractSWTBotTest {

    @Test
    public void openPEV2FileAndVerifyLoading() throws Exception {
        String sql = "SELECT 1";
        String plan = "{\"Plan\": {\"Node Type\": \"Result\"}}";
        String content = sql + "\n" + "=".repeat(50) + "\n" + plan;

        File tempFile = Files.createTempFile("test", ".pev2").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(content.getBytes());
        }
        tempFile.deleteOnExit();

        final boolean[] editorOpened = {false};
        Display.getDefault().syncExec(() -> {
            try {
                IWorkbenchPage page = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage();
                IFileStore fileStore = EFS.getLocalFileSystem()
                    .getStore(tempFile.toURI());
                IDE.openEditorOnFileStore(page, fileStore);
                IEditorPart editor = page.getActiveEditor();
                editorOpened[0] = editor instanceof PEV2EditorPart;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue("PEV2 editor should have opened", editorOpened[0]);

        bot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return PEV2TestHook.isLoaded();
            }

            @Override
            public String getFailureMessage() {
                return "pev2.html not loaded in PEV2 editor";
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

        assertTrue(PEV2TestHook.isPlanLoaded());
    }

}
