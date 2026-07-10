# In Browser (Gmail & GCP)

1. Sign out of all Google accounts ( or launch browser in inCognitio )
2. Create free gmail account - make note of password!  Use reasonable security (2FA) in Settings.
3. Go to [GCP](https://console.cloud.google.com/getting-started) - create new gcp tenant and activate free trial<BR> 		- MUST HAVE VALID CREDIT CARD and billing address**<BR> 		- THIS NEEDS REALTIME AUTHENTICATION from the card provider**
4. Create a new project with a memorable name (e.g. septbos22)
5. Link the new billing account:  Select the new project account in the top left drop-down and select 'Billing' from the main menu, it should prompt that no Billing Account is associated; select the new Billing Account from the menu in the prompt, or else go to Billing and manually associate the new Billing Account with the project.
6. Go to Compute Engine in the main menu and enable compute engine API
7. Go to Kubernetes Engine in the main menu and enable kubernetes engine API

***Below is not need if artifactory and kubernetes is in same tenant***
8. Go to IAM & Admin in the main menu and make a note of the Compute Engine default service account, e.g.:<BR> [751339998510-compute@developer.gserviceaccount.com](mailto:751339998510-compute@developer.gserviceaccount.com)  (example from July tenant)
9. Grant access to the repos in the main movetodata GCP tenant (movetodata-334213 project) from the Compute Engine default service account:  In 'IAM & Admin' grant 'Artifact Registry Reader' role to the new Compute Engine default service account.


# From the command line:
**Notes**:
* Google Cloud SDK must be installed on local machine or run within the Google Cloud Shell.  [https://cloud.google.com/sdk/docs/install](https://cloud.google.com/sdk/docs/install)
* The Kubernetes Kubectl command-line tool needs to be installed on the computer (this is installed by default in Google Cloud Shell)  [https://kubernetes.io/docs/tasks/tools/install-kubectl-macos/](https://kubernetes.io/docs/tasks/tools/install-kubectl-macos/)
* Install the gke-gcloud-auth-plugin:  gcloud components install gke-gcloud-auth-plugin
* Configure git command line utility on local machine -- local account must be granted access to the MoveToData Github repositories.
* You must have the SSL certificates (crt and key files), see step below for CERT_FILE_PATH and KEY_FILE_PATH variables.<BR>
### 
1. Clone the Unify git repo locally, i.e.<BR>
	git clone --recursive [git@ssh.github.com](mailto:git@ssh.github.com):MoveToData-io/Unify.git
2. cd Unify/GKE
3. Set up SDK project and user account:<BR> # gcloud auth login    <-- this should open a browser to authenticate the new tenant
4. Set the project, region, and zone:<BR> # gcloud config set compute/zone europe-west1<BR> # gcloud config set compute/region europe-west1-b<BR> # gcloud config set project \<new-project-name\><BR> Verify with:<BR> # gcloud config list<BR>
5. Change project id in env.sh, this file used as a global variables BR> PROJECT_ID=\<new-project-name\>
6. Change the CERT_FILE_PATH and KEY_FILE_PATH variables to the location on the local machine.  (These can be downloaded/regenerated from [gandi.net](https://gandi.net) - don't use these for Production/Customer workloads)
7. Run the create_gke_cluster.sh script:<BR> # ./create_gke_cluster.sh
8. Run the [start.sh](https://start.sh) script:<BR> # ./start.sh

# In Browser (GCP & Cloudflare)
1. In the GCP console for the new project, go to the 'VPC network' in the main menu and look in IP Addresses to get the External Static IP address named movetodata.
2. Log into Cloudflare:  [https://dash.cloudflare.com/login](https://dash.cloudflare.com/login) 
3. In DNS, change IPv4 address for dev to the new IP address
<BR>

# To close the old GCP/Gmail account:
1. In the Google Cloud console, go to the Account management page. Go to Account management in the Cloud Billing console.
2. At the prompt, choose the Cloud Billing account that you want to close.
3. At the top of the page, click Close billing account.<BR>
	[Create, modify, or close your self-serve Cloud Billing account](https://cloud.google.com/billing/docs/how-to/manage-billing-account)
4. Log into gmail; select 'Manage your account'; delete the gmail account.
<BR>
