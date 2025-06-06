# Overview

## Initial setup

<div style="text-align: justify">This documentation will guide you through the process of establishing a connection between your organization's data and the Bosler platform for the first time.

It is important to note that the initial setup of this connection is primarily a networking task, and should be handled by a member of your IT team who has experience in network engineering and is familiar with your organization's network topology and firewall configurations.

![Bosler Network](../../_media/boslerdiagram.jpg)

## Concept

In order to connect data to Bosler, the following three components must be properly installed and configured: the Agent, the Source, and the Link.

### Agent

The Agent, a software component that runs within an organization's network, serves as a secure intermediary between the organization's data sources and the Bosler instance. It is required for connecting to certain data sources, unless the data source is a cloud-based one that Bosler can access directly.

Additionally, a single Agent can support multiple Data Sources and Links

- [Learn more about the Agent architecture.](../ingestion/installagent.md)

### Data Sources

To establish a connection with Bosler, it is necessary to utilize an external data system known as a Source.

These sources can include, among others, a Postgres database, an S3 bucket, a file system on a Linux server, an SAP instance, and a REST API on the internet. It should be noted that prior to making a connection to Bosler, the Source must be appropriately configured. Furthermore, it is important to understand that a Source cannot be directly accessed within Bosler, as the data must be synchronized into a dataset before it can be employed.

### Links

A Link is responsible for obtaining specific data from a Source and incorporating it into Bosler. For instance, if a Postgres database Source contains various tables, it is possible to configure a Link to ingest a particular table into Bosler. Once a Link has been executed successfully, the outcome within Bosler will be a dataset, which can be utilized across all of Bosler's data processing, model development, and analytical tools.

- [Learn more about the Data Sources and Links.](/docs/ingestion/datasources.md)
</div>
