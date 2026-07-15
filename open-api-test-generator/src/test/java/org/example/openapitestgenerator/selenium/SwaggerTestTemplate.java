package org.example.openapitestgenerator.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SwaggerTestTemplate
{
    private final WebDriver driver;
    private final WebDriverWait wait;

    public SwaggerTestTemplate(WebDriver driver)
    {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void openUrl(String url)
    {
        driver.get(url);
    }

    public void authorize(String authType, String cred1, String cred2)
    {
        if (authType == null || authType.equalsIgnoreCase("none"))
        {
            return;
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".authorize"))).click();
        By modalLocator = By.cssSelector(".auth-container");
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalLocator));

        try
        {
            WebElement authBlock = null;

            if (authType.equalsIgnoreCase("basic"))
            {
                WebElement header = driver.findElement(By.xpath("//div[contains(@class, 'auth-container')]//h4[contains(translate(., 'BASIC', 'basic'), 'basic')]"));
                authBlock = header.findElement(By.xpath("./ancestor::div[contains(@class, 'wrapper') or contains(@class, 'block') or contains(@class, 'auth-container')][1]"));

                List<WebElement> inputs = authBlock.findElements(By.cssSelector("input"));
                if (inputs.size() >= 2)
                {
                    inputs.get(0).clear();
                    inputs.get(0).sendKeys(cred1);
                    inputs.get(1).clear();
                    inputs.get(1).sendKeys(cred2);
                }

            } else if (authType.equalsIgnoreCase("bearer"))
            {
                WebElement header = driver.findElement(By.xpath("//div[contains(@class, 'auth-container')]//h4[" + "not(contains(translate(., 'OAUTH', 'oauth'), 'oauth')) and " + "(contains(., 'api_key') or contains(., 'apiKey') or contains(translate(., 'BEARER', 'bearer'), 'bearer'))" + "]"));
                authBlock = header.findElement(By.xpath("./ancestor::div[contains(@class, 'wrapper') or contains(@class, 'block') or contains(@class, 'auth-container')][1]"));

                List<WebElement> inputs = authBlock.findElements(By.cssSelector("input"));
                if (!inputs.isEmpty())
                {
                    inputs.getFirst().clear();
                    inputs.getFirst().sendKeys(cred1);
                }
            }

            if (authBlock != null)
            {
                authBlock.findElement(By.cssSelector("button.authorize")).click();
            }

        } catch (Exception e)
        {
            System.err.println("Не удалось выполнить авторизацию: " + e.getMessage());
        } finally
        {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='auth-container']//button[contains(.,'Close')]"))).click();
        }
    }

    public void chooseEndpoint(String method, String path)
    {
        String xpath = String.format("//div[contains(@class, 'opblock')]//span[contains(text(), '%s')]/ancestor::div[contains(@class, 'opblock-summary')]//span[@data-path='%s']/ancestor::button", method.toUpperCase(), path);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
    }

    public void clickTryItOut()
    {
        By tryBtnLocator = By.cssSelector(".try-out__btn");
        WebElement tryBtn = wait.until(ExpectedConditions.elementToBeClickable(tryBtnLocator));
        tryBtn.click();
    }

    public void fillInput(String paramName, String value)
    {
        if (paramName.equalsIgnoreCase("body"))
        {

            List<WebElement> textareas = driver.findElements(By.cssSelector("textarea.body-param__text"));

            if (!textareas.isEmpty())
            {
                textareas.getFirst().clear();
                textareas.getFirst().sendKeys(value);
            }

            return;
        }

        List<WebElement> inputs = driver.findElements(By.cssSelector("input[placeholder='" + paramName + "']"));

        if (!inputs.isEmpty())
        {
            inputs.getFirst().clear();
            inputs.getFirst().sendKeys(value);
        }
    }

    public void clickExecute()
    {
        By executeBtnLocator = By.cssSelector(".execute");
        WebElement executeBtn = wait.until(ExpectedConditions.elementToBeClickable(executeBtnLocator));
        executeBtn.click();
    }

    public void verifyResponseCode(int expectedCode)
    {
        String xpath = "//table[contains(@class, 'live-responses-table')]//td[contains(@class, 'response-col_status') and contains(text(), '" + expectedCode + "')]";

        By liveResponseLocator = By.xpath(xpath);
        wait.until(ExpectedConditions.visibilityOfElementLocated(liveResponseLocator));
    }
}