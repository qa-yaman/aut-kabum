package steps.context;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

public class DriverContext {

	private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(45);
	private static final String DEFAULT_LANGUAGE = "pt-BR";
	private static final Object CHROMEDRIVER_SETUP_LOCK = new Object();
	private static volatile boolean CHROMEDRIVER_CONFIGURADO;

	private WebDriver driver;

	public void start() {
		if (driver != null) {
			return;
		}

		garantirChromedriverConfigurado();
		driver = new ChromeDriver(createChromeOptions());
		configureDriver(driver);
	}

	public void stop() {
		if (driver == null) {
			return;
		}

		driver.quit();
		driver = null;
	}

	public WebDriver getDriver() {
		if (driver == null) {
			throw new IllegalStateException("WebDriver nao foi inicializado para este cenario.");
		}
		return driver;
	}

	private ChromeOptions createChromeOptions() {
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

	private Map<String, Object> createChromePreferences() {
		Map<String, Object> preferences = new HashMap<>();
		preferences.put("profile.default_content_setting_values.notifications", 2);
		return preferences;
	}

	private boolean isHeadlessEnabled() {
		if (Boolean.parseBoolean(System.getProperty("headed", "false"))) {
			return false;
		}

		return Boolean.parseBoolean(System.getProperty("headless", "false"));
	}

	private void configureDriver(WebDriver webDriver) {
		webDriver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
	}

	private static void garantirChromedriverConfigurado() {
		if (CHROMEDRIVER_CONFIGURADO) {
			return;
		}

		synchronized (CHROMEDRIVER_SETUP_LOCK) {
			if (!CHROMEDRIVER_CONFIGURADO) {
				WebDriverManager.chromedriver().setup();
				CHROMEDRIVER_CONFIGURADO = true;
			}
		}
	}
}
