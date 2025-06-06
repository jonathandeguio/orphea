# Fetch the VDC Group instead of the VDC
data "vcd_vdc_group" "group" {
  name = var.vcd_vdc_group
}

# Fetch the NSX-T Edge Gateway, ensuring we reference the VDC group instead of a VDC
data "vcd_nsxt_edgegateway" "t1" {
  org      = var.vcd_org
  owner_id = data.vcd_vdc_group.group.id # Use the VDC group ID, not the VDC ID
  name     = var.vcp_vapp_nsxt_edgegateway_name
}

data "vcd_nsxt_app_port_profile" "ssh-application" {
  org        = var.vcd_org
  context_id = data.vcd_vdc_group.group.id
  name       = "SSH"
  scope      = "SYSTEM"
}

resource "vcd_nsxt_nat_rule" "dnat" {
  org = var.vcd_org

  edge_gateway_id     = data.vcd_nsxt_edgegateway.t1.id
  name                = var.db_dnat_name
  rule_type           = var.db_dnat_rule_type
  description         = var.db_dnat_description
  external_address    = var.db_external_address
  dnat_external_port  = var.db_dnat_external_port
  internal_address    = var.db_static_ip[0]
  logging             = var.db_dnat_logging
  app_port_profile_id = data.vcd_nsxt_app_port_profile.ssh-application.id
}
