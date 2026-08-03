import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main2 {

    private static final int MAX_RUN_MINUTES = 345;
    private static final boolean TODAY_OFF = false;

    public static void main(String[] args) {

        if (TODAY_OFF) {
            return;
        }

        String user = System.getenv("GAME_ID_MOBI");
        if (user == null || user.isEmpty()) {
            user = System.getenv("GAME_ID"); // Fallback to standard GAME_ID
        }

        String pass = System.getenv("GAME_PASSWORD_MOBI");
        if (pass == null || pass.isEmpty()) {
            pass = System.getenv("GAME_PASSWORD"); // Fallback to standard GAME_PASSWORD
        }

        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            throw new RuntimeException("GAME_ID or GAME_PASSWORD secrets not found.");
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        Random random = new Random();
        Instant startTime = Instant.now();

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // Updated URL for elem.mobi
            driver.get("https://elem.mobi/login/");
            sleep(2000);

            // Crash-proof login block
            List<WebElement> userInputs = driver.findElements(By.name("plogin"));
            List<WebElement> passInputs = driver.findElements(By.name("ppass"));
            List<WebElement> submitBtns = driver.findElements(By.cssSelector("input[type='submit']"));

            if (!userInputs.isEmpty() && !passInputs.isEmpty() && !submitBtns.isEmpty()) {
                userInputs.get(0).sendKeys(user);
                passInputs.get(0).sendKeys(pass);
                submitBtns.get(0).click();
                sleep(4000);
            } else {
                return;
            }

            // Crash-proof urf.in click with retries
            boolean navigated = false;
            for (int attempt = 1; attempt <= 5; attempt++) {
                List<WebElement> urfinLinks = driver.findElements(By.cssSelector("a.urfin"));
                if (!urfinLinks.isEmpty()) {
                    urfinLinks.get(0).click();
                    navigated = true;
                    sleep(3000);
                    break;
                }
                sleep(2000);
            }

            if (!navigated) {
                return;
            }

            int consecutiveIdle = 0;

            while (true) {
                long loopStart = System.currentTimeMillis();

                if (shouldStopNow(startTime)) {
                    break;
                }

                boolean actionPerformed = false;

                // Collect attack links
                List<String> attackLinks = new ArrayList<>();
                
                // Filter out hidden cards using :not(.chide2)
                List<WebElement> attack0 = driver.findElements(By.cssSelector("a[href*='attack0'].card:not(.chide2)"));
                List<WebElement> attack1 = driver.findElements(By.cssSelector("a[href*='attack1'].card:not(.chide2)"));
                List<WebElement> attack2 = driver.findElements(By.cssSelector("a[href*='attack2'].card:not(.chide2)"));

                for (WebElement e : attack0) attackLinks.add(e.getAttribute("href"));
                for (WebElement e : attack1) attackLinks.add(e.getAttribute("href"));
                for (WebElement e : attack2) attackLinks.add(e.getAttribute("href"));

                if (!attackLinks.isEmpty()) {
                    actionPerformed = true;
                }

                // Visit each attack link
                for (String link : attackLinks) {
                    try {
                        driver.get(link);
                        sleep(800);
                    } catch (Exception ignored) {
                    }
                }

                // Attack button
                List<WebElement> attackBtn = driver.findElements(By.xpath("//span[text()='Attack']"));
                if (!attackBtn.isEmpty()) {
                    try {
                        attackBtn.get(0).click();
                        actionPerformed = true;
                        sleep(1500);
                    } catch (Exception ignored) {
                    }
                }

                // -------- Gold attack logic (limit 20) --------
                // Updated to look for the Russian text string
                List<WebElement> goldAttack = driver.findElements(By.xpath("//span[contains(text(),'Напасть сразу за')]"));
                if (!goldAttack.isEmpty()) {
                    try {
                        String text = goldAttack.get(0).getText();
                        String number = text.replaceAll("[^0-9]", "");

                        if (!number.isEmpty()) {
                            int cost = Integer.parseInt(number);

                            if (cost <= 10) {
                                // Clicking the parent <a> tag to avoid element interception issues
                                WebElement parentLink = goldAttack.get(0).findElement(By.xpath("./ancestor::a"));
                                parentLink.click();
                                sleep(1200);

                                // Adding coverage for Russian or English confirmation prompt
                                List<WebElement> yes = driver.findElements(By.xpath("//span[contains(text(),'Да')] | //span[text()='Yes!']"));
                                if (!yes.isEmpty()) {
                                    yes.get(0).click();
                                }

                                actionPerformed = true;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                // Next button
                List<WebElement> nextBtn = driver.findElements(By.xpath("//span[text()='Next']"));
                if (!nextBtn.isEmpty()) {
                    try {
                        nextBtn.get(0).click();
                        actionPerformed = true;
                        sleep(1500);
                    } catch (Exception ignored) {
                    }
                }

                if (shouldStopNow(startTime)) {
                    break;
                }

                // Dynamic Sleep Strategy
                if (actionPerformed) {
                    consecutiveIdle = 0;
                    long elapsed = System.currentTimeMillis() - loopStart;
                    long remaining = 10000 - elapsed;

                    if (remaining > 0) {
                        sleep((int) remaining);
                    }
                } else {
                    consecutiveIdle++;
                    int sleepTimeMs;

                    if (consecutiveIdle >= 2) {
                        int minMs = 15 * 60 * 1000;
                        int maxMs = 16 * 60 * 1000;
                        sleepTimeMs = random.nextInt(maxMs - minMs + 1) + minMs;
                    } else {
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
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public static boolean shouldStopNow(Instant startTime) {
        long elapsedMinutes = Duration.between(startTime, Instant.now()).toMinutes();
        return elapsedMinutes >= MAX_RUN_MINUTES;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
