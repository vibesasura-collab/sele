import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        // ======================
        // HEADLESS BROWSER
        // ======================
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(20));

        Random random = new Random();

        // ======================
        // LOGIN
        // ======================
        driver.get("https://elem.cards/login/");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("plogin")))
                .sendKeys("Avatasoo");

        driver.findElement(By.name("ppass")).sendKeys("1193811");

        driver.findElement(By.cssSelector("input[type='submit']")).click();

        // wait until game button appears
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.urfin")))
                .click();

        System.out.println("✅ Login success");

        // ======================
        // MAIN LOOP
        // ======================
        while (true) {

            boolean actionPerformed = false;

            System.out.println("🔎 Searching attacks...");

            List<WebElement> attacks = new ArrayList<>();

            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack0']")));
            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack1']")));
            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack2']")));

            Collections.shuffle(attacks);

            for (WebElement attack : attacks) {
                try {
                    attack.click();
                    actionPerformed = true;
                    Thread.sleep(1000 + random.nextInt(300));
                } catch (Exception ignored) {}
            }

            // NORMAL ATTACK
            List<WebElement> attackBtn =
                    driver.findElements(By.xpath("//span[text()='Attack']"));

            if (!attackBtn.isEmpty()) {
                attackBtn.get(0).click();
                actionPerformed = true;
                Thread.sleep(1500);
            }

            // GOLD ATTACK
            List<WebElement> goldAttack =
                    driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));

            if (!goldAttack.isEmpty()) {

                String text = goldAttack.get(0).getText();
                String number = text.replaceAll("[^0-9]", "");

                if (!number.isEmpty()) {

                    int cost = Integer.parseInt(number);

                    if (cost <= 50) {

                        goldAttack.get(0).click();

                        Thread.sleep(1200);

                        List<WebElement> yes =
                                driver.findElements(By.xpath("//span[text()='Yes!']"));

                        if (!yes.isEmpty()) {
                            yes.get(0).click();
                        }

                        actionPerformed = true;
                        Thread.sleep(1500);
                    }
                }
            }

            // NEXT BUTTON
            List<WebElement> nextBtn =
                    driver.findElements(By.xpath("//span[text()='Next']"));

            if (!nextBtn.isEmpty()) {
                nextBtn.get(0).click();
                actionPerformed = true;
                Thread.sleep(1500);
            }

            // REFRESH
            if (actionPerformed) {
                driver.navigate().refresh();
                Thread.sleep(2500);
            } else {
                Thread.sleep(60000);
                driver.navigate().refresh();
                Thread.sleep(3000);
            }
        }
    }
}
