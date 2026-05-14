# Data Sources

<div style="text-align: justify"> A Data Source is any external data system that can be connected to Orphea. Examples of a Data Source include, but are not limited to:

- Postgres database
- S3 bucket
- SAP instance
- REST API on the internet

To establish a connection to Orphea, a configured Data Source is required. It is important to note that a Data Source cannot be used directly in Orphea, data must be synchronised into a dataset before it can be utilized.

## Prerequisites

Before configuring a Source, ensure that you have the necessary credentials and access permissions to the data system.

## Data Source

This guide will take you through the process of adding a data source. The user must be a platform administrator to import a data source. To import it:

- Log in to your account
- Go to Data Connection using the sidebar menu

![ds1](../../_media/Ingestion/datasources/ds1.jpg)

- Go to + New
- Under the source tab, select the option for "Source" in the top-right corner

![ds2](../../_media/Ingestion/datasources/ds2.jpg)

- Enter name for the Source with Connector Details for desired connector and previously created Agent plus parent folder
- Click Create</div>
