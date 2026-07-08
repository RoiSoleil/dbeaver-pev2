package org.eclipse.dbeaver_pev2.tests;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
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
    }

}
