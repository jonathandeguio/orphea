# VM Template

variable "vcd_catalog" {
  description = "The catalog name containing vm templates."
  type        = string
}

variable "vcd_template" {
  description = "Name of the vm template."
  type        = string
}

# Common

variable "vcd_user" {
  description = "Username for vcd."
  type        = string
}

variable "vcd_pass" {
  description = "Password for vcd."
  type        = string
  sensitive   = true
}

variable "vcd_url" {
  description = "VCD url."
  type        = string
}

variable "vcd_max_retry_timeout" {
  description = "Maximum retry timeout for api calls in seconds."
  type        = number
  default     = 60
}

variable "vcd_allow_unverified_ssl" {
  description = "Allow unverified ssl connections."
  type        = bool
  default     = true
}

variable "vcd_org" {
  description = "Organization in vcd."
  type        = string
}

variable "vcd_vdc" {
  description = "VDC in vcd."
  type        = string
}

variable "vcd_org_network" {
  description = "The network name in the organization."
  type        = string
}

variable "vapp_name" {
  description = "The name of the existing vapp."
  type        = string
}

variable "vcd_vdc_group" {
  description = "VDC group name for the organization."
  type        = string
}

variable "vcd_vapp_gateway" {
  description = "VDC organization nfs network."
  type        = string
}

variable "vcd_vapp_dns" {
  description = "DNS for vapp."
  type        = string
}

variable "vcd_org_network_type" {
  description = "Org network type."
  type        = string
}

variable "vcd_org_ip_allocation_mode" {
  description = "Org network ip allocation mode."
  type        = string
}

# IP Sets && DNAT Configuration variables Applicable only for DB VM

variable "vcp_vapp_nsxt_edgegateway_name" {
  description = "NSXT edge gateway name."
  type        = string
}

variable "vcd_vapp_nsxt_ip_sets_name" {
  description = "IP set name."
  type        = string
}

variable "vcd_vapp_nsxt_ip_sets_description" {
  description = "IP set description."
  type        = string
}

variable "vcd_vapp_nsxt_ip_sets_ips" {
  description = "IP set list of ip addresses."
  type        = list(string)
}

# DNAT Stays On DB

variable "db_dnat_name" {
  description = "DB dnat name."
  type        = string
}

variable "db_dnat_rule_type" {
  description = "DB dnat rule type."
  type        = string
}

variable "db_dnat_description" {
  description = "DB dnat description."
  type        = string
}

variable "db_dnat_external_port" {
  description = "DB dnat external port."
  type        = string
}
variable "db_dnat_logging" {
  description = "DB dnat logging."
  type        = bool
}

# Disk common

variable "bus_type" {
  description = "Disk bus type."
  type        = string
}
variable "bus_sub_type" {
  description = "Disk bus sub type."
  type        = string
}

# NFS Network Card

variable "vcd_vapp_nfs_network_name" {
  description = "Org nfs network name."
  type        = string
}
variable "vcd_vapp_nfs_ip_allocation_mode" {
  description = "Org nfs ip allocation mode."
  type        = string
}

variable "vcd_vapp_nfs_adapter_type" {
  description = "Org nfs network name."
  type        = string
}

# DB

variable "db_vm_count" {
  description = "Number of db vm's."
  type        = number
}

variable "db_cpu" {
  description = "Count of cpu."
  type        = number
}

variable "db_memory" {
  description = "Memory allocation amount."
  type        = number
}

variable "db_disk_count" {
  description = "No of db vm's disk."
  type        = number
}

variable "db_disk_size" {
  description = "No of db vm's disk."
  type        = number
}

variable "db_static_ip" {
  description = "Db vm static ip address for the vm."
  type        = list(string)
}

variable "db_external_address" {
  description = "DB external ip."
  type        = string
}

# Master 

variable "master_vm_count" {
  description = "Master vm count."
  type        = number
}

variable "master_cpu" {
  description = "Master cpu count."
  type        = number
}

variable "master_memory" {
  description = "Master ram allocation."
  type        = number
}

variable "master_disk_count" {
  description = "No of db vm's disk."
  type        = number
}
variable "master_disk_size" {
  description = "Disk bus type."
  type        = string
}

variable "master_static_ip" {
  description = "Db vm static ip address for the vm."
  type        = list(string)
}

# Worker

variable "worker_vm_count" {
  description = "Worker vm count."
  type        = number
}

variable "worker_cpu" {
  description = "Worker cpu count."
  type        = number
}

variable "worker_memory" {
  description = "Ram allocation."
  type        = number
}

variable "worker_disk_count" {
  description = "No of db vm's disk."
  type        = number
}
variable "worker_disk_size" {
  description = "Disk bus type."
  type        = string
}

variable "worker_static_ip" {
  description = "Db vm static ip address for the vm."
  type        = list(string)
}
