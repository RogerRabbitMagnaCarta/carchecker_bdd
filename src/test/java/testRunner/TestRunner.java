package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    plugin = {"pretty", "json:target/cucumber-reports/Cucumber.json","html:target/cucumber-reports/cucumber-html-report.html"},
    features = "src/test/resources/features",
    glue = "steps"
)

public class TestRunner extends AbstractTestNGCucumberTests {
}
