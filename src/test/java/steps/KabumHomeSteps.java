package steps;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import runner.RunCucumberTest;
import pages.KabumHomePage;

public class KabumHomeSteps {

	private KabumHomePage kabumHomePage;

	@Dado("que acesso o site da Kabum")
	public void queAcessoOSiteDaKabum() {
		assertNotNull(RunCucumberTest.driver, "WebDriver nao foi inicializado.");
		kabumHomePage = new KabumHomePage(RunCucumberTest.driver);
		kabumHomePage.abrirHome();
	}

	@Entao("o titulo da pagina deve conter {string}")
	public void oTituloDaPaginaDeveConter(String esperado) {
		assertNotNull(kabumHomePage, "Page Object nao foi inicializado.");
		String titulo = kabumHomePage.getTitulo();
		assertTrue(kabumHomePage.tituloContem(esperado), "Titulo esperado nao encontrado. Titulo atual: " + titulo);
	}
}

