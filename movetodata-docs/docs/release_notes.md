# Release Notes

---

## November 26th, 2024

### Global Search Improvements

- **New Features**:
  - Extended search capabilities for resource types and statuses.
  - Added "Recent Searches" functionality to streamline frequent queries.

### Data Mart Simplified

- **New Features**:
  - Simplified workflows for creating and managing data marts.
  - Reduced configuration overhead and enhanced usability for non-technical users.

### Snowflake Connector Performance Improvements

- **New Features**:

  - Optimized Snowflake connector to improve data transfer speed and reliability.
  - Enhanced query execution performance for large-scale datasets.

### Passport Storage Enhanced and Encrypted

- **New Features**:
  - Upgraded passport storage with encryption for enhanced data security.
  - Improved performance for storing and retrieving passport-related information.

### Docs Tutorial Videos

- **New Features**:
  - Added tutorial videos to the documentation for visual learning.
  - Comprehensive guides for common use cases and troubleshooting.

### Spark Details Logs

- **New Features**:

  - Introduced detailed Spark logs for advanced debugging and performance insights.
  - Logs include detailed metrics, Spark operations, and error tracebacks.

### Builder Synchronization with Database Sources

- **New Features**:

  - Build logs now reflect the synchronization process with database sources for better traceability.
  - Improved builder status logging to capture database operations.

### Header Preprocessing

- **New Features**:
  - Introduced header preprocessing for API and dataset requests to ensure clean and standardized data ingestion.
  - Automated handling of common header issues such as formatting and validation.

### CSV Preprocessing for API and Dataset

- **New Features**:
  - Enhanced CSV preprocessing pipelines for both API responses and dataset uploads.
  - Seamless integration of preprocessing capabilities with the API Connector for linked transformations.

### Bug Fixes:

- Resolved issues related to charts, dashboard functionality, security, and repository management.

---

## November 14th, 2024

### API Connector

An enhanced solution for preparing datasets using REST API requests.

- **New Features**:

  - Support for REST API authorization, including Bearer Token and API-KEY methods.
  - Capability to make grouped API requests and utilize data from previous requests to inform subsequent ones.
  - Transformation of JSON responses directly into datasets.

### Global Search improved

- Global Search has been improved to be able to search on resource type
- Ability to search based on status
- Recent Searches

### Bug Fixes:

- Resolved issues related to charts, dashboard functionality, security, and repository management.

---

## October 27th, 2024

### Access Token / Refresh Token Management

Enhanced token security to reduce exposure to potential threats.

- **Changes**:

  - Tokens are now stored securely in cookies instead of `localStorage` to mitigate XSS vulnerabilities.
  - Shortened access token lifespan (5-15 minutes) for increased security.
  - Refresh tokens now have a validity period of 7 days, after which users must re-authenticate with their password.

- **Bug Fixes and Improvements**:
  - Addressed various bugs and performance improvements related to charts, dashboard, security, and repository functionality.

---

## October 18th, 2024

### Multi-Factor Authentication (MFA)

Enhanced security through one-time password (OTP) generation using an authenticator app.

- **New Features**:

  - **Seamless MFA Setup**: Secure login enabled via an authenticator app with minimal screen switching for a smooth user experience.
  - **Recovery Codes**: Users can now generate recovery codes as a backup method for secure login.

- **Bug Fixes and Improvements**:
  - Addressed bugs and performance optimizations related to charts, dashboard functionality, security, and repository management.

---

## May 1st, 2023

### Kepler Release

- **New Features**:
  - Additional chart types for enhanced data visualization.
  - Improved dashboard filtering capabilities for more efficient data analysis.
  - Security hardening across the platform for robust data protection.
  - Integration of icon flows throughout the platform for better user experience.
  - Real-time build logs enabled via sockets for seamless monitoring.

---

## March 31st, 2023

### Kepler Release Updates

- **Improvements**:
  - Option to remove charts from the dashboard as needed.
  - Confirmation prompts for navigating away with unsaved changes on charts, helping prevent accidental data loss.

### General

- **Build Log Page**:
  - Updated layout and functionality for improved usability and information access.

---

## March 17th, 2023

### Code Repository

- **New Features**:
  - Collaborators avatars displayed for enhanced team visibility.
  - Support for SQL transformations within the repository.

### Kepler

- **Initial Features**:
  - Introduction of the initial set of chart functionalities.

---
