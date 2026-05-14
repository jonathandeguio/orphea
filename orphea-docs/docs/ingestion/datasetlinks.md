# Dataset Links

Creating a Link allows you to pull specific data from a Source and import it into Orphea. For instance, if you have a relational database Source that holds various tables, you can set up a Link to select a specific table and bring it into Orphea.
Establishing a Link results in the creation of a Orphea dataset within a selected Project. The process of transferring data from the Source to the dataset is carried out by building the dataset. While builds can be initiated manually, scheduling them is a more common practice.

To set up a Link, follow these steps:

- Identify the specific data from the Source that you wish to ingest into Orphea. This could be a specific table from a relational database set, for example.

- Determine the Project and location within Orphea where the data will be sent. This will be the location of the Orphea dataset that is created.

- Configure the Link by adding a schedule. This schedule will determine when data is automatically synchronized from the Source to the dataset. Note that builds can also be triggered manually.

Once these steps are completed, the Link will be set up and ready to use. Data will be ingested from the Source into Orphea according to the schedule defined in the configuration.
