package helpers;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AuthContextResolver {

	private static final By MODAL_AUTENTICACAO = By.cssSelector("[role='dialog'], .ReactModal__Overlay");
	private static final By DIALOG_ROLE = By.cssSelector("[role='dialog']");
	private static final By LINK_LOGIN_HEADER = By.id("linkLoginHeader");
	private static final By INPUT_TYPE_EMAIL = By.cssSelector("input[type='email']");
	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

	public enum AuthContextSurface {
		MODAL,
		ACCOUNT_PAGE,
		INLINE_EMAIL,
		UNKNOWN
	}

	private final WebDriver driver;

	public AuthContextResolver(WebDriver driver) {
		this.driver = driver;
	}

	public AuthContextSurface detectCurrentSurface() {
		if (isAuthModalVisible()) {
			return AuthContextSurface.MODAL;
		}
		if (isAccountRouteOrCadastroDom()) {
			return AuthContextSurface.ACCOUNT_PAGE;
		}
		if (isTypeEmailInputDisplayed()) {
			return AuthContextSurface.INLINE_EMAIL;
		}
		return AuthContextSurface.UNKNOWN;
	}

	public boolean isAuthModalVisible() {
		return hasVisibleElement(MODAL_AUTENTICACAO);
	}

	public boolean isAccountRouteOrCadastroDom() {
		return currentUrlContains("/account") || pageSourceContains("Cadastro");
	}

	public boolean isTypeEmailInputDisplayed() {
		return hasVisibleElement(INPUT_TYPE_EMAIL);
	}

	public void clickLoginHeaderFallback() {
		for (WebElement link : driver.findElements(LINK_LOGIN_HEADER)) {
			if (!isVisibleAndEnabled(link)) {
				continue;
			}

			try {
				link.click();
			} catch (ElementClickInterceptedException ex) {
				driver.findElement(By.tagName("body")).click();
				link.click();
			}
			return;
		}
	}

	public void waitUntilAuthContextReady(Duration timeout) {
		WebDriverWait waitAuth = new WebDriverWait(driver, timeout);

		try {
			waitAuth.until(ExpectedConditions.or(
					ExpectedConditions.visibilityOfElementLocated(DIALOG_ROLE),
					d -> currentUrlContains("/account"),
					ExpectedConditions.visibilityOfElementLocated(INPUT_TYPE_EMAIL)
			));
		} catch (TimeoutException e) {
			clickLoginHeaderFallback();
			waitAuth.until(ExpectedConditions.visibilityOfElementLocated(INPUT_TYPE_EMAIL));
		}
	}

	public void waitUntilAuthContextReady() {
		waitUntilAuthContextReady(DEFAULT_TIMEOUT);
	}

	private boolean hasVisibleElement(By by) {
		List<WebElement> elements = driver.findElements(by);
		for (WebElement element : elements) {
			if (isVisible(element)) {
				return true;
			}
		}
		return false;
	}

	private boolean currentUrlContains(String fragment) {
		try {
			String url = driver.getCurrentUrl();
			return url != null && url.contains(fragment);
		} catch (Exception ignored) {
			return false;
		}
	}

	private boolean pageSourceContains(String fragment) {
		try {
			String source = driver.getPageSource();
			return source != null && source.contains(fragment);
		} catch (Exception ignored) {
			return false;
		}
	}

	private boolean isVisibleAndEnabled(WebElement element) {
		try {
			return element.isDisplayed() && element.isEnabled();
		} catch (Exception ignored) {
			return false;
		}
	}

	private boolean isVisible(WebElement element) {
		try {
			return element.isDisplayed();
		} catch (Exception ignored) {
			return false;
		}
	}
}
