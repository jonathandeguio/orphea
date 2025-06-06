terraform {
  required_version = ">= 1.9.0"

  required_providers {
    vcd = {
      source  = "vmware/vcd"
      version = ">= 3.14"
    }
  }
}

# Configure the VMware Cloud Director Provider
provider "vcd" {
  user                 = var.vcd_user
  password             = var.vcd_pass
  org                  = var.vcd_org
  vdc                  = var.vcd_vdc
  url                  = var.vcd_url
  max_retry_timeout    = var.vcd_max_retry_timeout
  allow_unverified_ssl = var.vcd_allow_unverified_ssl
}
