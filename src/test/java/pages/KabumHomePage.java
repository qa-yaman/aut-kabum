package pages;

import java.util.Locale;
import helpers.WaitFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KabumHomePage {

	private static final String HOME_URL = "https://www.kabum.com.br/";

	private final WebDriver driver;
	private final WebDriverWait wait;

	public KabumHomePage(WebDriver driver) {
		this.driver = driver;
		this.wait = WaitFactory.explicit(driver);
	}

	public void abrirHome() {
		driver.get(HOME_URL);
		wait.until(d -> getTituloNormalizado().contains("kabum"));
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
