package com.fundingsocieties.driver;


import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;

@Slf4j
public class DriverUtils {
    private static WebDriver driver;

    public static void startBrowser(final String browser) {
        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver();
                break;

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                break;

            case "safari":
                WebDriverManager.safaridriver().setup();
                driver = new SafariDriver();
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                break;

            default:
                System.out.println("Browser: " + browser + " is invalid, Launching Chrome as browser of choice...");
                driver = new ChromeDriver();
        }
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static Object execJavaScript(final String script, final Object... obj) {
        return ((JavascriptExecutor) driver).executeScript(script, obj);
    }

    /**
     * Scroll to a specific position
     *
     * @param targetPoint target position
     */
    public static void scrollTo(final Point targetPoint) {
        try {
            log.info(String.format("Scroll to %s - %s", targetPoint.getX(), targetPoint.getY()));
            final JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(String.format("window.scrollTo(%s, %s)", targetPoint.getX(), targetPoint.getY()));
        } catch (final Exception e) {
            log.warn("Exception occurred when scrolling to '{}': {}", targetPoint, e);
        }
    }

    public static void navigateTo(final String url) {
        getDriver().navigate().to(url);
    }
}
