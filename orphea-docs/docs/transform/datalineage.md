# Data Lineage

A data pipeline is a system that manages the flow of data from various sources to a common destination. It is used to integrate data from different systems and create a unified view of the organization's data. Data pipelines are typically overseen by a team or individual who ensures that the data flows through the pipeline regularly and reliably.

Data pipelines typically involve several stages, including data syncing, schema imposition, data combination, and enabling teams to build use cases based on the common data foundation. In addition to these common features, data pipelines can be classified based on factors such as data scale, latency requirements, and maintenance complexity.

The type of pipeline used in Orphea is:

- Bezier

![Bezier](../_media/docs_ss/Transform/transform/DataLineage.gif)

## Setup

The pipeline setup process in Orphea involves using a point-and-click interface to set up a pipeline quickly and easily. Technical users can focus on declarative descriptions of the pipeline and desired outputs, while less technical users can create pipelines through a simplified schematic approach.

## Scheduling builds

Scheduling builds is an essential step in building a pipeline, as downstream data consumers expect data to be updated regularly. The frequency of data flow through a pipeline is determined by business requirements.
For example, some pipelines may run only weekly or daily, while others run on an hourly or more frequent basis.

## Quality

Data quality is an important aspect of data pipelines, and it is essential to check the quality of inputs and outputs at every step. Data synced from source systems often includes undefined values and poorly formatted or inconsistent data. Cleaning and normalizing data is a core part of the pipeline building process.

## Security

Security and governance are an essential aspects of data pipelines. Orphea's platform security primitives provide best-in-class capabilities for securing a data foundation and ensuring that sensitive data is handled appropriately.

## Support

Once a pipeline is published to production, it is important to think through the longevity of the pipeline from an organizational perspective.

Support processes for pipeline maintenance should be fleshed out, expectations should be clearly defined, and documentation should be available so that pipelines remain high-quality even as they are handed off from one team to another.

### Data Lineage offers a variety of capabilities, such as:-

The Bezier graph makes understanding data lineage effortlessly.
The ability to locate and discover datasets with ease.
The ability to visualize the flow of data from one parent cell to another.
The option to search for datasets using Project, table, and column names.
The ability to navigate Orphea Projects to examine data.
The ability to examine pipelines through an intuitive interface such as Bezier.

[//]: # (![Data Lineage1]&#40;../_media/old_doc_ss/Bezier/bezier1.png&#41;)
On the home page of Orphea you can visualize dataset pipelines. Just click on pipelines which will direct you to the Bezier page.

The ability to expand or collapse parents and children of datasets.
The ability to view attributes of a group of tables simultaneously.
The ability to visualize pipeline using coloring, such as highlighting out-of-date tables.
The ability to drill down into details about the data, such as its schema, when it was last created, and the code that generated the data.
The ability to collaborate with colleagues.
The ability to create pipeline snapshots to share with other users.

![Bezier](../_media/docs_ss/Transform/transform/DataLClose.gif)
Here in this above image, you can observe the flow of data.

:::tip
You can change the color of the nodes based on group the nodes with dropdown on right top of the screen.
:::
