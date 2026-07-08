package org.eclipse.dbeaver_pev2;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class PEV2TestHook {

    public static boolean isLoaded() {
        PEV2EditorPart editor = findEditor();
        return editor != null && editor.isPEV2Loaded();
    }

    public static boolean isPlanLoaded() {
        PEV2EditorPart editor = findEditor();
        return editor != null && editor.isPlanLoaded();
    }

    private static PEV2EditorPart findEditor() {
        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        if (page == null) return null;
        IEditorReference[] refs = page.getEditorReferences();
        for (IEditorReference ref : refs) {
            IEditorPart editor = ref.getEditor(false);
            if (editor instanceof PEV2EditorPart pev2Editor) {
                return pev2Editor;
            }
        }
        return null;
    }

}
