# Automation test for Funding Societies website

## Domain

### Website

https://fundingsocieties.com/

### Test cases list

**Note:** _Switching to **Closed** to view finished testcases_

https://github.com/thanhlelgg/fundingsocieties-e2e-tests/issues

### Prerequisites

- Install **Java Development Kit (JDK) 8**  (this is deployed on Java 8 so newer or older version are not recommended),
  set `JAVA_HOME` environment variable to installed folder
- Install **maven** and add maven execution file location to systems path
- Install Intellij IDE software (not required, for contribution only)
- Install Lombok from Plugins for Intellij IDE (not required, for contribution only)

### Run project and get report

#### Setup

**Note:** _although all browsers mentioned below are supported, only Chrome is properly tested and guarantee to work_

Enter the type of browser you want to run into the 'browser' parameter
at [test suite file](src/test/resources/test-suites/testng.xml). Ex: `chrome`, `firefox`
, `safari`, `edge`...
> `<parameter name="browser" value="browser_name"/>`

You can also comment out test you don't want to run or just remove them from the class list

#### Run project

> 1. From **Root folder** of Project
> 2. Enter `mvn clean test`

#### Get report

The emailable report can be found at `target/surefire-reports/emailable-report.html`

### Development

#### Technique used:

- **Java** as the language, **Selenium** as the core tool and **TestNG** as test runner.  
  The reason I chose Selenium for this because it's popular, easy to use, compatible with any systems, support most of
  the browsers and the one I'm most comfortable with. It's not the fastest tool, most lightweight nor easiest to use
  tool but still the safest option on the market.
- Library manager: **maven**, for the scope of this project, maven is relatively easier to use and more lightweight
  compared to **gradle**
- Design pattern: **Page Object Model**, control wrapper...
- Misc third party libs: **lombok** to help code faster, **opencsv** to work with csv file, **WebDriverManger** to help
  download web driver automatically, **log4j** and **slf4j** to support the logging...

#### Technical debt:

Currently, there's one failed test case in the default suite, where the total funded amount on statistic summary view is
not equals to the last quarter Amount disbursed value, I'm not sure if it's a bug or not, since I have not received any
document about the domain. So I just leave it failed for now.  
Other validation points are also a wild guess, that needs approval from domain owner before executing.

Another problem is about industry pie chart, where some slice is too small (from 0,01% to 0,1%) that it can't be
clicked/hovered by Selenium. And it can't be seen by human eyes as well. Currently, the test only passed because we
round up the percentage and the missing slice value is way too small.   
This is rather problem of the UI, because if end user can't see, then there's no point to automate the parts.

#### TODO list to improve this project:

- Add a more detailed report, with capture screenshot on failed (recommended: Extent Report). This task is quite
  time-consuming, so I won't implement it in this demo
- Currently, all data collected from chart are stored in the csv file, it's not really optimized. We could store it to a
  cloud system (like Google Drive, OneDrive...) or store it in a mailable report.
- Control wrapper, element helper, driver helper, report helper... should be provided through a submodule, or as a third
  party dependency, so it can be shared to other project
- Add a lib that support to check code style (recommended: checkstyle), so we can make sure everyone is on the same page
  when it came to coding style
- Support run test in parallel. This does not really cost much time to set up, but considering we only have 4 testcases,
  and only take less than 30 seconds to run, it's quite unnecessary.