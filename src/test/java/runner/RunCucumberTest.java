package runner;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

@CucumberOptions(
		features = "src/test/resources/features",
		glue = "steps",
		plugin = {
				"pretty",
				"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
		}
)
@RunWith(Cucumber.class)
public class RunCucumberTest {

	private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(45);
	private static final Duration IMPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(3);
	private static final String DEFAULT_LANGUAGE = "pt-BR";

	public static WebDriver driver;

	@BeforeClass
	public static void start() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver(createChromeOptions());
		configureDriver(driver);
	}

	@AfterClass
	public static void stop() {
		if (driver != null) {
			driver.quit();
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
		preferences.put("profile.managed_default_content_settings.images", 2);
		preferences.put("profile.default_content_setting_values.notifications", 2);
		return preferences;
	}

	private static boolean isHeadlessEnabled() {
		return Boolean.parseBoolean(System.getProperty("headless", "true"));
	}

	private static void configureDriver(WebDriver webDriver) {
		webDriver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
		webDriver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIMEOUT);
		webDriver.manage().deleteAllCookies();
	}
}
