package pages;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import helpers.WaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KabumCadastroPage {
	private static final String HOME_URL = "https://www.kabum.com.br/";

	private static final By BOTAO_CADASTRO_HEADER = By.id("linkCadastroHeader");
	private static final By BOTAO_LOGIN_HEADER = By.id("linkLoginHeader");
	private static final By DIALOG_AUTENTICACAO = By.cssSelector("[role='dialog']");
	private static final By INPUT_LOGIN = By.cssSelector("input[name='login'], input[data-testid='check-login-input']");
	private static final By INPUT_EMAIL_CADASTRO = By.cssSelector("input[data-testid='email-input']");
	private static final By INPUT_CPF_CADASTRO = By.cssSelector("input[data-testid='cpf-input']");
	private static final By INPUT_CELULAR_CADASTRO = By.cssSelector("input[data-testid='mobile-number-input']");
	private static final By INPUT_DATA_NASCIMENTO_CADASTRO = By.cssSelector("input[data-testid='birth-date-input']");
	private static final By INPUT_NOME_COMPLETO_CADASTRO = By.cssSelector("input[data-testid='complete-name-input']");
	private static final By INPUT_SENHA_CADASTRO = By.cssSelector("input[data-testid='password-input-cpf']");
	private static final By INPUT_CEP_ENDERECO = By.cssSelector("input[name='zipcode']");
	private static final By INPUT_NUMERO_ENDERECO = By.cssSelector("input[name='number']");
	private static final By INPUT_COMPLEMENTO_ENDERECO = By.cssSelector("input[name='complement']");
	private static final By CHECKBOX_POLITICAS = By.cssSelector("input[type='checkbox'][name='policies']");
	private static final By LABEL_CHECKBOX_POLITICAS = By.xpath(
			"//label[contains(normalize-space(.), 'Li e estou de acordo')]"
	);
	private static final By BOTAO_ENTRAR = By.cssSelector("[role='dialog'] button[type='submit']");
	private static final By ELEMENTOS_CLICAVEIS = By.cssSelector(
			"button, a, label, [role='button'], [role='radio'], input[type='button'], input[type='submit']"
	);
	private static final By CAMPOS_EDITAVEIS = By.cssSelector("input, textarea");
	private static final By CHECKBOXES = By.cssSelector("input[type='checkbox']");
	private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter DATA_BR_SEM_SEPARADOR = DateTimeFormatter.ofPattern("ddMMyyyy");
	private static final DateTimeFormatter DATA_INPUT = DateTimeFormatter.ISO_LOCAL_DATE;

	private final WebDriver driver;
	private final WebDriverWait wait;
	private final WebDriverWait waitLong;

	public KabumCadastroPage(WebDriver driver) {
		this.driver = driver;
		this.wait = WaitFactory.explicit(driver);
		this.waitLong = WaitFactory.explicitLong(driver);
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
		preencher(INPUT_EMAIL_CADASTRO, email, "e-mail", "email");
	}

	public void preencherCpf(String cpf) {
		preencher(INPUT_CPF_CADASTRO, cpf, "cpf");
	}

	public void preencherCelular(String celular) {
		preencher(INPUT_CELULAR_CADASTRO, celular, "telefone celular", "celular", "telefone", "mobile");
	}

	public void preencherDataNascimento(String dataNascimento) {
		preencher(INPUT_DATA_NASCIMENTO_CADASTRO, dataNascimento, "data de nascimento", "nascimento");
	}

	public void preencherNomeCompleto(String nomeCompleto) {
		preencher(INPUT_NOME_COMPLETO_CADASTRO, nomeCompleto, "nome completo", "nome");
	}

	public void preencherSenha(String senha) {
		preencher(INPUT_SENHA_CADASTRO, senha, "criar uma senha", "senha", "password");
	}

	public void marcarCheckboxLiEEstouDeAcordo() {
		WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(CHECKBOX_POLITICAS));
		if (!isCheckboxMarcado(checkbox)) {
			WebElement label = wait.until(ExpectedConditions.presenceOfElementLocated(LABEL_CHECKBOX_POLITICAS));
			clicarCaixaDoCheckbox(label);
		}

		if (!isCheckboxMarcado(checkbox)) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
		}

		if (!isCheckboxMarcado(checkbox)) {
			((JavascriptExecutor) driver).executeScript(
					"const checkbox = arguments[0];"
							+ "checkbox.checked = true;"
							+ "checkbox.dispatchEvent(new Event('input', { bubbles: true }));"
							+ "checkbox.dispatchEvent(new Event('change', { bubbles: true }));",
					checkbox
			);
		}

		wait.until(d -> isCheckboxMarcado(checkbox));
	}

	public void clicarEmContinuarNoCadastro() {
		clicarElementoPorTexto("continuar");
	}

	public void aguardarEtapaEnderecoPorCep() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(INPUT_CEP_ENDERECO));
	}

	public void aguardarEtapaNumeroEndereco() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(INPUT_NUMERO_ENDERECO));
	}

	public void preencherCep(String cep) {
		preencher(INPUT_CEP_ENDERECO, cep, "cep");
	}

	public void clicarEmConfirmar() {
		clicarElementoPorTexto("confirmar");
	}

	public void preencherNumero(String numero) {
		preencher(INPUT_NUMERO_ENDERECO, numero, "numero");
	}

	public void preencherComplemento(String complemento) {
		preencher(INPUT_COMPLEMENTO_ENDERECO, complemento, "complemento");
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

	private void preencher(By by, String valor) {
		WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
		preencher(campo, valor);
	}

	private void preencher(By by, String valor, String... palavrasChaveFallback) {
		List<WebElement> elementos = driver.findElements(by);
		for (WebElement elemento : elementos) {
			if (estaEditavel(elemento)) {
				preencher(elemento, valor);
				return;
			}
		}

		preencherCampoPorPalavrasChave(valor, palavrasChaveFallback);
	}

	private void preencherCampoPorPalavrasChave(String valor, String... palavrasChave) {
		WebElement campo = encontrarCampoVisivelPorPalavrasChave(palavrasChave);
		preencher(campo, valor);
	}

	private void preencher(WebElement campo, String valor) {
		if ("date".equalsIgnoreCase(valorSeguro(campo.getAttribute("type")))) {
			preencherCampoData(campo, valor);
			return;
		}

		campo.click();
		campo.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		campo.sendKeys(Keys.DELETE);
		campo.sendKeys(valor);
	}

	private void preencherCampoData(WebElement campo, String valor) {
		String dataNormalizada = normalizarDataParaInput(valor);
		((JavascriptExecutor) driver).executeScript(
				"const input = arguments[0];"
						+ "const value = arguments[1];"
						+ "const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;"
						+ "input.focus();"
						+ "setter.call(input, value);"
						+ "input.dispatchEvent(new Event('input', { bubbles: true }));"
						+ "input.dispatchEvent(new Event('change', { bubbles: true }));"
						+ "input.dispatchEvent(new Event('blur', { bubbles: true }));",
				campo,
				dataNormalizada
		);
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

	private void clicarCaixaDoCheckbox(WebElement label) {
		try {
			wait.until(ExpectedConditions.visibilityOf(label));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", label);
			new Actions(driver).moveToElement(label, 12, label.getSize().getHeight() / 2).click().perform();
		} catch (Exception e) {
			((JavascriptExecutor) driver).executeScript(
					"const label = arguments[0];"
							+ "const rect = label.getBoundingClientRect();"
							+ "const x = rect.left + Math.min(16, Math.max(8, rect.width * 0.08));"
							+ "const y = rect.top + rect.height / 2;"
							+ "const target = document.elementFromPoint(x, y) || label;"
							+ "target.click();",
					label
			);
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
				valorSeguro(campo.getAttribute("autocomplete"))
		);

		String contexto = "";
		try {
			Object resultado = ((JavascriptExecutor) driver).executeScript(
					"const e = arguments[0];"
							+ "const texts = [];"
							+ "if (e.id && window.CSS && CSS.escape) {"
							+ "  document.querySelectorAll(`label[for='${CSS.escape(e.id)}']`).forEach(label => texts.push(label.innerText || ''));"
							+ "}"
							+ "const parentLabel = e.closest('label');"
							+ "if (parentLabel) { texts.push(parentLabel.innerText || ''); }"
							+ "const labelledBy = e.getAttribute('aria-labelledby');"
							+ "if (labelledBy) {"
							+ "  labelledBy.split(/\\s+/).forEach(id => {"
							+ "    const label = document.getElementById(id);"
							+ "    if (label) { texts.push(label.innerText || ''); }"
							+ "  });"
							+ "}"
							+ "const fieldset = e.closest('fieldset');"
							+ "if (fieldset) {"
							+ "  const legend = fieldset.querySelector('legend');"
							+ "  if (legend) { texts.push(legend.innerText || ''); }"
							+ "}"
							+ "let current = e;"
							+ "for (let i = 0; i < 2 && current; i += 1) {"
							+ "  const previous = current.previousElementSibling;"
							+ "  if (previous) { texts.push(previous.innerText || ''); }"
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

	private boolean isCheckboxMarcado(WebElement checkbox) {
		try {
			return checkbox.isSelected() || Boolean.TRUE.equals(
					((JavascriptExecutor) driver).executeScript("return arguments[0].checked === true;", checkbox)
			);
		} catch (Exception e) {
			return false;
		}
	}

	private String normalizarDataParaInput(String dataNascimento) {
		try {
			return LocalDate.parse(dataNascimento, DATA_BR).format(DATA_INPUT);
		} catch (DateTimeParseException e) {
			try {
				return LocalDate.parse(dataNascimento, DATA_BR_SEM_SEPARADOR).format(DATA_INPUT);
			} catch (DateTimeParseException ignored) {
				return dataNascimento;
			}
		}
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
