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

        System.out.println("🚀 Selenium Session Started");

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
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(4));

            // ---------------- LOGIN ----------------
            driver.get("https://elem.cards/login/");
            sleep(2000);

            driver.findElement(By.name("plogin")).sendKeys(user);
            driver.findElement(By.name("ppass")).sendKeys(pass);
            driver.findElement(By.cssSelector("input[type='submit']")).click();

            sleep(4000);
            System.out.println("🔓 Login complete");

            // ---------------- ENTER URFIN ----------------
            List<WebElement> urfin = driver.findElements(By.cssSelector("a.urfin"));

            if (!urfin.isEmpty()) {
                urfin.get(0).click();
                sleep(4000);
            }

            System.out.println("📍 In urfin page");

            // ---------------- MAIN LOOP ----------------
            while (true) {

                long mins = Duration.between(start, Instant.now()).toMinutes();
                if (mins > 330) {
                    System.out.println("⛔ Time limit reached");
                    break;
                }

                System.out.println("🔍 Checking state...");

                // ---------------- BATTLE CARDS ----------------
                List<WebElement> attacks =
                        driver.findElements(By.cssSelector("div.fb_path a.card"));

                if (!attacks.isEmpty()) {

                    System.out.println("⚔️ Battle cards found: " + attacks.size());

                    for (WebElement card : attacks) {
                        try {
                            card.click();
                            sleep(800);
                        } catch (Exception ignored) {}
                    }

                    click(driver, "//span[text()='Attack']");
                    click(driver, "//span[text()='Next']");

                    driver.navigate().refresh();
                    sleep(2000);

                } else {

                    // ---------------- GOLD CHECK ----------------
                    List<WebElement> gold =
                            driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));

                    if (!gold.isEmpty()) {

                        String text = gold.get(0).getText();
                        String num = text.replaceAll("[^0-9]", "");

                        if (!num.isEmpty()) {
                            int cost = Integer.parseInt(num);

                            System.out.println("💰 Gold cost: " + cost);

                            if (cost <= 10) {
                                gold.get(0).click();
                                sleep(1000);

                                List<WebElement> yes =
                                        driver.findElements(By.xpath("//span[text()='Yes!']"));

                                if (!yes.isEmpty()) {
                                    yes.get(0).click();
                                }

                                System.out.println("✅ Gold used");
                            } else {
                                System.out.println("❌ Gold too expensive");
                            }
                        }

                    } else {

                        // ---------------- COOLDOWN / NO BOSS ----------------
                        System.out.println("⏳ No battle available");

                        try {
                            WebElement cd =
                                    driver.findElement(By.id("urfin_cooldown"));

                            System.out.println("Cooldown: " + cd.getText());

                        } catch (Exception ignored) {}

                        sleep(5000);
                        driver.navigate().refresh();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        } finally {
            System.out.println("🧹 Closing driver");
            driver.quit();
        }
    }

    // ---------------- SAFE CLICK ----------------
    public static void click(WebDriver driver, String xpath) {
        try {
            List<WebElement> el = driver.findElements(By.xpath(xpath));
            if (!el.isEmpty()) {
                el.get(0).click();
                sleep(800);
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
