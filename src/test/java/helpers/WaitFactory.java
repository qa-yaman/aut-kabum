package helpers;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class WaitFactory {

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
	private static final Duration LONG_TIMEOUT = Duration.ofSeconds(20);

	private WaitFactory() {
	}

	public static WebDriverWait explicit(WebDriver driver) {
		return new WebDriverWait(driver, DEFAULT_TIMEOUT);
	}

	public static WebDriverWait explicitLong(WebDriver driver) {
		return new WebDriverWait(driver, LONG_TIMEOUT);
	}
}
