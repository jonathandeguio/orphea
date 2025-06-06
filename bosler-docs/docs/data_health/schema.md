# Schema

#### Purpose

Schema checks ensure that the dataset schema matches the expected structure. They verify the conformity of column names, data types, and constraints.

#### Configuration

To configure a schema check, you need to define the expected schema and compare it with the actual schema. Here is an example configuration:

**Schema Check Example**

- **Name:** Schema Validation
- **Type:** Schema
- **Criteria:**
  - Match Expected Schema:
    - Column: ID, Type: Integer
    - Column: Name, Type: String
    - Column: Value, Type: Float

#### Monitoring

Schema checks can be monitored via the application dashboard. Alerts can be configured to notify stakeholders in case of schema discrepancies.

#### Best Practices

- **Well-Defined Schema:** Define a clear and precise schema for each dataset.
- **Continuous Monitoring:** Use continuous monitoring tools to detect schema discrepancies in real time.
- **Documentation:** Document schemas to facilitate maintenance and updates.
