import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class faf {

    // 5 Hours 30 Minutes maximum budget (330 minutes)
    private static final long MAX_RUNTIME_MS = TimeUnit.MINUTES.toMillis(330);
    private static final long SLEEP_INTERVAL_MS = TimeUnit.HOURS.toMillis(1);

    public static void main(String[] args) {
        String user = System.getenv("USER_KEY");
        String pass = System.getenv("ACCESS_KEY");

        WebDriverManager.chromedriver().setup();

        long startTime = System.currentTimeMillis();
        int runCount = 1;

        System.out.println("Starting 6-hour runner loop...");

        while (true) {
            long elapsedTime = System.currentTimeMillis() - startTime;

            if (elapsedTime >= MAX_RUNTIME_MS) {
                System.out.println("Reached 5 hours 30 minutes budget limit. Exiting cleanly.");
                break;
            }

            System.out.println("\n=== Starting Execution Cycle #" + runCount + " ===");

            // Execute bot cycle with fresh browser instance
            runSingleBotCycle(user, pass);

            long timeAfterTask = System.currentTimeMillis() - startTime;

            // Stop before sleeping if 1-hour sleep exceeds the 5h 30m limit
            if (timeAfterTask + SLEEP_INTERVAL_MS >= MAX_RUNTIME_MS) {
                System.out.println("Execution #" + runCount + " complete. Next sleep would cross 5h 30m limit. Exiting runner.");
                break;
            }

            System.out.println("Cycle #" + runCount + " completed. Sleeping for 1 hour...");
            sleep(SLEEP_INTERVAL_MS);

            runCount++;
        }

        System.out.println("All cycles completed successfully.");
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

            // STEP 1: Free gems
            collectFreeGemsIfAvailable(driver);

            // STEP 2: FIRST 5 ATTACKS
            runOneFullCycle(driver);

            // STEP 3: CHANGE PACK (ONLY ONCE)
            clickChangePack(driver);

            // STEP 4: FREE GEMS AGAIN
            collectFreeGemsIfAvailable(driver);

            // STEP 5: SECOND 5 ATTACKS
            runOneFullCycle(driver);

        } catch (Exception e) {
            System.err.println("Error during bot execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up browser driver each cycle to prevent memory leaks over 6 hours
            driver.quit();
        }
    }

    // LOGIN
    private static void login(WebDriver driver, String user, String pass) {
        driver.get("https://elem.cards/login/");
        sleep(3000);

        driver.findElement(By.name("plogin")).sendKeys(user);
        driver.findElement(By.name("ppass")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        sleep(5000);
    }

    // 5 ATTACK LOOP + UPGRADE AFTER EACH ATTACK
    private static void runOneFullCycle(WebDriver driver) {
        driver.get("https://elem.cards/funnyfights/?autotune=on");
        sleep(3000);

        for (int i = 1; i <= 5; i++) {
            driver.get("https://elem.cards/funnyfights/enemy/" + i + "/");
            sleep(2500);

            List<WebElement> attackBtns = driver.findElements(
                By.xpath("//a[contains(@href,'/funnyfights/attack/') and .//span[text()='Attack!']]")
            );

            if (!attackBtns.isEmpty()) {
                driver.get(attackBtns.get(0).getAttribute("href"));
                sleep(4000);
            }

            // UPGRADE AFTER EACH ATTACK
            try {
                List<WebElement> upgradeBtns = driver.findElements(
                    By.xpath("//a[contains(@href,'/funnyfights/manage/upgrade/0/')]")
                );

                if (!upgradeBtns.isEmpty()) {
                    driver.get(upgradeBtns.get(0).getAttribute("href"));
                    sleep(2000);
                }
            } catch (Exception ignored) {}

            // BACK TO AUTOTUNE FOR NEXT ATTACK
            driver.get("https://elem.cards/funnyfights/?autotune=on");
            sleep(2000);
        }
    }

    // CHANGE PACK (ONLY ONCE)
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

    // FREE GEMS
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
