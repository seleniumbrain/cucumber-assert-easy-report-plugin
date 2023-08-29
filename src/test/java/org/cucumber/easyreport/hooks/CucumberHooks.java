package org.cucumber.easyreport.hooks;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.*;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCase;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import org.cucumber.easyreport.exception.EasyReportException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Field;
import java.util.List;

public class CucumberHooks {

    private static WebDriver driver;

    private int counter = 0;

    @BeforeAll
    public static void beforeAllCucumberHook() {
//        driver = WebDriverManager.chromedriver().create();
    }

    @Before
    public void easyReportInitScenario(Scenario scenario) {
//        driver.get("https://www.google.com");
    }

    @Before
    public void secondBeforeScenarioMethod() {
        System.out.println("second before scenario method");
//        throw new EasyReportException("ForceFailCheck");
    }

    @BeforeStep
    public void easyReportInitStep(Scenario scenario) {
//        throw new EasyReportException("ForceFailCheck");
    }

    @AfterStep
    public void easyReportEndStep(Scenario scenario) {

//        final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
//        final byte[] screenshot = "Screenshot Image".getBytes();
//        scenario.attach(screenshot, "image/png", "screenshot for " + getStepName(scenario));
//        scenario.attach(screenshot, "image/png", "screenshot");
        scenario.attach("test screenshot".getBytes(), "image/png", "screenshot");

        StringBuilder sb = new StringBuilder();
        sb.append("Total Number of cases = 5").append("\n")
                .append("Total Number of cases = 5").append("\n")
                .append("Total Number of cases = 5").append("\n")
                .append("Total Number of cases = 5").append("\n");
        scenario.attach(sb.toString(), "text/plain", "screenshot for " + getStepName(scenario));

//        if(getStepName(scenario).contains("check3") || getStepName(scenario).contains("process12"))
//            throw new EasyReportException("ForceFailCheck");

        counter++;
    }

    @After
    public void easyReportEndScenario(Scenario scenario) {

    }

    @AfterAll
    public static void afterAllCucumberHook() {
//        driver.quit();
    }

    @SneakyThrows
    private String getStepName(Scenario scenario) {
        Field f = scenario.getClass().getDeclaredField("delegate");
        f.setAccessible(true);
        TestCaseState tcs = (TestCaseState) f.get(scenario);

        Field f2 = tcs.getClass().getDeclaredField("testCase");
        f2.setAccessible(true);
        TestCase r = (TestCase) f2.get(tcs);

        List<PickleStepTestStep> stepDefs = r.getTestSteps().stream()
                .filter(x -> x instanceof PickleStepTestStep)
                .map(x -> (PickleStepTestStep) x)
                .toList();
        PickleStepTestStep currentStepDef = stepDefs.get(counter);
        return currentStepDef.getStep().getText();

    }
}
