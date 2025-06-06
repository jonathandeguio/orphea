# Size

#### Purpose

Size checks ensure that the dataset size falls within the expected range. They help detect anomalies such as missing data or unexpected additions.

#### Configuration

To configure a size check, you need to define the expected size range of the data. Here is an example configuration:

**Size Check Example**

- **Name:** Dataset Size Validation
- **Type:** Size
- **Criteria:**
  - Minimum Size: 10 MB
  - Maximum Size: 100 MB

#### Monitoring

Size checks can be monitored via the application dashboard. Alerts can be configured to notify stakeholders when the dataset size falls outside the expected range.

#### Best Practices

- **Define Clear Ranges:** Specify clear minimum and maximum size limits for each dataset.
- **Regular Monitoring:** Continuously monitor dataset sizes to quickly identify anomalies.
- **Adjust Criteria:** Regularly review and adjust size criteria to align with changing data volumes.
