resource "vcd_nsxt_ip_set" "ip_set" {
  org             = var.vcd_org
  edge_gateway_id = data.vcd_nsxt_edgegateway.t1.id
  name            = var.vcd_vapp_nsxt_ip_sets_name
  description     = var.vcd_vapp_nsxt_ip_sets_description
  ip_addresses    = var.vcd_vapp_nsxt_ip_sets_ips
}
