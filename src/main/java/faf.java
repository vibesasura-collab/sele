import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class faf {

    private static final long MAX_RUNTIME_MS = TimeUnit.MINUTES.toMillis(330);
    private static final long SLEEP_INTERVAL_MS = TimeUnit.HOURS.toMillis(1);

    public static void main(String[] args) {
        String user = System.getenv("USER_KEY");
        String pass = System.getenv("ACCESS_KEY");

        WebDriverManager.chromedriver().setup();

        long startTime = System.currentTimeMillis();
        int runCount = 1;

        while (true) {
            long elapsedTime = System.currentTimeMillis() - startTime;

            if (elapsedTime >= MAX_RUNTIME_MS) {
                break;
            }

            runSingleBotCycle(user, pass);

            long timeAfterTask = System.currentTimeMillis() - startTime;

            if (timeAfterTask + SLEEP_INTERVAL_MS >= MAX_RUNTIME_MS) {
                break;
            }

            sleep(SLEEP_INTERVAL_MS);

            runCount++;
        }
    }

    private static void runSingleBotCycle(String user, String pass) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        try {
            login(driver, user, pass);

            for (int batch = 1; batch <= 4; batch++) {
                collectFreeGemsIfAvailable(driver);
                runOneFullCycle(driver);

                if (batch < 4) {
                    clickChangePack(driver);
                }
            }

        } catch (Exception ignored) {
        } finally {
            driver.quit();
        }
    }

    private static void login(WebDriver driver, String user, String pass) {
        driver.get("https://elem.cards/login/");
        sleep(3000);

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(5000);
    }

    private static void runOneFullCycle(WebDriver driver) {
        driver.get("https://elem.cards/funnyfights/?autotune=on");
        sleep(3000);

        for (int i = 1; i <= 6; i++) {
            driver.get("https://elem.cards/funnyfights/enemy/" + i + "/");
            sleep(2500);

            List<WebElement> attackBtns = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/attack/') and .//span[text()='Attack!']]")
            );

            if (!attackBtns.isEmpty()) {
                driver.get(attackBtns.get(0).getAttribute("href"));
                sleep(4000);
            }

            try {
                List<WebElement> upgradeBtns = driver.findElements(
                    By.xpath("//a[contains(@href,'/funnyfights/manage/upgrade/0/')]")
                );

                if (!upgradeBtns.isEmpty()) {
                    driver.get(upgradeBtns.get(0).getAttribute("href"));
                    sleep(2000);
                }
            } catch (Exception ignored) {}

            driver.get("https://elem.cards/funnyfights/?autotune=on");
            sleep(2000);
        }
    }

    private static void clickChangePack(WebDriver driver) {
        driver.get("https://elem.cards/funnyfights/?autotune=on");
        sleep(3000);

        List<WebElement> btn = driver.findElements(
            By.xpath("//a[contains(@href,'/funnyfights/nextpack/')]")
        );

        if (!btn.isEmpty()) {
            try {
                btn.get(0).click();
            } catch (Exception e) {
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", btn.get(0));
            }
            sleep(4000);
        }
    }

    private static void collectFreeGemsIfAvailable(WebDriver driver) {
        try {
            driver.get("https://elem.cards/funnyfights/");
            sleep(2500);

            List<WebElement> btn = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/freegems/')]")
            );

            if (!btn.isEmpty()) {
                driver.get(btn.get(0).getAttribute("href"));
                sleep(2000);
            }
        } catch (Exception ignored) {}
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
