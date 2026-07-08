package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;

public class ConnectionTest extends AbstractSWTBotTest {

    @Test
    public void openPEV2FileDirectly() throws Exception {
        String sql = "SELECT 1";
        String plan = "{\"Plan\": {\"Node Type\": \"Result\"}}";
        String content = sql + "\n" + "=".repeat(50) + "\n" + plan;

        File tempFile = Files.createTempFile("test", ".pev2").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(content.getBytes());
        }
        tempFile.deleteOnExit();

        final boolean[] opened = {false};
        Display.getDefault().syncExec(() -> {
            try {
                IWorkbenchPage page = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage();
                IFileStore fileStore = EFS.getLocalFileSystem()
                    .getStore(tempFile.toURI());
                IDE.openEditorOnFileStore(page, fileStore);
                opened[0] = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertNotNull(bot.activeEditor());
    }

}
