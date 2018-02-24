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
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CPBot {
    private static final String USER_ANGENT_STRING =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2869.0 Safari/537.36"; // Relatively new Chrome UA

    private WebDriver myDriver;
    private Mail sendMail;
    private List<String> myRecipients;

    /*
     * @param recipients A list of the recipients, formatted as an email address
     */
    private CPBot(List<String> recipients) {
        sendMail = new Mail(); // Use a personal library to send the email
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.home") + "/chromedriver"); // Set the chromedriver location
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", true);
        dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", USER_ANGENT_STRING); // Create the various dCaps
        myDriver = new ChromeDriver(dCaps); // Create the chromedriver
        myDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS); // Set timeout to 30
        myRecipients = recipients; // Set the private list as the parameter
    }

    private void check(){
        myDriver.get("http://uscyberpatriot.org/competition/current-competition/scores"); // Load the site
        List<WebElement> headers = myDriver.findElements(By.xpath("/html/body/form/div[5]/div[1]/div/div[2]/div/div[2]/div[3]/div[1]/div[1]/div/div/div/div[2]/div/div/div[1]/ul/li"));
        // xpath to the headers
        if (headers.size() > 4){ // We want the 5th header (Semi final round)
            System.out.println("WEBSITE HAS BEEN UPDATED");
            try {
                sendMail.send(myRecipients,"","WEBSITE HAS BEEN UPDATED!"); // Send email
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            for (WebElement header : headers){ // Find the header that contains semi
                WebElement textLoc = header.findElement(By.xpath("./div"));
                if (textLoc.getText().toUpperCase().contains("SEMI")){
                    System.out.println("SEMI SCORES DETECTED!");
                    try {
                        sendMail.send(myRecipients,"","SEMI SCORES DETECTED!"); // Send the email
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                    List<WebElement> files = header.findElements(By.xpath("./ul/li/div/div/a")); // Look for the link
                    for (WebElement file : files){
                        if (file.getText().toUpperCase().contains("OPEN")){
                            System.out.println(file.getAttribute("href"));
                            try {
                                sendMail.send(myRecipients,"",file.getAttribute("href")); // Send the link
                            } catch (MessagingException e) {
                                e.printStackTrace();
                            }
                            downloadFile(file.getAttribute("href")); // Download the file
                        }
                    }
                }
            }
        }
    }
    public static void main(String[] args){
        List<String> toSend = Arrays.asList(args);
        System.out.println(toSend.get(0));
        final CPBot bt = new CPBot(toSend);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("checking...");
                bt.check();
            }
        }, 0, 180000);
        //bt.check();

    }

    private void downloadFile(String url){
        try {
            FileUtils.copyURLToFile(new URL(url),new File(System.getProperty("user.home") + "/semi.xlsx")); // Download
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkOurPosition(); // Check if we made it
    }

    private void checkOurPosition(){
        System.out.println("Checking position: "); // Start checking
        InputStream input = null;
        try {
            input = new FileInputStream(System.getProperty("user.home") + "/semi.xlsx"); // Use the downloaded file
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Workbook wb = null; // Get the workbook
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
            if (row.getCell(0).getStringCellValue().toUpperCase().contains("TEAM")){ // Look for the header row
                found = true;
            } else {
                rowNum++;
            }
        }
        Row correctRow = sheet.getRow(rowNum);
        int colNum = 0;
        found = false;
        while (!found){
            if (correctRow.getCell(colNum).getStringCellValue().toUpperCase().contains("ADVANCE")){ // Look for the column
                // that checks advancement position
                found = true;
            } else {
                colNum++;
            }
        }
        rowNum = 250;
        found = false;
        while (!found){
            Row row = sheet.getRow(rowNum);
            if (row.getCell(0).getStringCellValue().contains("0247")){ // Look for our team
                found = true;
                String advancement = row.getCell(colNum).getStringCellValue();
                System.out.println(advancement);
              /*try {
                  sendMail.send(myRecipients,"",advancement); // Send email if we made it
              } catch (MessagingException e) {
                  e.printStackTrace();
              }*/
            } else {
                rowNum++;
            }
        }
        myDriver.quit();
        System.exit(0);
    }
}
