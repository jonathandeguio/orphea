resource "vcd_vapp_org_network" "comman-net-card" {
  vapp_name        = vcd_vapp.bosler.name
  org_network_name = var.vcd_org_network

  reboot_vapp_on_removal = true
}

resource "vcd_vapp_org_network" "nfs-net-card" {
  vapp_name        = vcd_vapp.bosler.name
  org_network_name = var.vcd_vapp_nfs_network_name

  reboot_vapp_on_removal = true
}
