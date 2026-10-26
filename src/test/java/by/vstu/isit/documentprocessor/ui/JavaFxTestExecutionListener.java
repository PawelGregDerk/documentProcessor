package by.vstu.isit.documentprocessor.ui;

import javafx.application.Platform;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class JavaFxTestExecutionListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (!FxUiTestSupport.isToolkitStarted()) {
            return;
        }
        try {
            Platform.exit();
        } catch (IllegalStateException ignored) {
            // toolkit already stopped
        }
    }
}
