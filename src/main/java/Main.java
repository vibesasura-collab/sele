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
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main {

    private static final int MAX_RUN_MINUTES = 345; // stop a bit before workflow timeout
    private static final LocalTime DAILY_STOP_START = LocalTime.of(23, 30); // GMT
    private static final LocalTime DAILY_STOP_END = LocalTime.of(1, 0);      // GMT

    private static final boolean TODAY_OFF = false; // true = bot OFF today, false = bot ON

    public static void main(String[] args) {

        if (TODAY_OFF) {
            System.out.println("Bot OFF today. Exiting.");
            return;
        }

        String user = System.getenv("GAME_ID");
        String pass = System.getenv("GAME_PASSWORD");

        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            throw new RuntimeException("GAME_ID or GAME_PASSWORD not found in GitHub Secrets.");
        }

        if (isInShutdownWindow()) {
            System.out.println("Inside daily shutdown window (23:30-01:00 GMT). Exiting.");
            return;
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
        Instant startTime = Instant.now();

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
                if (shouldStopNow(startTime)) {
                    System.out.println("Stopping now due to runtime limit or daily shutdown window.");
                    break;
                }
boolean actionPerformed = false;
              System.out.println("Clicking attack0, attack1, attack2...");

List<WebElement> attacks = new ArrayList<>();
attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack0']")));
attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack1']")));
attacks.addAll(driver.findElements(By.cssSelector("a[href*='attack2']")));

if (!attacks.isEmpty()) {

    for (WebElement attack : attacks) {
        try {
            attack.click();
        } catch (Exception ignored) {
        }
    }

    // wait 10 seconds
    sleep(10000);

    // refresh page
    driver.navigate().refresh();

    sleep(2500);
}

                List<WebElement> attackBtn = driver.findElements(By.xpath("//span[text()='Attack']"));
                if (!attackBtn.isEmpty()) {
                    try {
                        attackBtn.get(0).click();
                        actionPerformed = true;
                        sleep(1500);
                    } catch (Exception ignored) {
                    }
                }

                List<WebElement> goldAttack = driver.findElements(By.xpath("//span[contains(text(),'Attack now for')]"));
                if (!goldAttack.isEmpty()) {
                    try {
                        String text = goldAttack.get(0).getText();
                        String number = text.replaceAll("[^0-9]", "");

                        if (!number.isEmpty()) {
                            int cost = Integer.parseInt(number);

                            if (cost <= 20) {
                                goldAttack.get(0).click();
                                sleep(1200);

                                List<WebElement> yes = driver.findElements(By.xpath("//span[text()='Yes!']"));
                                if (!yes.isEmpty()) {
                                    yes.get(0).click();
                                }

                                actionPerformed = true;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

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
                    System.out.println("Stopping now due to runtime limit or daily shutdown window.");
                    break;
                }

                sleep(10000);
driver.navigate().refresh();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public static boolean shouldStopNow(Instant startTime) {
        long elapsedMinutes = Duration.between(startTime, Instant.now()).toMinutes();
        return elapsedMinutes >= MAX_RUN_MINUTES || isInShutdownWindow();
    }

    public static boolean isInShutdownWindow() {
        LocalTime now = LocalTime.now(ZoneOffset.UTC);
        return !now.isBefore(DAILY_STOP_START) || now.isBefore(DAILY_STOP_END);
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
