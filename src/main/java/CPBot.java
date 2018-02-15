import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.mail.MessagingException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CPBot {
  private static final String USER_ANGENT_STRING =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2869.0 Safari/537.36";

  private WebDriver driver;
  private Mail sendMail;
  private List<String> myRecipients;

  private CPBot(List<String> recipients) {
      sendMail = new Mail();
      System.setProperty("webdriver.chrome.driver", System.getProperty("user.home") + "/chromedriver.exe");
      DesiredCapabilities dCaps = new DesiredCapabilities();
      dCaps.setJavascriptEnabled(true);
      dCaps.setCapability("takesScreenshot", true);
      dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", USER_ANGENT_STRING);
      //driver = new PhantomJSDriver(dCaps);
      driver = new ChromeDriver(dCaps);
      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
      myRecipients = recipients;
  }

  private void startChecking(){
      driver.get("http://uscyberpatriot.org/competition/current-competition/scores");
      List<WebElement> headers = driver.findElements(By.xpath("/html/body/form/div[5]/div[1]/div/div[2]/div/div[2]/div[3]/div[1]/div[1]/div/div/div/div[2]/div/div/div[1]/ul/li"));
      if (headers.size() >= 4){
          try {
              sendMail.send(myRecipients,"","WEBSITE HAS BEEN UPDATED!");
          } catch (MessagingException e) {
              e.printStackTrace();
          }
          for (WebElement header : headers){
              WebElement textLoc = header.findElement(By.xpath("./div"));
              if (textLoc.getText().toUpperCase().contains("STATE")){
                  try {
                      sendMail.send(myRecipients,"","SEMI SCORES DETECTED!");
                  } catch (MessagingException e) {
                      e.printStackTrace();
                  }
                  List<WebElement> files = header.findElements(By.xpath("./ul/li/div/div/a"));
                  for (WebElement file : files){
                      System.out.println(file.getText());
                      if (file.getText().toUpperCase().contains("OPEN")){
                          try {
                              sendMail.send(myRecipients,"",file.getAttribute("href"));
                          } catch (MessagingException e) {
                              e.printStackTrace();
                          }
                          downloadFile(file.getAttribute("href"));
                      }
                  }
              }
              System.out.println(textLoc.getText());
          }
      }
  }
  public static void main(String[] args){
      List<String> toSend = Arrays.asList(args);
      CPBot bt = new CPBot(toSend);
      bt.startChecking();
  }

  private void downloadFile(String url){
      try {
          FileUtils.copyURLToFile(new URL(url),new File(System.getProperty("user.home") + "/semi.xlsx"));
      } catch (IOException e) {
          e.printStackTrace();
      }
      checkOurPosition();
  }

  private void checkOurPosition(){
      InputStream input = null;
      try {
          input = new FileInputStream(System.getProperty("user.home") + "/semi.xlsx");
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      }
      Workbook wb = null;
      try {
          assert input != null;
          wb = WorkbookFactory.create(input);
      } catch (IOException e) {
          e.printStackTrace();
      } catch (InvalidFormatException e) {
          e.printStackTrace();
      }
      assert wb != null;
      Sheet sheet = wb.getSheetAt(0);
      int rowNum = 0;
      boolean found = false;
      while (!found){
          Row row = sheet.getRow(rowNum);
          if (row.getCell(0).getStringCellValue().toUpperCase().contains("TEAM")){
              found = true;
          }
      }
      Row correctRow = sheet.getRow(rowNum);
      int colNum = 0;
      found = false;
      while (!found){
          if (correctRow.getCell(colNum).getStringCellValue().toUpperCase().contains("ADVANCE")){
              found = true;
          }
      }
      rowNum = 250;
      found = false;
      while (!found){
          Row row = sheet.getRow(rowNum);
          if (row.getCell(0).getStringCellValue().contains("0247")){
              String advancement = row.getCell(colNum).getStringCellValue();
              try {
                  sendMail.send(myRecipients,"",advancement);
              } catch (MessagingException e) {
                  e.printStackTrace();
              }
          }
      }
  }
}
