package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.FileWriter;
import java.io.IOException;


public class CarValuationStepDefinitions {
    private static final Logger logger = Logger.getLogger(CarValuationStepDefinitions.class.getName());
    private static final String BASE_PATH = "src/test/resources/testdata/";
    private String inputFileName;
    private String outputFileName;
    private final List<String> extractedRegNumbers = new ArrayList<>();
    private final Map<String, List<String>> fileContents = new HashMap<>();
    private final List<String[]> results = new ArrayList<>();
    private static final String CSV_FILE_PATH = "src/test/resources/actual_output.csv";
    String expectedFileBasePath = "src/test/resources/testdata/";
    String expectedFilePath = null;
    String actualFilePath = "src/test/resources/actual_output.csv";
    String reportDir = "target/cucumber-reports";
    String htmlReportPath = reportDir + "/comparison_report.html";

    private WebDriver driver;

    @Given("the following input files for processing:")
    public void the_following_input_files_for_processing(io.cucumber.datatable.DataTable files) {
        List<Map<String, String>> fileMappings = files.asMaps(String.class, String.class);

        for (Map<String, String> fileMap : fileMappings) {
            inputFileName = fileMap.get("input_file").trim();
            outputFileName = fileMap.get("output_file").trim();
            expectedFilePath = "src/test/resources/testdata/" + fileMap.get("output_file").trim();

            if (inputFileName != null && !inputFileName.isEmpty()) {
                loadFile(inputFileName);
            }
            if (outputFileName != null && !outputFileName.isEmpty()) {
                loadFile(outputFileName);
            }
        }
        logger.info("Loaded files: " + inputFileName + " and " + outputFileName);
    }


    private void loadFile(String fileName) {
        String filePath = BASE_PATH + fileName;
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
            fileContents.put(fileName, lines);
            logger.info("Successfully loaded file: " + fileName);
        } catch (Exception e) {
            logger.severe("Error: File not found -> " + filePath);
            throw new RuntimeException("File not found: " + filePath, e);
        }
    }


    @When("I extract registration numbers from {string} using predefined patterns")
    public void extractRegistrationNumbers(String fileKey) {
        if (!fileContents.containsKey(fileKey)) {
            throw new RuntimeException("File " + fileKey + " not found in memory.");
        }

        Pattern pattern = Pattern.compile("\\b[A-Z]{2}[0-9]{2} [A-Z]{3}\\b|\\b[A-Z]{2}[0-9]{2}[A-Z]{3}\\b");
        List<String> fileLines = fileContents.get(fileKey);
        extractedRegNumbers.clear();

        for (String line : fileLines) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                extractedRegNumbers.add(matcher.group());
            }
        }

        if (extractedRegNumbers.isEmpty()) {
            logger.warning("No registration numbers found in " + fileKey);
        } else {
            logger.info("Extracted registration numbers: " + extractedRegNumbers);
        }
    }

    @And("I perform a car valuation search for each registration on {string}")
public void performCarValuationSearch(String website) {
    if (extractedRegNumbers.isEmpty()) {
        logger.warning("No registration numbers found to perform valuation search.");
        return;
    }

    // Use WebDriverManager to setup ChromeDriver
    WebDriverManager.chromedriver().setup();

    // Set up headless Chrome
    ChromeOptions options = new ChromeOptions();
    // options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
    options.addArguments("--disable-gpu", "--window-size=1920,1080");
    
    driver = new ChromeDriver(options);

    try {
       

        for (String reg : extractedRegNumbers) {
            driver.get("https://www.caranalytics.co.uk/price");
            logger.info("Opened Car Analytics Website.");
            logger.info("Searching for registration: " + reg);

            // Enter registration number
            WebElement regInput = driver.findElement(By.id("vrm-input"));
            regInput.clear();
            regInput.sendKeys(reg);

            // Click 'Check vehicle' button
            WebElement checkButton = driver.findElement(By.cssSelector("button.service_page_button"));
            checkButton.click();

            // Wait for results
            Thread.sleep(3000);

            // Check if error message is displayed
            List<WebElement> errorMessages = driver.findElements(By.xpath("//div[contains(@class, 'alert-danger') and contains(text(), 'Vehicle registration number is not in the correct format')]"));
            if (!errorMessages.isEmpty()) {
                logger.warning(" Error: Registration number [" + reg + "] is not in the correct format.");
                System.out.println(" Invalid registration format: " + reg);
                // Store the "not_found" entry for this registration
                results.add(new String[]{reg, "not_found", "not_found", "not_found"});
                continue; // Skip this registration and move to the next
            }

            // Extract Make, Model, Year
            String make = driver.findElement(By.xpath("//td[contains(text(),'Make')]/following-sibling::td")).getText();
            String model = driver.findElement(By.xpath("//td[contains(text(),'Model')]/following-sibling::td")).getText();
            String year = driver.findElement(By.xpath("//td[contains(text(),'Year of Manufacture')]/following-sibling::td")).getText();

            System.out.println("\n Car valuation for [" + reg + "]:");
            System.out.println(" Make: " + make);
            System.out.println(" Model: " + model);
            System.out.println(" Year of Manufacture: " + year + "\n");
            results.add(new String[]{reg, make, model, year});

        }
    } catch (Exception e) {
        logger.severe("Error during car valuation search: " + e.getMessage());
    } finally {
        driver.quit();
        writeResultsToCSV();
        compareResults();
    }

}
private void compareResults() {

    try {
        // Ensure the report directory exists
        Files.createDirectories(Paths.get(reportDir));

        // Read both expected and actual files
        List<String> expectedLines = Files.readAllLines(Paths.get(expectedFilePath), StandardCharsets.UTF_8);
        List<String> actualLines = Files.readAllLines(Paths.get(actualFilePath), StandardCharsets.UTF_8);

        // Store actual records in a lookup map using registration as key
        Map<String, String> actualDataMap = new HashMap<>();
        for (String line : actualLines.subList(1, actualLines.size())) {  // Skipping header
            String[] parts = line.split(",");
            if (parts.length > 0) {
                String regNumber = parts[0].replaceAll("\\s+", "").toUpperCase(); // Normalize REG
                actualDataMap.put(regNumber, line);
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("<html><head><title>Comparison Report</title>");
        report.append("<style>body { font-family: Arial, sans-serif; }");
        report.append("table { border-collapse: collapse; width: 100%; }");
        report.append("th, td { border: 1px solid black; padding: 8px; text-align: left; }");
        report.append(".match { background-color: #c8e6c9; } .mismatch { background-color: #ffcdd2; }");
        report.append(".not_found { background-color: #ffeb3b; }</style>");
        report.append("</head><body>");
        report.append("<h2>Comparison Report: Expected vs. Actual Using https://www.caranalytics.co.uk/price </h2>");
        report.append("<table><tr><th>Registration</th><th>Expected</th><th>Actual</th><th>Status</th></tr>");

        // Compare each expected line
        for (int i = 1; i < expectedLines.size(); i++) { // Skipping header
            String expected = expectedLines.get(i);
            String[] expectedParts = expected.split(",");
            String regNumber = expectedParts[0].replaceAll("\\s+", "").toUpperCase(); // Normalize REG

            if (actualDataMap.containsKey(regNumber)) {
                String actual = actualDataMap.get(regNumber);
                boolean containsNotFound = actual.toLowerCase().contains("not_found");
                boolean isMatch = expected.equals(actual);
                String status = isMatch ? "✅ Match" : (containsNotFound ? "🚨 Not Found" : "❌ Mismatch");
                String rowClass = isMatch ? "match" : (containsNotFound ? "not_found" : "mismatch");

                // If row contains "not_found", do not highlight differences
                String displayedActual = containsNotFound ? actual : highlightDifferences(expected, actual);

                report.append("<tr class='").append(rowClass).append("'><td>")
                        .append(regNumber).append("</td><td>")
                        .append(expected).append("</td><td>")
                        .append(displayedActual).append("</td><td>")
                        .append(status).append("</td></tr>");
            } else {
                // If registration is missing in actual output
                String notFoundRow = regNumber + ",not_found,not_found,not_found";
                report.append("<tr class='not_found'><td>")
                        .append(regNumber).append("</td><td>")
                        .append(expected).append("</td><td>")
                        .append(notFoundRow).append("</td><td>🚨 Not Found</td></tr>");
            }
        }

        report.append("</table></body></html>");
        Files.write(Paths.get(htmlReportPath), report.toString().getBytes(StandardCharsets.UTF_8));
        logger.info(" HTML Report Generated: " + htmlReportPath);
    } catch (IOException e) {
        logger.severe(" Error writing HTML report: " + e.getMessage());
    }
}


private String highlightDifferences(String expected, String actual) {
    String[] expectedParts = expected.split(",");
    String[] actualParts = actual.split(",");

    StringBuilder highlightedActual = new StringBuilder();
    for (int i = 1; i < expectedParts.length; i++) {
        if (i < actualParts.length) {
            if (!expectedParts[i].trim().equalsIgnoreCase(actualParts[i].trim())) {
                // Wrap incorrect/missing values in red
                highlightedActual.append("<span style='color:red;'>").append(actualParts[i]).append("</span>");
            } else {
                highlightedActual.append(actualParts[i]); // Correct value
            }
        } else {
            // If actual has fewer columns, highlight missing parts
            highlightedActual.append("<span style='color:red;'>MISSING</span>");
        }

        if (i < expectedParts.length - 1) {
            highlightedActual.append(", ");
        }
    }
    return highlightedActual.toString();
}


private void writeResultsToCSV() {
        try (FileWriter csvWriter = new FileWriter(CSV_FILE_PATH)) {
            csvWriter.append("VARIANT_REG,MAKE,MODEL,YEAR\n");
            for (String[] result : results) {
                csvWriter.append(String.join(",", result)).append("\n");
            }
            logger.info(" Results successfully written to " + CSV_FILE_PATH);
        } catch (IOException e) {
            logger.severe(" Error writing to CSV file: " + e.getMessage());
        }
    }
}
