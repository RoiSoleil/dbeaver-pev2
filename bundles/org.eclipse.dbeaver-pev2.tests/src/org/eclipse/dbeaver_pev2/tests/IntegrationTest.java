package org.eclipse.dbeaver_pev2.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.dbeaver_pev2.PEV2EditorPart;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

public class IntegrationTest extends AbstractSWTBotTest {

	@Test
	public void testExplainOnPostgreSQL() throws Exception {
		PostgreSQLConnectionHelper.createConnection(bot);

		bot.sleep(3000);

		final IEditorPart[] editorRef = { null };
		Display.getDefault().syncExec(() -> {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				editorRef[0] = page.getActiveEditor();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		assertNotNull("PEV2 editor should be active", editorRef[0]);
		assertTrue("Editor should be PEV2EditorPart", editorRef[0] instanceof PEV2EditorPart);
        bot.closeAllEditors();
	}
}
