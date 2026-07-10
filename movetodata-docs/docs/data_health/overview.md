# Overview

### Data Health Checks Reference

Our application provides robust data health checks to monitor and validate the quality, consistency, and freshness of your datasets. These checks help ensure that your data pipelines operate smoothly and that your data meets the required standards. This document outlines the different types of data health checks available and how to configure and use them effectively.

1. **Job-Level Checks**

   - **Purpose:** Validate the successful completion of jobs that produce datasets.
   - **Configuration:** Set up to run after a job finishes to ensure it meets the defined criteria.

2. **Build-Level Checks**

   - **Purpose:** Ensure builds complete successfully and within an expected duration.
   - **Configuration:** Applied to dataset builds to monitor their execution time and success rate.

3. **Freshness Checks**

   - **Purpose:** Verify that datasets are up-to-date.
   - **Configuration:** Compare the dataset's latest update time with the current time to ensure data is refreshed as expected.

4. **Content Checks**

   - **Purpose:** Validate the content of datasets against predefined rules.
   - **Configuration:** Implement rules to check data values, ranges, uniqueness, and other content-related properties.

5. **Schema Checks**
   - **Purpose:** Ensure the dataset schema matches the expected structure.
   - **Configuration:** Compare the current schema with the expected schema and validate column names, data types, and constraints.

#### Setting Up Health Checks

1. **Defining Checks**

   - Specify the type of check you want to implement.
   - Define the criteria and thresholds for each check.

2. **Applying Checks**

   - Attach checks to datasets, jobs, or builds using the configuration interface.
   - Ensure that the checks run at appropriate stages in your data pipeline.

3. **Monitoring and Alerts**
   - Set up notifications for check failures to alert relevant stakeholders.
   - Use the monitoring dashboard to view the status of all health checks.

#### Example Configurations

**Job-Level Check Example**

- **Job Name:** Validate Data Ingestion
- **Type:** Job-Level
- **Criteria:**
  - Success
  - Duration: Maximum 1 hour

**Build-Level Check Example**

- **Build Name:** Data Transformation Build
- **Type:** Build-Level
- **Criteria:**
  - Success
  - Duration: Maximum 2 hours

**Freshness Check Example**

- **Name:** Dataset Freshness
- **Type:** Freshness
- **Criteria:**
  - Maximum Age: 24 hours

**Content Check Example**

- **Name:** Data Validity
- **Type:** Content
- **Criteria:**
  - Unique: Column ID
  - Range: Column Value (Min: 0, Max: 100)

**Schema Check Example**

- **Name:** Schema Validation
- **Type:** Schema
- **Criteria:**
  - Match Expected Schema:
    - Column: ID, Type: Integer
    - Column: Name, Type: String
    - Column: Value, Type: Float

#### Best Practices

- **Regular Updates:** Keep your health checks updated to reflect changes in your data pipelines and business requirements.
- **Automated Monitoring:** Automate health checks and monitoring to ensure continuous validation without manual intervention.
- **Comprehensive Coverage:** Use a combination of different types of checks to cover various aspects of data quality and pipeline health.

By implementing these data health checks, you can ensure the reliability, accuracy, and timeliness of your data, supporting better decision-making and operational efficiency.
