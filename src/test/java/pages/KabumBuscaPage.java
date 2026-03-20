package pages;

import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KabumBuscaPage {

	private static final String HOME_URL = "https://www.kabum.com.br/";

	private final WebDriver driver;
	private final WebDriverWait wait;

	private final By campoBusca = By.id("inputBusca");
	private final By menuSugestoes = By.cssSelector("[id$='-menu']");
	private final By opcoesSugestao = By.cssSelector("[id$='-menu'] [role='option'], [id$='-menu'] a");
	private final By tituloBusca = By.tagName("h1");

	public KabumBuscaPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	}

	public void abrirHome() {
		limparEstadoDaSessao();
		driver.get("about:blank");
		driver.get(HOME_URL);
		wait.until(ExpectedConditions.visibilityOfElementLocated(campoBusca));
	}

	public void informarProdutoNaBusca(String produto) {
		WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(campoBusca));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", input);
		((JavascriptExecutor) driver).executeScript("arguments[0].focus();", input);
		input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		input.sendKeys(produto);
		wait.until(d -> !input.getAttribute("value").isBlank());
	}

	public void selecionarPrimeiraSugestaoDaBusca() {
		String urlAntesDaBusca = getUrlAtual();
		wait.until(ExpectedConditions.visibilityOfElementLocated(menuSugestoes));
		WebElement sugestao = wait.until(d -> d.findElements(opcoesSugestao)
				.stream()
				.filter(WebElement::isDisplayed)
				.findFirst()
				.orElse(null));
		clicarComFallback(sugestao);
		if (getUrlAtual().equals(urlAntesDaBusca)) {
			wait.until(ExpectedConditions.elementToBeClickable(campoBusca)).sendKeys(Keys.ENTER);
		}
	}

	public boolean estaNaListagemDeBuscaPor(String termo) {
		String termoNormalizado = normalizar(termo);
		return wait.until(d -> {
			String url = normalizar(driver.getCurrentUrl());
			String titulo = normalizar(driver.getTitle());
			String heading = buscarTituloDaPagina();
			String pagina = normalizar(driver.getPageSource());
			return !url.equals(normalizar(HOME_URL))
					&& (url.contains(termoNormalizado)
					|| titulo.contains(termoNormalizado)
					|| heading.contains(termoNormalizado)
					|| pagina.contains(termoNormalizado));
		});
	}

	public String getUrlAtual() {
		return driver.getCurrentUrl();
	}

	private void limparEstadoDaSessao() {
		driver.manage().deleteAllCookies();
		try {
			((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
		} catch (RuntimeException ex) {
		}
	}

	private void clicarComFallback(WebElement elemento) {
		try {
			wait.until(ExpectedConditions.elementToBeClickable(elemento)).click();
		} catch (RuntimeException ex) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
		}
	}

	private String buscarTituloDaPagina() {
		try {
			return normalizar(wait.until(ExpectedConditions.visibilityOfElementLocated(tituloBusca)).getText());
		} catch (RuntimeException ex) {
			return "";
		}
	}

	private String normalizar(String valor) {
		if (valor == null) {
			return "";
		}
		return valor.trim().toLowerCase(Locale.ROOT);
	}
}
