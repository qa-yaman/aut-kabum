package steps;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import pages.KabumHomePage;
import steps.context.DriverContext;

public class KabumHomeSteps {
	private final DriverContext driverContext;

	private KabumHomePage kabumHomePage;

	public KabumHomeSteps(DriverContext driverContext) {
		this.driverContext = driverContext;
	}

	@Dado("que acesso o site da Kabum")
	public void queAcessoOSiteDaKabum() {
		kabumHomePage = new KabumHomePage(driverContext.getDriver());
		kabumHomePage.abrirHome();
	}

	@Entao("o titulo da pagina deve conter {string}")
	public void oTituloDaPaginaDeveConter(String esperado) {
		assertNotNull(kabumHomePage, "Page Object nao foi inicializado.");
		String titulo = kabumHomePage.getTitulo();
		assertTrue(kabumHomePage.tituloContem(esperado), "Titulo esperado nao encontrado. Titulo atual: " + titulo);
	}
}

