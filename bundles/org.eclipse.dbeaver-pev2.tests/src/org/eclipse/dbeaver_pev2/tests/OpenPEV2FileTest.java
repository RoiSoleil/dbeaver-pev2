package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.dbeaver_pev2.PEV2EditorPart;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;

public class OpenPEV2FileTest extends AbstractSWTBotTest {

  private File tempFile;

  @Test
  public void openPEV2FileDirectly() throws Exception {
    String content = "SELECT 1\n" + "=".repeat(50) + "\n{\"Plan\": {\"Node Type\": \"Result\"}}";

    tempFile = Files.createTempFile("test", ".pev2").toFile();
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(content.getBytes());
    }
    tempFile.deleteOnExit();

    final IEditorPart[] editorRef = { null };
    Display.getDefault().syncExec(() -> {
      try {
        IWorkbenchPage page = PlatformUI.getWorkbench()
                                        .getActiveWorkbenchWindow().getActivePage();
        IFileStore fileStore = EFS.getLocalFileSystem()
                                  .getStore(tempFile.toURI());
        editorRef[0] = IDE.openEditor(page,
            new FileStoreEditorInput(fileStore),
            "org.eclipse.dbeaver_pev2.PEV2Editor");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    assertNotNull(editorRef[0]);
    assertTrue(editorRef[0] instanceof PEV2EditorPart);
    PEV2EditorPart pev2EditorPart = (PEV2EditorPart) editorRef[0];
    Display.getDefault().syncExec(() -> {
      bot.waitUntil(new DefaultCondition() {

        @Override
        public boolean test() throws Exception {
          return pev2EditorPart.isPlanLoaded() && tempFile.getName().equals(pev2EditorPart.getBrowser().evaluate("""
              return document.querySelector("#app > div > nav > div > div").innerText
              """));
        }

        @Override
        public String getFailureMessage() {
          return "The title is not good in PEV2";
        }
      }, 5000);
    });
  }

}
