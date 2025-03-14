# Car Valuation Test Automation

## 📌 Requirements

Ensure you have the following installed on your system before running the tests:

- **Java** (Version 8 or above)
- **Maven** (Latest version recommended)
- **Chrome WebDriver** (Managed via WebDriverManager)
- **Google Chrome Browser**
- **Cucumber & TestNG** (Dependencies managed via `pom.xml`)

## 📝 Summary of the Tests

This automation suite verifies the accuracy of car valuation details retrieved from **Car Analytics** based on vehicle registration numbers.

### 🔹 Test Workflow
1. **Reads Input Files** containing vehicle registration numbers.
2. **Extracts Registrations** using predefined patterns.
3. **Performs Car Valuation Search** via Selenium WebDriver.
4. **Handles Errors** (e.g., invalid registration format).
5. **Generates an Output CSV** with extracted details.
6. **Compares Results** against expected output.
7. **Generates Reports** highlighting matches, mismatches, and missing data.

## 📊 Summary of Reports

The framework generates two key reports:

1. **Cucumber HTML Report** (`target/cucumber-reports/cucumber-html-report.html`)
   - Standard Cucumber execution report with test scenarios and results.

2. **Comparison Report** (`target/cucumber-reports/comparison_report.html`)
   - Compares actual and expected car valuation data.
   - Highlights mismatches and missing data.
   - Uses color coding:
     - ✅ **Match** - Green
     - ❌ **Mismatch** - Red
     - 🚨 **Not Found** - Yellow

## 🚀 How to Run Tests

1. Clone the repository:
   ```sh
   git clone https://github.com/RogerRabbitMagnaCarta/carchecker_bdd.git
   cd carchecker_bdd
   ```

2. Install dependencies:
   ```sh
   mvn clean install
   ```

3. Run the tests using Maven:
   ```sh
   mvn test
   ```

## 📑 How to View Reports

### 🔹 View Cucumber HTML Report

After execution, open the Cucumber HTML report in your browser:
```sh
open target/cucumber-reports/cucumber-html-report.html
```

### 🔹 View Comparison Report

After execution, open the comparison report:
```sh
open target/cucumber-reports/comparison_report.html
```

This report will show discrepancies between expected and actual car details in a structured table.

---

For any issues, please raise a ticket or reach out to the team. Happy Testing! 🚀