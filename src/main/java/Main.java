import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || pass == null) {
            throw new RuntimeException("Missing GitHub Secrets");
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        try {

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // ================= LOGIN =================
            driver.get("https://elem.cards/login/");
            sleep(2000);

            driver.findElement(By.name("plogin")).sendKeys(user);
            driver.findElement(By.name("ppass")).sendKeys(pass);
            driver.findElement(By.cssSelector("input[type='submit']")).click();

            sleep(4000);

            System.out.println("🔓 Login complete");

            // ================= URFIN =================
            List<WebElement> urfin = driver.findElements(By.cssSelector("a.urfin"));

            if (urfin.isEmpty()) {
                throw new RuntimeException("Urfin not found - login failed or page changed");
            }

            urfin.get(0).click();
            sleep(3000);

            System.out.println("📍 In urfin page");

            // ================= MAIN LOOP =================
            while (true) {

                System.out.println("🔍 Checking state...");

                boolean action = false;

                // ================= ATTACK CYCLE =================
                String[] attackOrder = {"attack0", "attack1", "attack2"};

                for (String a : attackOrder) {

                    List<WebElement> cards =
                            driver.findElements(By.cssSelector("a[href*='" + a + "']"));

                    if (!cards.isEmpty()) {

                        cards.get(0).click();
                        System.out.println("⚔ clicked " + a);

                        action = true;

                        // ⭐ SMART WAIT (FIXES YOUR 10 SEC ISSUE)
                        smartWaitForCards(driver);

                    }
                }

                // ================= NORMAL ATTACK =================
                clickIfExists(driver, "//span[text()='Attack']");

                // ================= GOLD ATTACK =================
                List<WebElement> gold =
                        driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));

                if (!gold.isEmpty()) {

                    String num = gold.get(0).getText().replaceAll("[^0-9]", "");

                    if (!num.isEmpty()) {
                        int cost = Integer.parseInt(num);

                        if (cost <= 20) {

                            gold.get(0).click();
                            sleep(800);

                            clickIfExists(driver, "//span[text()='Yes!']");

                            System.out.println("💰 Gold attack used");
                            action = true;
                        }
                    }
                }

                // ================= REFRESH STRATEGY =================
                if (action) {
                    sleep(1000);
                } else {
                    sleep(2000);
                }

                driver.navigate().refresh();
            }

        } catch (Exception e) {
            System.out.println("Bot stopped: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    // ================= SMART WAIT =================
    public static void smartWaitForCards(WebDriver driver) {

        for (int i = 0; i < 10; i++) {

            sleep(1000);

            List<WebElement> cards =
                    driver.findElements(By.cssSelector("div.fb_path a.card"));

            // if new state appears early → stop waiting
            if (!cards.isEmpty()) {
                break;
            }
        }
    }

    // ================= CLICK HELPER =================
    public static void clickIfExists(WebDriver driver, String xpath) {
        try {
            List<WebElement> el = driver.findElements(By.xpath(xpath));
            if (!el.isEmpty()) {
                el.get(0).click();
                sleep(800);
            }
        } catch (Exception ignored) {}
    }

    // ================= SLEEP =================
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
