provider "google" {

  credentials = file("/tmp/cred.json")
  project = "orphea-foo"
  region  = "us-central1"
  zone    = "us-central1-c"
}

