package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dbeaver_pev2.PEV2EditorPart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.Test;

public class PEV2EditorPartTest extends AbstractSWTBotTest {

  private static final String CONTENT = "SELECT 1\n" + "=".repeat(50) + "\n{\"Plan\": {\"Node Type\": \"Result\"}}";

  @Test
  public void openPEV2FileDirectly() throws Exception {
    String content = "SELECT 1\n" + "=".repeat(50) + "\n{\"Plan\": {\"Node Type\": \"Result\"}}";

    File tempFile = Files.createTempFile("test", ".pev2").toFile();
    tempFile.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(content.getBytes());
    }

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
    assertNotNull(pev2EditorPart.getBrowser());
  }

  @Test
  public void doSaveAsCancel() throws Exception {
    PEV2EditorPart editor = openPEV2File();
    editor.setFileChooser(() -> null);
    Display.getDefault().syncExec(editor::doSaveAs);
  }

  @Test
  public void doSaveAsSave() throws Exception {
    PEV2EditorPart editor = openPEV2File();
    File saved = Files.createTempFile("saved", ".pev2").toFile();
    saved.deleteOnExit();
    editor.setFileChooser(saved::getAbsolutePath);
    Display.getDefault().syncExec(editor::doSaveAs);
    String content = Files.readString(saved.toPath());
    assertTrue(content.contains("SELECT 1"));
    assertTrue(content.contains("Result"));
  }

  @Test
  public void doSaveAsOnUnwritableFileIsLogged() throws Exception {
    PEV2EditorPart editor = openPEV2File();
    File directory = Files.createTempDirectory("pev2").toFile();
    directory.deleteOnExit();
    editor.setFileChooser(directory::getAbsolutePath);
    Display.getDefault().syncExec(editor::doSaveAs);
    assertTrue(directory.isDirectory());
  }

  @Test
  public void editorIsNeverDirty() throws Exception {
    PEV2EditorPart editor = openPEV2File();
    Display.getDefault().syncExec(() -> {
      editor.doSave(null);
      editor.setFocus();
    });
    assertFalse(editor.isDirty());
    assertTrue(editor.isSaveAsAllowed());
  }

  @Test
  public void pev2PageIsLoaded() throws Exception {
    PEV2EditorPart editor = openPEV2File();
    bot.waitUntil(new DefaultCondition() {

      @Override
      public boolean test() throws Exception {
        return editor.isPEV2Loaded() && editor.isPlanLoaded();
      }

      @Override
      public String getFailureMessage() {
        return "PEV2 did not load the plan";
      }
    }, 10000);
  }

  @Test
  public void openWorkspaceFile() throws Exception {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("pev2");
    if (!project.exists()) {
      project.create(null);
    }
    project.open(null);
    IFile file = project.getFile("plan.pev2");
    if (!file.exists()) {
      file.create(new ByteArrayInputStream(CONTENT.getBytes()), true, null);
    }
    assertTrue(openEditor(new FileEditorInput(file)) instanceof PEV2EditorPart);
  }

  @Test
  public void fileWithoutSeparatorIsRejected() throws Exception {
    File tmp = Files.createTempFile("invalid", ".pev2").toFile();
    tmp.deleteOnExit();
    Files.writeString(tmp.toPath(), "SELECT 1");
    IEditorPart editor = openEditor(new FileStoreEditorInput(EFS.getLocalFileSystem().getStore(tmp.toURI())));
    assertFalse(editor instanceof PEV2EditorPart);
  }

  @Test
  public void unsupportedInputIsRejected() throws Exception {
    IEditorInput input = new IEditorInput() {

      @Override
      public <T> T getAdapter(Class<T> adapter) {
        return null;
      }

      @Override
      public boolean exists() {
        return true;
      }

      @Override
      public ImageDescriptor getImageDescriptor() {
        return null;
      }

      @Override
      public String getName() {
        return "unsupported.pev2";
      }

      @Override
      public IPersistableElement getPersistable() {
        return null;
      }

      @Override
      public String getToolTipText() {
        return getName();
      }
    };
    assertFalse(openEditor(input) instanceof PEV2EditorPart);
  }

  private static IEditorPart openEditor(IEditorInput input) {
    final IEditorPart[] editorRef = { null };
    Display.getDefault().syncExec(() -> {
      try {
        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        editorRef[0] = IDE.openEditor(page, input, "org.eclipse.dbeaver_pev2.PEV2Editor");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    assertNotNull(editorRef[0]);
    return editorRef[0];
  }

  private static PEV2EditorPart openPEV2File() throws Exception {
    String content = "SELECT 1\n" + "=".repeat(50) + "\n{\"Plan\": {\"Node Type\": \"Result\"}}";
    File tmp = Files.createTempFile("test", ".pev2").toFile();
    tmp.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tmp)) {
      fos.write(content.getBytes());
    }
    final IEditorPart[] editorRef = { null };
    Display.getDefault().syncExec(() -> {
      try {
        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        IFileStore fileStore = EFS.getLocalFileSystem().getStore(tmp.toURI());
        editorRef[0] = IDE.openEditor(page,
            new FileStoreEditorInput(fileStore),
            "org.eclipse.dbeaver_pev2.PEV2Editor");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    assertNotNull(editorRef[0]);
    assertTrue(editorRef[0] instanceof PEV2EditorPart);
    return (PEV2EditorPart) editorRef[0];
  }
}
