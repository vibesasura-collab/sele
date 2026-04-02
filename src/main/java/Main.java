import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
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
                            sleep(1000 + random.nextInt(300));
                        } catch (Exception ignored) {
                        }
                    }
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

                            if (cost <= 0) {
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

                if (actionPerformed) {
                    driver.navigate().refresh();
                    sleep(2500);
                } else {
                    sleep(60000);
                    driver.navigate().refresh();
                }
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
