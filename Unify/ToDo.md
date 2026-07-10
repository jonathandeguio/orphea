## Infrastructure To Do 

- Fix SuperSet's external IP
- Test Helm build
- Investigate autoscaling and shutdown options to reduce costs
- Build entire deployment with Terraform (incl Github actions, Cloudflare DNS)
- Redesign Spark Services
- Move storage buckets for the websites to a safe tenant (main movetodata.io?)
- Set up an external image repo - look at options (main movetodata.io?)
- Add commands to automatically build triggers - look at moving from Webhooks to Actions
- Secrets safety - Google Secrets Manager, Hashicorp Vault, native k8s options.
- Automate granting permissions to common repo. See https://cloud.google.com/artifact-registry/docs/access-control?hl=en-GB &
	https://cloud.google.com/artifact-registry/docs/access-control?hl=en-GB#grant 

### Trello board:  https://trello.com/b/4qTPpA7r/movetodata-infrastructure
