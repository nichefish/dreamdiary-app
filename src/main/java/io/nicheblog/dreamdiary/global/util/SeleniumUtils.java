package io.nicheblog.dreamdiary.global.util;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

/**
 * SeleniumUtils
 * <pre>
 *  크롬 테스트 관련 Selenium Utils
 * </pre>
 * TODO: 진행중
 *
 * @author nichefish
 */
@Log4j2
public class SeleniumUtils {

    /**
     * chromedriver 반환
     */
    public ChromeOptions getOptions() throws Exception {

        // chromeDriver 로드
        URL resourceUrl = getClass().getClassLoader().getResource("file/selenium/chromedriver.exe");
        if (resourceUrl == null) {
            log.info("resourceUrl is null.");
            return null;
        }
        String chromeDriverPath = resourceUrl.getPath();
        // 2번쨰 매개변수에 다운로드 받은 크롬드라이버 파일 경로를 적어준다. 상대경로, 절대경로 둘다 상관없다.
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--remote-allow-origins=*");

        return chromeOptions;
    }

    /**
     * chromedriver 테스트
     */
    public void init(final String... args) throws Exception {

        ChromeOptions chromeOptions = this.getOptions();

        WebDriver driver = new ChromeDriver(chromeOptions);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 실제 브라우저 구동 로직
        try {
            driver.get("https://google.com/ncr");
            driver.findElement(By.name("q"))
                  .sendKeys("cheese" + Keys.ENTER);
            //구글 검색 창에 cheese 입력하고 엔터키를 누른다.
            WebElement firstResult = wait.until(presenceOfElementLocated(By.cssSelector("h3>div")));
            System.out.println(firstResult.getAttribute("textContent"));
        } finally {
            // driver.quit();
        }
    }

}