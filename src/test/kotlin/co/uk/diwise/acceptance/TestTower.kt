package co.uk.diwise.acceptance

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@io.cucumber.junit.CucumberOptions(
    plugin = ["pretty", "html:target/cucumber", "junit:target/report/cucumber_junit_report.xml"],
    tags = "not @Ignore",
    monochrome = true,
    strict = true,
    features = ["classpath:feature"]
)
class TestTower