package pages;

import java.util.Locale;
import helpers.WaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KabumHomePage {

	private static final String HOME_URL = "https://www.kabum.com.br/";
	private static final By HOME_READY = By.cssSelector(
			"[data-testid='header-logo'], "
					+ "[data-testid='search-input'], "
					+ "#inputBusca, header"
	);

	private final WebDriver driver;
	private final WebDriverWait wait;

	public KabumHomePage(WebDriver driver) {
		this.driver = driver;
		this.wait = WaitFactory.explicit(driver);
	}

	public void abrirHome() {
		driver.get(HOME_URL);
		wait.until(ExpectedConditions.visibilityOfElementLocated(HOME_READY));
	}

	public String getTitulo() {
		return driver.getTitle();
	}

	public boolean tituloContem(String valorEsperado) {
		return getTituloNormalizado().contains(normalizar(valorEsperado));
	}

	private String getTituloNormalizado() {
		return normalizar(getTitulo());
	}

	private String normalizar(String valor) {
		if (valor == null) {
			return "";
		}
		return valor.trim().toLowerCase(Locale.ROOT);
	}
}
