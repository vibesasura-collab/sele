import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            throw new RuntimeException("GAME_ID or GAME_PASSWORD not found in GitHub Secrets.");
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        Random random = new Random();
        
        // Tracks consecutive loops where no action was performed
        int consecutiveIdle = 0; 

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

            while (true) {

                long loopStart = System.currentTimeMillis();
                boolean actionPerformed = false;

                // -------- Collect attack links first --------
                List<String> attackLinks = new ArrayList<>();

                List<WebElement> attack0 = driver.findElements(By.cssSelector("a[href*='attack0']"));
                List<WebElement> attack1 = driver.findElements(By.cssSelector("a[href*='attack1']"));
                List<WebElement> attack2 = driver.findElements(By.cssSelector("a[href*='attack2']"));

                for (WebElement e : attack0) attackLinks.add(e.getAttribute("href"));
                for (WebElement e : attack1) attackLinks.add(e.getAttribute("href"));
                for (WebElement e : attack2) attackLinks.add(e.getAttribute("href"));
                
                // If we found links, we consider the bot active
                if (!attackLinks.isEmpty()) {
                    actionPerformed = true;
                }

                // -------- Visit each attack --------
                for (String link : attackLinks) {
                    try {
                        driver.get(link);
                        sleep(800);
                    } catch (Exception ignored) {}
                }

                // -------- Attack button --------
                List<WebElement> attackBtn = driver.findElements(By.xpath("//span[text()='Attack']"));
                if (!attackBtn.isEmpty()) {
                    try {
                        attackBtn.get(0).click();
                        actionPerformed = true;
                        sleep(1500);
                    } catch (Exception ignored) {}
                }

                // -------- Gold attack --------
                List<WebElement> goldAttack = driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));
                if (!goldAttack.isEmpty()) {
                    try {
                        String text = goldAttack.get(0).getText();
                        String number = text.replaceAll("[^0-9]", "");

                        if (!number.isEmpty()) {
                            int cost = Integer.parseInt(number);
                            if (cost <= 10) {
                                goldAttack.get(0).click();
                                sleep(1200);
                                List<WebElement> yes = driver.findElements(By.xpath("//span[text()='Yes!']"));
                                if (!yes.isEmpty()) yes.get(0).click();
                                actionPerformed = true;
                            }
                        }
                    } catch (Exception ignored) {}
                }

                // -------- Next button --------
                List<WebElement> nextBtn = driver.findElements(By.xpath("//span[text()='Next']"));
                if (!nextBtn.isEmpty()) {
                    try {
                        nextBtn.get(0).click();
                        actionPerformed = true;
                        sleep(1500);
                    } catch (Exception ignored) {}
                }

                // -------- Dynamic Waiting Logic --------
                if (actionPerformed) {
                    // Reset idle counter since we successfully attacked
                    consecutiveIdle = 0; 
                    
                    // Standard 10-second loop
                    long elapsed = System.currentTimeMillis() - loopStart;
                    long remaining = 10000 - elapsed;
                    if (remaining > 0) {
                        sleep((int) remaining);
                    }
                } else {
                    // We did nothing this loop
                    consecutiveIdle++;
                    int sleepTimeMs;
                    
                    if (consecutiveIdle >= 2) {
                        // "No more bot" state (prolonged empty targets) -> 15-16 mins
                        System.out.println("Sustained idle. Sleeping 15-16 minutes...");
                        int minMs = 15 * 60 * 1000;
                        int maxMs = 16 * 60 * 1000;
                        sleepTimeMs = random.nextInt(maxMs - minMs + 1) + minMs;
                    } else {
                        // First time seeing no attacks / cost too high -> 5-6 mins
                        System.out.println("No targets or cost > 10. Sleeping 5-6 minutes...");
                        int minMs = 5 * 60 * 1000;
                        int maxMs = 6 * 60 * 1000;
                        sleepTimeMs = random.nextInt(maxMs - minMs + 1) + minMs;
                    }
                    
                    sleep(sleepTimeMs);
                }

                driver.navigate().refresh();
            }

        } catch (Exception e) {
            e.printStackTrace();
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
