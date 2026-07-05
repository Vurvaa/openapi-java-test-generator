package org.example.openapitestgenerator.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

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

    public void chooseEndpoint(String method, String path)
    {
        String xpath = String.format("//div[contains(@class, 'opblock')]//span[contains(text(), '%s')]/ancestor::div[contains(@class, 'opblock-summary')]//span[@data-path='%s']/ancestor::button",
                method.toUpperCase(), path);
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
        By inputLocator = By.cssSelector("input[placeholder='" + paramName + "']");
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(inputLocator));
        input.clear();
        input.sendKeys(value);
    }

    public void clickExecute()
    {
        By executeBtnLocator = By.cssSelector(".execute");
        WebElement executeBtn = wait.until(ExpectedConditions.elementToBeClickable(executeBtnLocator));
        executeBtn.click();
    }

    public void verifyResultText(String expectedText)
    {
        By responseLocator = By.xpath("//*[contains(normalize-space(text()), '" + expectedText + "')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(responseLocator));
    }
}