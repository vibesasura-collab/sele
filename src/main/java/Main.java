import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        // ✅ setup chrome driver automatically
        WebDriverManager.chromedriver().setup();

        // ✅ ONLY ONE ChromeOptions object
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        Random random = new Random();

        // ================= LOGIN =================
        driver.get("https://elem.cards/login/");

        Thread.sleep(2000);

        driver.findElement(By.name("plogin")).sendKeys("Avatasoo");
        driver.findElement(By.name("ppass")).sendKeys("1193811@1144");
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        Thread.sleep(4000);

        driver.findElement(By.cssSelector("a.urfin")).click();
        Thread.sleep(3000);

        // ================= MAIN LOOP =================
        while (true) {

            boolean actionPerformed = false;

            System.out.println("Searching attacks...");

            List<WebElement> attacks = new ArrayList<>();

            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack0']")));
            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack1']")));
            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack2']")));

            if (!attacks.isEmpty()) {

                Collections.shuffle(attacks);

                for (WebElement attack : attacks) {
                    try {
                        attack.click();
                        actionPerformed = true;
                        Thread.sleep(1000 + random.nextInt(300));
                    } catch (Exception ignored) {}
                }
            }

            // NORMAL ATTACK
            List<WebElement> attackBtn =
                    driver.findElements(By.xpath("//span[text()='Attack']"));

            if (!attackBtn.isEmpty()) {
                attackBtn.get(0).click();
                actionPerformed = true;
                Thread.sleep(1500);
            }

            // GOLD ATTACK <=20
            List<WebElement> goldAttack =
                    driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));

            if (!goldAttack.isEmpty()) {

                String text = goldAttack.get(0).getText();
                String number = text.replaceAll("[^0-9]", "");

                if (!number.isEmpty()) {

                    int cost = Integer.parseInt(number);

                    if (cost <= 20) {

                        goldAttack.get(0).click();
                        Thread.sleep(1200);

                        List<WebElement> yes =
                                driver.findElements(By.xpath("//span[text()='Yes!']"));

                        if (!yes.isEmpty()) {
                            yes.get(0).click();
                        }

                        actionPerformed = true;
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

            // REFRESH LOGIC
            if (actionPerformed) {
                driver.navigate().refresh();
                Thread.sleep(2500);
            } else {
                Thread.sleep(60000);
                driver.navigate().refresh();
            }
        }
    }
}
