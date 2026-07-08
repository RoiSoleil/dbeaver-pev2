package org.eclipse.dbeaver_pev2;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
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
    return false;
  }

  private void readFile() throws PartInitException {
    try {
      pev2Content = PEV2File.read(getInputStream());
    } catch (Exception e) {
      throw new PartInitException("Unable to read file : " + getEditorInput().getName(), e);
    }
  }

  private InputStream getInputStream() throws PartInitException, MalformedURLException, IOException {
    IEditorInput input = getEditorInput();
    URI uri = null;
    if (input instanceof IFileEditorInput fileInput) {
      uri = fileInput.getFile().getLocationURI();
    } else if (input instanceof IURIEditorInput uriInput) {
      uri = uriInput.getURI();
    } else {
      throw new PartInitException("Unsupported editor input: " + input.getClass());
    }
    return uri.toURL().openStream();
  }

  @Override
  public void setFocus() {
  }

}
