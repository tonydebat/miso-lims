package uk.ac.bbsrc.tgac.miso.ui.test;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UiTests {

  private static final String BASE_URL = "http://dcooke.res.oicr.on.ca:8080/";
  
  private static final String PROJECT_SHORTNAME = "TEST1";
  private static final String PROJECT_ALIAS = "Test Project 1";
  
  private WebDriver driver;
  
  
  @Before
  public void setup() {
    System.setProperty("webdriver.gecko.driver", "/home/dcooke/dev/geckodriver");
    driver = new FirefoxDriver();
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }
  
  private void assertPageChange(String urlRelativeToBase) {
    String url = BASE_URL + urlRelativeToBase;
    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains(url));
    assertTrue(driver.getCurrentUrl().startsWith(url));
  }
  
  @Test
  public void test() {
    // login page
    driver.get(BASE_URL);
    assertPageChange("login.jsp");
    
    // log in
    WebElement usernameField = driver.findElement(By.id("j_username"));
    usernameField.sendKeys("admin");
    WebElement passwordField = driver.findElement(By.id("j_password"));
    passwordField.sendKeys("admin");
    passwordField.submit();
    assertPageChange("miso/mainMenu");
    
    // main menu
    WebElement projectsLink = driver.findElement(By.linkText("My Projects"));
    projectsLink.click();
    assertPageChange("miso/projects");
    
    // list projects
    // TODO: figure out how to select Add Project button (or add an id to it)
    WebElement addProjectButton = driver.findElement(By.className("fg-button"));
    addProjectButton.click();
    assertPageChange("miso/project/new");
    
    // create project
    driver.findElement(By.id("alias")).sendKeys(PROJECT_ALIAS);
    driver.findElement(By.id("shortName")).sendKeys(PROJECT_SHORTNAME);
    driver.findElement(By.id("description")).sendKeys("test things for testing tests");
    driver.findElement(By.id("progress2")).click();
    Select referenceGenome = new Select(driver.findElement(By.id("referenceGenome")));
    referenceGenome.selectByVisibleText("Human hg19");
    driver.findElement(By.className("fg-button")).click();
  }
  
}
