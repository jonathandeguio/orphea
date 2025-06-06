# Content

#### Purpose

Content checks validate the content of datasets against predefined rules. They ensure that data meets specified quality and consistency criteria.

#### Configuration

To configure a content check, you need to define rules to verify data values, ranges, uniqueness, and other properties. Here is an example configuration:

**Content Check Example**

- **Name:** Data Validity
- **Type:** Content
- **Criteria:**
  - Unique: Column ID
  - Range: Column Value (Min: 0, Max: 100)

#### Monitoring

Content checks can be monitored via the application dashboard. Alerts can be configured to notify stakeholders in case of data anomalies.

#### Best Practices

- **Clear Rules:** Define clear and precise content rules.
- **Continuous Validation:** Perform continuous validations to detect anomalies in real time.
- **Review Rules:** Regularly review content rules to ensure their relevance.
