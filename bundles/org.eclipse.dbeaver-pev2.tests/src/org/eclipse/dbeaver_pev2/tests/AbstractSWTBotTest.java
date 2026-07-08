package org.eclipse.dbeaver_pev2.tests;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public abstract class AbstractSWTBotTest {

    protected SWTWorkbenchBot bot;

    @Rule
    public TestWatcher screenshotRule = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            try {
                Files.createDirectories(Path.of("screenshots"));
                bot.captureScreenshot(
                    "screenshots/" + description.getMethodName() + ".png");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Before
    public void setup() {
        bot = new SWTWorkbenchBot();
        openDBeaverPerspective();
    }

    private void openDBeaverPerspective() {
        Display.getDefault().syncExec(() -> {
            try {
                IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
                if (window != null) {
                    PlatformUI.getWorkbench().showPerspective(
                        "org.jkiss.dbeaver.core.perspective", window);
                }
            } catch (Exception e) {
                // Perspective might not be available; tests will handle gracefully
            }
        });
    }

}
