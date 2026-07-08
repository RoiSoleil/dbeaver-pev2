package org.eclipse.dbeaver_pev2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dbeaver_pev2.PEV2File.PEV2Content;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class PEV2EditorPart extends MultiPageEditorPart {

  private Browser browser;
  private URL fileUrl;

  private SourceViewer sourceViewer;

  private PEV2Content pev2Content;

  @Override
  protected void createPages() {
    createBrowserPage();
    createSourcePage();

    createTabToolbar();
  }

  private void createTabToolbar() {
    CTabFolder folder = (CTabFolder) getContainer();
    ToolBar toolbar = new ToolBar(folder, SWT.FLAT);
    ToolItem saveItem = new ToolItem(toolbar, SWT.PUSH);
    saveItem.setImage(
        PlatformUI.getWorkbench()
                  .getSharedImages()
                  .getImage(ISharedImages.IMG_ETOOL_SAVE_EDIT));

    saveItem.setToolTipText("Save As...");
    saveItem.addListener(SWT.Selection, e -> doSaveAs());
    folder.setTopRight(toolbar, SWT.RIGHT);
  }

  private void createBrowserPage() {
    Composite container = new Composite(getContainer(), SWT.NONE);
    container.setLayout(new FillLayout());

    browser = new Browser(container, SWT.None);
    browser.setUrl(fileUrl.toExternalForm());
    browser.addProgressListener(ProgressListener.completedAdapter(e -> browser.execute("""
        window.setPlanData('%s', `%s`, `%s`);
        """.formatted(getEditorInput().getName(),
        pev2Content.plan(),
        pev2Content.sql()))));

    int index = addPage(container);
    setPageText(index, "Plan");
  }

  private void createSourcePage() {
    Composite container = new Composite(getContainer(), SWT.NONE);
    container.setLayout(new FillLayout());

    sourceViewer = new SourceViewer(
        container,
        null,
        SWT.V_SCROLL | SWT.H_SCROLL);

    sourceViewer.configure(
        new SourceViewerConfiguration());

    sourceViewer.getTextWidget()
                .setEditable(false);

    sourceViewer.setDocument(
        new Document(pev2Content.content()));

    int index = addPage(container);
    setPageText(index, "Source");
  }

  @Override
  public void doSave(IProgressMonitor var1) {
  }

  @Override
  public void doSaveAs() {
    Shell shell = getSite().getShell();

    FileDialog dialog = new FileDialog(shell, SWT.SAVE);

    dialog.setText("Save PEV2 file");
    dialog.setFileName(getEditorInput().getName());
    dialog.setFilterExtensions("*.pev2");

    String filename = dialog.open();

    if (filename == null) {
      return;
    }

    File file = new File(filename);
    try (FileOutputStream fileOutputStream = new FileOutputStream(file); InputStream inputStream = getInputStream()) {
      inputStream.transferTo(fileOutputStream);
    } catch (IOException e) {
      Activator.error(e);
    }
  }

  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {
    Bundle bundle = FrameworkUtil.getBundle(getClass());
    URL url = FileLocator.find(bundle, new Path("pev2.html"), null);
    try {
      fileUrl = FileLocator.toFileURL(url);
    } catch (IOException e) {
      throw new PartInitException("Unable to load pev2.html", e);
    }
    this.setSite(site);
    this.setInput(input);
    readFile();
  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return true;
  }

  private void readFile() throws PartInitException {
    try {
      pev2Content = PEV2File.read(getInputStream());
    } catch (Exception e) {
      throw new PartInitException("Unable to read file : " + getEditorInput().getName(), e);
    }
  }

  private InputStream getInputStream() throws IOException {
    IEditorInput input = getEditorInput();
    URI uri = null;
    if (input instanceof IFileEditorInput fileInput) {
      uri = fileInput.getFile().getLocationURI();
    } else if (input instanceof IURIEditorInput uriInput) {
      uri = uriInput.getURI();
    } else {
      throw new IOException("Unsupported editor input: " + input.getClass());
    }
    return uri.toURL().openStream();
  }

  @Override
  public void setFocus() {
  }

}
