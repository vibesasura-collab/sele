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
            throw new RuntimeException("Missing credentials");
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // ⚡ FAST + STABLE GITHUB MODE
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // ======================
            // LOGIN
            // ======================
            driver.get("https://elem.cards/login/");
            sleep(2000);

            driver.findElement(By.name("plogin")).sendKeys(user);
            driver.findElement(By.name("ppass")).sendKeys(pass);
            driver.findElement(By.cssSelector("input[type='submit']")).click();

            sleep(4000);

            System.out.println("🔓 Login complete");

            // ======================
            // URFIN PAGE
            // ======================
            driver.findElement(By.cssSelector("a.urfin")).click();
            sleep(3000);

            System.out.println("📍 In urfin page");

            // ======================
            // MAIN LOOP (STABLE)
            // ======================
            while (true) {

                System.out.println("🔍 Checking state...");

                boolean action = false;

                // ======================
                // WAIT FOR BATTLE CARDS
                // ======================
                List<WebElement> cards =
                        driver.findElements(By.cssSelector("div.fb_path a.card"));

                if (!cards.isEmpty()) {
                    System.out.println("⚔ Battle found: " + cards.size());

                    cards.get(0).click();
                    sleep(800);
                    action = true;
                }

                // ======================
                // ATTACK BUTTON
                // ======================
                List<WebElement> attack =
                        driver.findElements(By.cssSelector("a[href*='/urfin/start']"));

                if (!attack.isEmpty()) {
                    attack.get(0).click();
                    System.out.println("⚔ Attack clicked");
                    sleep(1200);
                    action = true;
                }

                // ======================
                // GOLD ATTACK SAFE CHECK
                // ======================
                List<WebElement> gold =
                        driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));

                if (!gold.isEmpty()) {

                    String txt = gold.get(0).getText();
                    String num = txt.replaceAll("[^0-9]", "");

                    if (!num.isEmpty()) {

                        int cost = Integer.parseInt(num);

                        System.out.println("💰 Gold cost: " + cost);

                        if (cost <= 20) {
                            gold.get(0).click();
                            sleep(800);

                            List<WebElement> yes =
                                    driver.findElements(By.xpath("//span[text()='Yes!']"));

                            if (!yes.isEmpty()) {
                                yes.get(0).click();
                            }

                            System.out.println("💰 Gold attack used");
                            action = true;
                        }
                    }
                }

                // ======================
                // REFRESH STRATEGY
                // ======================
                if (action) {
                    driver.navigate().refresh();
                    sleep(2000);
                } else {
                    sleep(5000);
                    driver.navigate().refresh();
                }
            }

        } catch (Exception e) {
            System.out.println("Bot crashed: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
