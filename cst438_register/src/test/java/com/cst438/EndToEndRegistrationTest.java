package com.cst438;

import com.cst438.domain.Student;
import com.cst438.domain.StudentRepository;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
public class EndToEndRegistrationTest {

    public static final String DRIVER_FILE_LOCATION = "C:/Users/J/Desktop/geckodriver-v0.31.0-win64/geckodriver.exe";

    public static final String URL = "http://localhost:3000/students";

    public static final int SLEEP_DURATION = 1000; // 1 second.

    public static final String TEST_USER_NAME = "J Burk";

    public static final String TEST_USER_EMAIL = "jburk@csumb.edu";
    
    private static Boolean email_present = false;
    
    private static Boolean name_present = false;

    @Autowired
    StudentRepository studentRepository;

    @Test
    public void addStudentTest() throws Exception {
        Student s = null;
        do {
            s = studentRepository.findByEmail(TEST_USER_EMAIL);
            if (s != null) {
                studentRepository.delete(s);
            }
        } while (s != null);

        // set the driver location and start driver
        //@formatter:off
        // browser	property name 				Java Driver Class
        // edge 	webdriver.edge.driver 		EdgeDriver
        // FireFox 	webdriver.firefox.driver 	FirefoxDriver
        // IE 		webdriver.ie.driver 		InternetExplorerDriver
        //@formatter:on

        System.setProperty("webdriver.gecko.driver", DRIVER_FILE_LOCATION);
        WebDriver driver = new FirefoxDriver();      


        try {
            driver.get(URL);
            Thread.sleep(SLEEP_DURATION);

            // find 'add student' button and click it
            driver.findElement(By.xpath("//button[@id='AddS']")).click();
            Thread.sleep(SLEEP_DURATION);

            // fill in 'name' field
            driver.findElement(By.xpath("//input[@name='name']")).sendKeys(TEST_USER_NAME);
            Thread.sleep(SLEEP_DURATION);

            // fill in 'email' field
            driver.findElement(By.xpath("//input[@name='email']")).sendKeys(TEST_USER_EMAIL);
            Thread.sleep(SLEEP_DURATION);

            // submit 'add student' form
            driver.findElement(By.xpath("//button[@id='Add']")).click();
            Thread.sleep(SLEEP_DURATION);

            // verify new student is in the database
            Student student = studentRepository.findByEmail(TEST_USER_EMAIL);
            assertNotNull(student, "Added student not in database");
            
            // verify new student shows on page
            java.util.List<WebElement> we_list = driver.findElements(By.ByClassName.className("MuiDataGrid-cellContent"));
            // search for email and name on page
            for (int i=0; i < we_list.size(); i++) {
            	if (student.getEmail().equals(we_list.get(i).getText())) {
            		email_present = true;
            	}
            	if (student.getName().equals(we_list.get(i).getText())) {
            		name_present = true;
            	}
            }
            assertEquals(true, email_present);
            assertEquals(true, name_present);
            //assertEquals(student.getName(), we_list.get(11).getText());
            //assertEquals(student.getEmail(), we_list.get(12).getText());

        } catch (Exception ex) {
            throw ex;
        } finally {
            // clean up database
            s = studentRepository.findByEmail(TEST_USER_EMAIL);
            if (s != null) {
                studentRepository.delete(s);
            }
        }
    }
}