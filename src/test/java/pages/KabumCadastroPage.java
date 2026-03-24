package pages;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import helpers.WaitFactory;
import pages.components.CadastroFormFragment;

public class KabumCadastroPage {
	private static final String HOME_URL = "https://www.kabum.com.br/";

	private static final By BOTAO_CADASTRO_HEADER = By.id("linkCadastroHeader");
	private static final By BOTAO_LOGIN_HEADER = By.id("linkLoginHeader");
	private static final By DIALOG_AUTENTICACAO = By.cssSelector("[role='dialog']");

	private final WebDriver driver;
	private final WebDriverWait wait;
	private final WebDriverWait waitLong;
	private final CadastroFormFragment formFragment;

	public KabumCadastroPage(WebDriver driver) {
		this.driver = driver;
		this.wait = WaitFactory.explicit(driver);
		this.waitLong = WaitFactory.explicitLong(driver);
		this.formFragment = new CadastroFormFragment(driver, wait);
	}

	public void abrirHome() {
		driver.get(HOME_URL);
	}

	public void aguardarHomeCarregada() {
		wait.until(ExpectedConditions.or(
				ExpectedConditions.visibilityOfElementLocated(BOTAO_CADASTRO_HEADER),
				ExpectedConditions.visibilityOfElementLocated(BOTAO_LOGIN_HEADER)
		));
	}

	public void clicarNoAcessoOuCadastroDaHome() {
		WebElement botao = encontrarPrimeiroVisivel(BOTAO_CADASTRO_HEADER, BOTAO_LOGIN_HEADER);
		clicar(botao);
	}

	public void aguardarPopupAutenticacao() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG_AUTENTICACAO));
		wait.until(d -> obterTextoDaPaginaNormalizado().contains("acesse sua conta ou cadastre-se"));
	}

	public boolean isPopupAutenticacaoVisivel() {
		return isElementoVisivel(DIALOG_AUTENTICACAO)
				&& obterTextoDaPaginaNormalizado().contains("acesse sua conta ou cadastre-se");
	}

	public void preencherEmailNoPopup(String email) {
		formFragment.preencherEmailNoPopup(email);
	}

	public void clicarEmEntrarNoPopup() {
		formFragment.clicarEmEntrarNoPopup();
	}

	public void aguardarNovaEtapaOuBloqueioDeSeguranca() {
		wait.until(d -> isBloqueioDeSegurancaVisivel() || isEtapaNovoCadastroVisivel());
	}

	public boolean isBloqueioDeSegurancaVisivel() {
		String texto = obterTextoDaPaginaNormalizado();
		return texto.contains("falha na verificacao de seguranca")
				|| texto.contains("erro no processamento do recaptcha");
	}

	public boolean isCaptchaPresent() {
		String pageSource = normalizar(obterPageSource());
		String pageText = obterTextoDaPaginaNormalizado();

		boolean temMarcadorCaptchaNoDom = pageSource.contains("g-recaptcha")
				|| pageSource.contains("grecaptcha")
				|| pageSource.contains("recaptcha/api2")
				|| pageSource.contains("recaptcha/enterprise");

		boolean temDesafioVisivel = pageText.contains("nao sou um robo")
				|| pageText.contains("sou humano")
				|| pageText.contains("verificacao de seguranca")
				|| pageText.contains("erro no processamento do recaptcha")
				|| pageText.contains("falha na verificacao de seguranca");

		return temDesafioVisivel || (temMarcadorCaptchaNoDom && isBloqueioDeSegurancaVisivel());
	}

	public boolean isEtapaNovoCadastroVisivel() {
		String texto = obterTextoDaPaginaNormalizado();
		return texto.contains("vimos que voce e novo no kabum")
				|| texto.contains("crie sua conta");
	}

	public String obterMensagemDeBloqueio() {
		String texto = obterTextoDaPaginaNormalizado();
		if (texto.contains("falha na verificacao de seguranca")) {
			return "Falha na verificacao de seguranca. Por favor, tente novamente.";
		}
		if (texto.contains("erro no processamento do recaptcha")) {
			return "Erro no processamento do ReCaptcha.";
		}
		return "Fluxo bloqueado pela camada de seguranca da KaBuM durante a validacao do e-mail.";
	}

	public void selecionarOpcaoCpf() {
		formFragment.selecionarOpcaoCpf();
	}

	public void preencherEmailCadastro(String email) {
		formFragment.preencherEmailCadastro(email);
	}

	public void preencherCpf(String cpf) {
		formFragment.preencherCpf(cpf);
	}

	public void preencherCelular(String celular) {
		formFragment.preencherCelular(celular);
	}

	public void preencherDataNascimento(String dataNascimento) {
		formFragment.preencherDataNascimento(dataNascimento);
	}

	public void preencherNomeCompleto(String nomeCompleto) {
		formFragment.preencherNomeCompleto(nomeCompleto);
	}

	public void preencherSenha(String senha) {
		formFragment.preencherSenha(senha);
	}

	public void marcarCheckboxLiEEstouDeAcordo() {
		formFragment.marcarCheckboxLiEEstouDeAcordo();
	}

	public void clicarEmContinuarNoCadastro() {
		formFragment.clicarEmContinuarNoCadastro();
	}

	public void aguardarEtapaEnderecoPorCep() {
		formFragment.aguardarEtapaEnderecoPorCep();
	}

	public void aguardarEtapaNumeroEndereco() {
		formFragment.aguardarEtapaNumeroEndereco();
	}

	public void preencherCep(String cep) {
		formFragment.preencherCep(cep);
	}

	public void clicarEmConfirmar() {
		formFragment.clicarEmConfirmar();
	}

	public void preencherNumero(String numero) {
		formFragment.preencherNumero(numero);
	}

	public void preencherComplemento(String complemento) {
		formFragment.preencherComplemento(complemento);
	}

	public void aguardarMensagemSucesso(String mensagemEsperada) {
		String mensagemNormalizada = normalizar(mensagemEsperada);
		waitLong.until(d -> {
			String texto = obterTextoDaPaginaNormalizado();
			return texto.contains(mensagemNormalizada) || texto.contains("usuario cadastrado com sucesso");
		});
	}

	public void aguardarAvancoDaEtapaDeCadastro() {
		wait.until(d -> {
			String texto = obterTextoDaPaginaNormalizado();
			return texto.contains("cep")
					|| texto.contains("endereco")
					|| texto.contains("numero")
					|| !isEtapaNovoCadastroVisivel();
		});
	}

	private WebElement encontrarPrimeiroVisivel(By... seletores) {
		for (By seletor : seletores) {
			List<WebElement> elementos = driver.findElements(seletor);
			for (WebElement elemento : elementos) {
				if (estaVisivel(elemento)) {
					return elemento;
				}
			}
		}
		throw new NoSuchElementException("Nenhum elemento visivel encontrado para os seletores informados.");
	}

	private void clicar(WebElement elemento) {
		try {
			wait.until(ExpectedConditions.visibilityOf(elemento));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", elemento);
			elemento.click();
		} catch (ElementClickInterceptedException | TimeoutException e) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
		}
	}

	private boolean isElementoVisivel(By by) {
		try {
			return driver.findElements(by).stream().anyMatch(this::estaVisivel);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean estaVisivel(WebElement elemento) {
		try {
			return elemento != null && elemento.isDisplayed();
		} catch (StaleElementReferenceException | NoSuchElementException e) {
			return false;
		}
	}

	private String obterTextoDaPagina() {
		try {
			return driver.findElement(By.tagName("body")).getText();
		} catch (Exception e) {
			return "";
		}
	}

	private String obterPageSource() {
		try {
			return driver.getPageSource();
		} catch (Exception e) {
			return "";
		}
	}

	private String obterTextoDaPaginaNormalizado() {
		return normalizar(obterTextoDaPagina());
	}

	private String normalizar(String valor) {
		if (valor == null) {
			return "";
		}

		String semAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");

		return semAcentos.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
	}
}
