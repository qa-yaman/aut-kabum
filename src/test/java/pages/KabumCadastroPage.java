package pages;

import java.text.Normalizer;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KabumCadastroPage {
	private static final String HOME_URL = "https://www.kabum.com.br/";

	private static final By BOTAO_CADASTRO_HEADER = By.id("linkCadastroHeader");
	private static final By BOTAO_LOGIN_HEADER = By.id("linkLoginHeader");
	private static final By DIALOG_AUTENTICACAO = By.cssSelector("[role='dialog']");
	private static final By INPUT_LOGIN = By.cssSelector("input[name='login'], input[data-testid='check-login-input']");
	private static final By BOTAO_ENTRAR = By.cssSelector("[role='dialog'] button[type='submit']");
	private static final By ELEMENTOS_CLICAVEIS = By.cssSelector(
			"button, a, label, [role='button'], [role='radio'], input[type='button'], input[type='submit']"
	);
	private static final By CAMPOS_EDITAVEIS = By.cssSelector("input, textarea");
	private static final By CHECKBOXES = By.cssSelector("input[type='checkbox']");

	private final WebDriver driver;
	private final WebDriverWait wait;

	public KabumCadastroPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
		preencher(INPUT_LOGIN, email);
	}

	public void clicarEmEntrarNoPopup() {
		WebElement botao = wait.until(ExpectedConditions.elementToBeClickable(BOTAO_ENTRAR));
		clicar(botao);
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
				|| pageText.contains("nao sou um robô")
				|| pageText.contains("sou humano")
				|| pageText.contains("verificacao de seguranca")
				|| pageText.contains("verificacao de segurança")
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
		clicarElementoPorTexto("cadastrar cpf", "cpf");
	}

	public void preencherEmailCadastro(String email) {
		preencherCampoPorPalavrasChave(email, "e-mail", "email");
	}

	public void preencherCpf(String cpf) {
		preencherCampoPorPalavrasChave(cpf, "cpf");
	}

	public void preencherCelular(String celular) {
		preencherCampoPorPalavrasChave(celular, "telefone celular", "celular", "telefone", "mobile");
	}

	public void preencherDataNascimento(String dataNascimento) {
		preencherCampoPorPalavrasChave(dataNascimento, "data de nascimento", "nascimento");
	}

	public void preencherNomeCompleto(String nomeCompleto) {
		preencherCampoPorPalavrasChave(nomeCompleto, "nome completo", "nome");
	}

	public void preencherSenha(String senha) {
		preencherCampoPorPalavrasChave(senha, "criar uma senha", "senha", "password");
	}

	public void marcarCheckboxLiEEstouDeAcordo() {
		WebElement checkbox = encontrarCheckboxPorPalavrasChave("li e estou de acordo", "politicas da empresa");
		if (!checkbox.isSelected()) {
			clicar(checkbox);
		}
	}

	public void clicarEmContinuarNoCadastro() {
		clicarElementoPorTexto("continuar");
	}

	public void aguardarEtapaEnderecoPorCep() {
		wait.until(d -> obterTextoDaPaginaNormalizado().contains("informe o endereco para receber o pedido"));
	}

	public void preencherCep(String cep) {
		preencherCampoPorPalavrasChave(cep, "cep");
	}

	public void clicarEmConfirmar() {
		clicarElementoPorTexto("confirmar");
	}

	public void preencherNumero(String numero) {
		preencherCampoPorPalavrasChave(numero, "numero");
	}

	public void preencherComplemento(String complemento) {
		preencherCampoPorPalavrasChave(complemento, "complemento");
	}

	public void aguardarMensagemSucesso(String mensagemEsperada) {
		String mensagemNormalizada = normalizar(mensagemEsperada);
		new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
			String texto = obterTextoDaPaginaNormalizado();
			return texto.contains(mensagemNormalizada) || texto.contains("usuario cadastrado com sucesso");
		});
	}

	public void aguardarAvancoDaEtapaDeCadastro() {
		new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
			String texto = obterTextoDaPaginaNormalizado();
			return texto.contains("cep")
					|| texto.contains("endereco")
					|| texto.contains("numero")
					|| !isEtapaNovoCadastroVisivel();
		});
	}

	private void preencher(By by, String valor) {
		WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
		preencher(campo, valor);
	}

	private void preencherCampoPorPalavrasChave(String valor, String... palavrasChave) {
		WebElement campo = encontrarCampoVisivelPorPalavrasChave(palavrasChave);
		preencher(campo, valor);
	}

	private void preencher(WebElement campo, String valor) {
		campo.click();
		campo.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		campo.sendKeys(Keys.DELETE);
		campo.sendKeys(valor);
	}

	private WebElement encontrarCampoVisivelPorPalavrasChave(String... palavrasChave) {
		List<WebElement> campos = driver.findElements(CAMPOS_EDITAVEIS);
		StringBuilder diagnostico = new StringBuilder();

		for (WebElement campo : campos) {
			if (!estaEditavel(campo)) {
				continue;
			}

			String assinatura = obterAssinaturaDoCampo(campo);
			diagnostico.append("[").append(assinatura).append("] ");

			for (String palavraChave : palavrasChave) {
				if (assinatura.contains(normalizar(palavraChave))) {
					return campo;
				}
			}
		}

		throw new NoSuchElementException(
				"Nenhum campo editavel foi encontrado para: "
						+ String.join(", ", palavrasChave)
						+ ". Campos visiveis identificados: "
						+ diagnostico
		);
	}

	private WebElement encontrarCheckboxPorPalavrasChave(String... palavrasChave) {
		List<WebElement> checkboxes = driver.findElements(CHECKBOXES);

		for (WebElement checkbox : checkboxes) {
			if (!estaVisivel(checkbox)) {
				continue;
			}

			String assinatura = obterAssinaturaDoCampo(checkbox);
			for (String palavraChave : palavrasChave) {
				if (assinatura.contains(normalizar(palavraChave))) {
					return checkbox;
				}
			}
		}

		throw new NoSuchElementException(
				"Nenhum checkbox correspondente foi encontrado para: " + String.join(", ", palavrasChave)
		);
	}

	private void clicarElementoPorTexto(String... textosEsperados) {
		List<WebElement> elementos = driver.findElements(ELEMENTOS_CLICAVEIS);

		for (WebElement elemento : elementos) {
			if (!estaVisivel(elemento)) {
				continue;
			}

			String textoNormalizado = obterTextoDoElemento(elemento);
			for (String textoEsperado : textosEsperados) {
				if (textoNormalizado.contains(normalizar(textoEsperado))) {
					clicar(elemento);
					return;
				}
			}
		}

		throw new NoSuchElementException(
				"Nenhum elemento clicavel foi encontrado com os textos: " + String.join(", ", textosEsperados)
		);
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

	private boolean estaEditavel(WebElement campo) {
		try {
			return estaVisivel(campo) && campo.isEnabled() && !"true".equalsIgnoreCase(campo.getAttribute("readonly"));
		} catch (Exception e) {
			return false;
		}
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

	private String obterAssinaturaDoCampo(WebElement campo) {
		String atributos = String.join(" ",
				valorSeguro(campo.getAttribute("name")),
				valorSeguro(campo.getAttribute("id")),
				valorSeguro(campo.getAttribute("placeholder")),
				valorSeguro(campo.getAttribute("aria-label")),
				valorSeguro(campo.getAttribute("type")),
				valorSeguro(campo.getAttribute("data-testid")),
				valorSeguro(campo.getAttribute("value"))
		);

		String contexto = "";
		try {
			Object resultado = ((JavascriptExecutor) driver).executeScript(
					"const e = arguments[0];"
							+ "const texts = [];"
							+ "if (e.id && window.CSS && CSS.escape) {"
							+ "  document.querySelectorAll(`label[for='${CSS.escape(e.id)}']`).forEach(label => texts.push(label.innerText || ''));"
							+ "}"
							+ "let current = e;"
							+ "for (let i = 0; i < 4 && current; i += 1) {"
							+ "  texts.push(current.innerText || '');"
							+ "  current = current.parentElement;"
							+ "}"
							+ "return texts.join(' ');",
					campo
			);
			contexto = resultado == null ? "" : resultado.toString();
		} catch (Exception ignored) {
			contexto = "";
		}

		return normalizar(atributos + " " + contexto);
	}

	private String obterTextoDoElemento(WebElement elemento) {
		String texto = elemento.getText();
		if (texto == null || texto.isBlank()) {
			texto = valorSeguro(elemento.getAttribute("value"));
		}
		return normalizar(texto);
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

	private String valorSeguro(String valor) {
		return valor == null ? "" : valor;
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
