package runner;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import io.github.bonigarcia.wdm.WebDriverManager;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "steps")
@ConfigurationParameter(
		key = PLUGIN_PROPERTY_NAME,
		value = "pretty,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
)
public class RunCucumberTest {

	private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(45);
	private static final Duration IMPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(3);
	private static final String DEFAULT_LANGUAGE = "pt-BR";

	public static WebDriver driver;

	public static void start() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver(createChromeOptions());
		configureDriver(driver);
	}

	public static void stop() {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
	}

	private static ChromeOptions createChromeOptions() {
		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", createChromePreferences());
		options.setPageLoadStrategy(PageLoadStrategy.EAGER);
		options.addArguments(
				"--remote-allow-origins=*",
				"--window-size=1920,1080",
				"--disable-blink-features=AutomationControlled",
				"--disable-notifications",
				"--disable-gpu",
				"--disable-dev-shm-usage",
				"--no-sandbox",
				"--lang=" + DEFAULT_LANGUAGE
		);
		options.setExperimentalOption("excludeSwitches", new String[] {"enable-automation"});
		options.setExperimentalOption("useAutomationExtension", false);

		if (isHeadlessEnabled()) {
			options.addArguments("--headless=new");
		}

		return options;
	}

	private static Map<String, Object> createChromePreferences() {
		Map<String, Object> preferences = new HashMap<>();
		preferences.put("profile.default_content_setting_values.notifications", 2);
		return preferences;
	}

	private static boolean isHeadlessEnabled() {
		if (Boolean.parseBoolean(System.getProperty("headed", "false"))) {
			return false;
		}

		return Boolean.parseBoolean(System.getProperty("headless", "false"));
	}

	private static void configureDriver(WebDriver webDriver) {
		webDriver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
		webDriver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIMEOUT);
	}
}
