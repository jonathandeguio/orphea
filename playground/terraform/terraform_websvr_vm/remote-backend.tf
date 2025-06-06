terraform {
  backend "gcs" {
    bucket      = "tf_foo_test"
    prefix      = "terraform/state1"
    credentials = "/tmp/cred.json"  #mention here the name and add service account key inside same folder
  }
}