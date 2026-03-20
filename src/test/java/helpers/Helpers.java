package helpers;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Helpers {

	public static WebDriver abrirNavegador() {
		WebDriverManager.chromedriver().setup();
		WebDriver navegador = new ChromeDriver();
		navegador.manage().window().maximize();
		return navegador;
	}
}

