# Architecture

Bosler is an architecture that is built to cater to all types of users, handle high-demand data-driven workloads and adapt to various infrastructure substrates. To accomplish this, it utilizes a service mesh that is managed by Palantir Apollo and follows a set of software-defined principles.
As Bosler has grown in importance for many organizations, it has been important to ensure that:

* Auto-scaling of Bosler services and the associated compute mesh is based on a consistent containerization paradigm. This is achieved using Kubernetes Docker engine, which powers all of Bosler's auto-scaling infrastructure.

* Upgrades to Bosler are done without any downtime, with detailed monitoring to guide upgrade strategies, monitor progress and roll back if necessary.

* Security and lineage are integral to every aspect of Bosler, and are consistently upheld throughout the platform's architecture. This ensures that no single service or user is responsible for enforcing the organization's security policies or keeping track of data provenance. From data to decision-making, Bosler's core services are designed to implement, enforce, and monitor governance policies that are configured, synced and inherited. This way, the platform can ensure that all operations are compliant with the enterprise's standards for security and data governance.

* We strive to make sure that the most widely used open-source languages are securely and consistently accessible within code-driven paradigms. This includes languages such as Python, SQL and R; Python and R for machine learning workflows.

* All services within Bosler are configured in a highly-available, redundant setup. This includes not just the core backend services but also front-end application services, visualization tools, build executions and all the services used by different users.

* Bosler is designed to be storage-agnostic, meaning it can work with various storage technologies across different levels of the architecture. This includes utilizing blob storage or HDFS, S3. This flexibility allows Bosler to adapt to the specific storage needs of an organization.

* Bosler compute is mainly based on Apache Spark which is proven technology for massive scale data.
