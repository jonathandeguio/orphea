# Status

#### Purpose

Status checks validate the operational status of data processing tasks and datasets. They ensure that all data pipelines and related components are running as expected.

#### Configuration

To configure a status check, you need to specify the expected status of various components. Here is an example configuration:

**Status Check Example**

- **Name:** Task Status Validation
- **Type:** Status
- **Criteria:**
  - Task: Data Ingestion
  - Expected Status: Running
  - Task: Data Transformation
  - Expected Status: Completed

#### Monitoring

Status checks can be monitored via the application dashboard. Alerts can be configured to notify stakeholders in case of unexpected status changes.

#### Best Practices

- **Automate Checks:** Automate status checks to ensure continuous monitoring.
- **Clear Definitions:** Define clear expected statuses for all critical tasks and components.
- **Incident Response:** Establish procedures for responding to status check failures.
