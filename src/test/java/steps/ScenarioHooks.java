package steps;

import java.io.ByteArrayInputStream;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import steps.context.DriverContext;

public class ScenarioHooks {
	private final DriverContext driverContext;

	public ScenarioHooks(DriverContext driverContext) {
		this.driverContext = driverContext;
	}

	@Before(order = 0)
	public void iniciarDriver() {
		driverContext.start();
	}

	@After(order = 1)
	public void anexarScreenshotEmFalha(Scenario scenario) {
		if (!scenario.isFailed()) {
			return;
		}

		WebDriver driver;
		try {
			driver = driverContext.getDriver();
		} catch (IllegalStateException exception) {
			Allure.addAttachment("screenshot_error", "Driver nao foi inicializado para o cenario.");
			return;
		}

		if (!(driver instanceof TakesScreenshot)) {
			Allure.addAttachment("screenshot_error", "Driver atual nao suporta captura de screenshot.");
			return;
		}

		try {
			byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
			scenario.attach(screenshot, "image/png", "screenshot-falha");
			Allure.addAttachment(
					"Screenshot da falha",
					"image/png",
					new ByteArrayInputStream(screenshot),
					".png"
			);
			Allure.addAttachment("url_na_falha", safeCurrentUrl(driver));
		} catch (WebDriverException exception) {
			Allure.addAttachment(
					"screenshot_error",
					"Falha ao capturar screenshot: " + exception.getMessage()
			);
		}
	}

	@After(order = 0)
	public void finalizarDriver() {
		driverContext.stop();
	}

	private String safeCurrentUrl(WebDriver driver) {
		try {
			return driver.getCurrentUrl();
		} catch (WebDriverException exception) {
			return "URL indisponivel: " + exception.getMessage();
		}
	}
}
