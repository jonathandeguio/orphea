provider "google" {

  credentials = file("/tmp/cred.json")
  project = "bosler-foo"
  region  = "us-central1"
  zone    = "us-central1-c"
}