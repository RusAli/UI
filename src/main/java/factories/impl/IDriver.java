package factories.impl;

import exceptions.BrowserNotSupportedException;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.Config;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public interface IDriver {
  String REMOTE_URL = System.getProperty("webdriver.remote.url");
  boolean HEADLESS = Boolean.valueOf(System.getProperty("webdriver.headless"));

  public WebDriver newDriver();

  default URL getRemoteUrl() {
    try {
      return new URL(REMOTE_URL);
    } catch (MalformedURLException e) {
      return null;
    }
  }

  default void downloadLocalWebDriver(DriverManagerType driverType) throws BrowserNotSupportedException {
    Config wdmConfig = WebDriverManager.getInstance().config();
    wdmConfig.setAvoidBrowserDetection(true);

    String browserVersion = System.getProperty("browser.version", "");

    if (!browserVersion.isEmpty()) {
      switch (driverType) {
        case CHROME:
          wdmConfig.setChromeDriverVersion(browserVersion);
          break;
        case OPERA:
          wdmConfig.setOperaDriverVersion(browserVersion);
          break;
        default:
          throw new BrowserNotSupportedException(driverType.getBrowserName());
      }
    }

    WebDriverManager.getInstance(driverType).setup();
  }

}
