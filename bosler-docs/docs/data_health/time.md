# Time

#### Purpose

Task-level checks aim to validate the successful execution of tasks that produce datasets. They ensure that each task completes correctly and that the resulting data meets expectations.

#### Configuration

To configure a task-level check, you need to specify the success and performance criteria. Here is an example configuration:

**Task-Level Check Example**

- **Task Name:** Validate Data Ingestion
- **Type:** Task-Level
- **Criteria:**
  - Success
  - Duration: Maximum 1 hour

#### Monitoring

Task-level checks can be monitored via the application dashboard. Alerts can be configured to notify stakeholders in case of task failure or exceeding the allotted time.

#### Best Practices

- **Define Clear Criteria:** Specify clear success criteria for each task.
- **Automation:** Automate the checks to ensure continuous monitoring.
- **Failure Analysis:** Analyze task failures to identify points of failure and improve processes.

---

### Build-Level Checks

#### Purpose

Build-level checks ensure that builds complete successfully and within a predetermined time. They monitor the stability and performance of data build processes.

#### Configuration

To configure a build-level check, you need to specify the success and performance criteria. Here is an example configuration:

**Build-Level Check Example**

- **Build Name:** Data Transformation Build
- **Type:** Build-Level
- **Criteria:**
  - Success
  - Duration: Maximum 2 hours

#### Monitoring

Build-level checks can be monitored via the application dashboard. Alerts can be configured to notify stakeholders in case of build failure or exceeding the allotted time.

#### Best Practices

- **Optimize Processes:** Optimize build processes to minimize execution times.
- **Continuous Monitoring:** Use continuous monitoring tools to detect anomalies in real time.
- **Documentation:** Document build configurations to facilitate maintenance and updates.

---

### Freshness Checks

#### Purpose

Freshness checks ensure that datasets are up to date. They verify that data is updated within the expected timeframe, ensuring its relevance and utility.

#### Configuration

To configure a freshness check, you need to define the maximum acceptable age of the data. Here is an example configuration:

**Freshness Check Example**

- **Name:** Dataset Freshness
- **Type:** Freshness
- **Criteria:**
  - Maximum Age: 24 hours

#### Monitoring

Freshness checks can be monitored via the application dashboard. Alerts can be configured to notify stakeholders when data is not updated within the expected timeframe.

#### Best Practices

- **Schedule Updates:** Schedule regular updates of datasets.
- **Automate Workflows:** Automate workflows to ensure timely data updates.
- **Review Criteria:** Regularly review freshness criteria to align with business requirements.
