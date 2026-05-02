import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("🚀 Selenium session started");

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing credentials");
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        Instant start = Instant.now();

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // ---------------- LOGIN ----------------
            driver.get("https://elem.cards/login/");
            sleep(2000);

            driver.findElement(By.name("plogin")).sendKeys(user);
            driver.findElement(By.name("ppass")).sendKeys(pass);
            driver.findElement(By.cssSelector("input[type='submit']")).click();

            sleep(4000);
            System.out.println("🔓 Login completed");

            // ---------------- OPEN PAGE ----------------
            List<WebElement> urfin = driver.findElements(By.cssSelector("a.urfin"));
            if (!urfin.isEmpty()) {
                urfin.get(0).click();
                System.out.println("📍 Entered section");
            }

            sleep(3000);

            // ---------------- MAIN LOOP ----------------
            while (true) {

                long mins = Duration.between(start, Instant.now()).toMinutes();
                if (mins > 300) {
                    System.out.println("⛔ Time limit reached");
                    break;
                }

                System.out.println("🔍 Scanning actions...");

                // Example: collect action links safely
                List<WebElement> actions = driver.findElements(By.cssSelector("a[href*='attack']"));

                for (WebElement a : actions) {
                    try {
                        a.click();
                        sleep(800);
                    } catch (Exception ignored) {}
                }

                clickIfExists(driver, "//span[text()='Attack']");
                handleGoldStyleAction(driver);   // 💰 example pattern
                clickIfExists(driver, "//span[text()='Next']");

                driver.navigate().refresh();
                sleep(4000);
            }

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        } finally {
            System.out.println("🧹 Closing browser");
            driver.quit();
        }
    }

    // ---------------- GOLD-LIKE ACTION HANDLER (GENERIC PATTERN) ----------------
    public static void handleGoldStyleAction(WebDriver driver) {
        try {
            List<WebElement> gold = driver.findElements(
                    By.xpath("//*[contains(text(),'Attack now for')]")
            );

            if (!gold.isEmpty()) {

                String text = gold.get(0).getText();
                String number = text.replaceAll("[^0-9]", "");

                if (!number.isEmpty()) {
                    int cost = Integer.parseInt(number);

                    System.out.println("💰 Detected cost: " + cost);

                    if (cost <= 10) {
                        gold.get(0).click();
                        sleep(1000);

                        List<WebElement> yes = driver.findElements(
                                By.xpath("//*[text()='Yes!']")
                        );

                        if (!yes.isEmpty()) {
                            yes.get(0).click();
                        }

                        System.out.println("✅ Action confirmed");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Gold action error: " + e.getMessage());
        }
    }

    // ---------------- SAFE CLICK ----------------
    public static void clickIfExists(WebDriver driver, String xpath) {
        try {
            List<WebElement> el = driver.findElements(By.xpath(xpath));
            if (!el.isEmpty()) {
                el.get(0).click();
                sleep(1000);
            }
        } catch (Exception ignored) {}
    }

    // ---------------- SLEEP ----------------
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
