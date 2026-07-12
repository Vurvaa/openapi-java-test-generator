package org.example.openapitestgenerator.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

public class SwaggerTestTemplate
{
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ObjectMapper mapper = new ObjectMapper();

    public SwaggerTestTemplate(WebDriver driver)
    {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void openUrl(String url)
    {
        driver.get(url);
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
            } else
            {
                try
                {
                    JsonNode json = mapper.readTree(value);

                    json.properties().forEach(entry ->
                    {
                        WebElement input = driver.findElement(By.cssSelector("input[placeholder='" + entry.getKey() + "']"));
                        input.sendKeys(org.openqa.selenium.Keys.CONTROL + "a", org.openqa.selenium.Keys.BACK_SPACE);
                        input.sendKeys(entry.getValue().toString());
                    });
                } catch (Exception ignored) {}
            }
        } else
        {
            WebElement input = driver.findElement(By.cssSelector("input[placeholder='" + paramName + "']"));
            input.clear();
            input.sendKeys(value);
        }
    }

    public void clickExecute()
    {
        By executeBtnLocator = By.cssSelector(".execute");
        WebElement executeBtn = wait.until(ExpectedConditions.elementToBeClickable(executeBtnLocator));
        executeBtn.click();
    }

    public void verifyResponseCode(int expectedText)
    {
        By responseLocator = By.xpath("//*[contains(normalize-space(text()), '" + expectedText + "')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(responseLocator));
    }
}