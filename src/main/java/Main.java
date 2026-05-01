import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int MAX_RUN_MINUTES = 340; // stop before GitHub kills job
    private static final int REST_AFTER_MINUTES = 360; // 6 hours
    private static final int REST_DURATION_MS = 600000; // 10 min

    private static final LocalTime DAILY_STOP_START = LocalTime.of(23, 30);
    private static final LocalTime DAILY_STOP_END = LocalTime.of(1, 0);

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials");
        }

        if (isInShutdownWindow()) {
            System.out.println("Inside daily shutdown window. Exiting.");
            return;
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox",
                "--disable-dev-shm-usage", "--disable-gpu");

        WebDriver driver = new ChromeDriver(options);
        Instant startTime = Instant.now();
        Instant lastRestTime = Instant.now();

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            driver.get("https://elem.cards/login/");
            sleep(2000);

            driver.findElement(By.name("plogin")).sendKeys(user);
            driver.findElement(By.name("ppass")).sendKeys(pass);
            driver.findElement(By.cssSelector("input[type='submit']")).click();

            sleep(4000);
            driver.findElement(By.cssSelector("a.urfin")).click();
            sleep(3000);

            while (!Thread.currentThread().isInterrupted()) {

                long loopStart = System.currentTimeMillis();

                if (shouldStopNow(startTime)) {
                    System.out.println("Stopping before forced shutdown.");
                    break;
                }

                // ✅ 6-hour rest logic
                long sinceLastRest = Duration.between(lastRestTime, Instant.now()).toMinutes();
                if (sinceLastRest >= REST_AFTER_MINUTES) {
                    System.out.println("Taking 10 min rest...");
                    sleep(REST_DURATION_MS);
                    lastRestTime = Instant.now();
                }

                // -------- Collect links --------
                List<String> attackLinks = new ArrayList<>();

                for (WebElement e : driver.findElements(By.cssSelector("a[href*='attack0']")))
                    attackLinks.add(e.getAttribute("href"));

                for (WebElement e : driver.findElements(By.cssSelector("a[href*='attack1']")))
                    attackLinks.add(e.getAttribute("href"));

                for (WebElement e : driver.findElements(By.cssSelector("a[href*='attack2']")))
                    attackLinks.add(e.getAttribute("href"));

                // -------- Visit attacks --------
                for (String link : attackLinks) {
                    if (Thread.currentThread().isInterrupted()) break;
                    try {
                        driver.get(link);
                        sleep(800);
                    } catch (Exception ignored) {}
                }

                // -------- Attack --------
                clickIfExists(driver, "//span[text()='Attack']", 1500);
                handleGoldAttack(driver);
                clickIfExists(driver, "//span[text()='Next']", 1500);

                // -------- Maintain 10 sec loop --------
                long elapsed = System.currentTimeMillis() - loopStart;
                long remaining = 10000 - elapsed;
                if (remaining > 0) sleep((int) remaining);

                driver.navigate().refresh();
            }

        } catch (Exception e) {
            System.out.println("Bot stopped: " + e.getMessage());
        } finally {
            System.out.println("Closing driver...");
            try { driver.quit(); } catch (Exception ignored) {}
        }
    }

    // ---------------- Helpers ----------------

    public static boolean shouldStopNow(Instant startTime) {
        long mins = Duration.between(startTime, Instant.now()).toMinutes();
        return mins >= MAX_RUN_MINUTES || isInShutdownWindow();
    }

    public static boolean isInShutdownWindow() {
        LocalTime now = LocalTime.now(ZoneOffset.UTC);
        return !now.isBefore(DAILY_STOP_START) || now.isBefore(DAILY_STOP_END);
    }

    public static void clickIfExists(WebDriver driver, String xpath, int delay) {
        List<WebElement> el = driver.findElements(By.xpath(xpath));
        if (!el.isEmpty()) {
            try {
                el.get(0).click();
                sleep(delay);
            } catch (Exception ignored) {}
        }
    }

    public static void handleGoldAttack(WebDriver driver) {
        List<WebElement> gold = driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));
        if (!gold.isEmpty()) {
            try {
                String num = gold.get(0).getText().replaceAll("[^0-9]", "");
                if (!num.isEmpty() && Integer.parseInt(num) <= 10) {
                    gold.get(0).click();
                    sleep(1200);

                    List<WebElement> yes = driver.findElements(By.xpath("//span[text()='Yes!']"));
                    if (!yes.isEmpty()) yes.get(0).click();
                }
            } catch (Exception ignored) {}
        }
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted");
        }
    }
}
