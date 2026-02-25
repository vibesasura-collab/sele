import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        WebDriver driver = new ChromeDriver();
        Random random = new Random();

        // ======================
        // LOGIN
        // ======================
        driver.get("https://elem.cards/login/");
        driver.manage().window().maximize();

        Thread.sleep(2500);

        driver.findElement(By.name("plogin")).sendKeys("Avatasoo");
        driver.findElement(By.name("ppass")).sendKeys("1193811");
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        Thread.sleep(4000);

        driver.findElement(By.cssSelector("a.urfin")).click();
        Thread.sleep(3000);

        // ======================
        // MAIN LOOP
        // ======================
        while (true) {

            boolean actionPerformed = false;

            System.out.println("🔎 Searching attacks...");

            // ======================
            // ATTACK 0 1 2 (Random Order)
            // ======================
            List<WebElement> attacks = new ArrayList<>();

            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack0']")));
            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack1']")));
            attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack2']")));

            if (!attacks.isEmpty()) {

                Collections.shuffle(attacks);

                for (WebElement attack : attacks) {
                    try {
                        attack.click();
                        System.out.println("⚔ Attack clicked");

                        actionPerformed = true;

                        // Human delay (1–1.3 sec)
                        Thread.sleep(1000 + random.nextInt(300));

                    } catch (Exception ignored) {}
                }
            }

            // ======================
            // NORMAL ATTACK BUTTON
            // ======================
            List<WebElement> attackBtn =
                    driver.findElements(By.xpath("//span[text()='Attack']"));

            if (!attackBtn.isEmpty()) {

                attackBtn.get(0).click();
                System.out.println("⚔ Normal Attack");

                actionPerformed = true;
                Thread.sleep(1500);
            }

            // ======================
            // GOLD ATTACK (<=20 only)
            // ======================
            List<WebElement> goldAttack =
                    driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));

            if (!goldAttack.isEmpty()) {

                String text = goldAttack.get(0).getText();
                String number = text.replaceAll("[^0-9]", "");

                if (!number.isEmpty()) {

                    int cost = Integer.parseInt(number);

                    if (cost <= 50) {

                        goldAttack.get(0).click();
                        System.out.println("💰 Gold attack used (" + cost + ")");

                        Thread.sleep(1200);

                        List<WebElement> yes =
                                driver.findElements(By.xpath("//span[text()='Yes!']"));

                        if (!yes.isEmpty()) {
                            yes.get(0).click();
                            System.out.println("✅ Gold confirmed");
                        }

                        actionPerformed = true;
                        Thread.sleep(1500);
                    }
                    else {
                        System.out.println("❌ Gold too expensive: " + cost);
                    }
                }
            }

            // ======================
            // NEXT BUTTON
            // ======================
            List<WebElement> nextBtn =
                    driver.findElements(By.xpath("//span[text()='Next']"));

            if (!nextBtn.isEmpty()) {

                nextBtn.get(0).click();
                System.out.println("➡ Next clicked");

                actionPerformed = true;
                Thread.sleep(1500);
            }

            // ======================
            // REFRESH LOGIC
            // ======================
            if (actionPerformed) {

                System.out.println("🔄 Browser Refresh");

                driver.navigate().refresh();   // ✅ REAL browser refresh
                Thread.sleep(2500);
            }
            else {

                System.out.println("😴 Nothing available → wait 1 minute");

                Thread.sleep(60000);

                driver.navigate().refresh();
                System.out.println("🔄 1‑minute refresh");
                Thread.sleep(3000);
            }
        }
    }
}