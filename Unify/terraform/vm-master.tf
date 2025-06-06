# db nodes setup
resource "vcd_vapp_vm" "master_node" {
  count            = var.master_vm_count
  name             = "master-${format("%02d", count.index + 1)}"
  vapp_name        = vcd_vapp.bosler.name
  vapp_template_id = data.vcd_catalog_vapp_template.ubuntu.id
  memory           = var.master_memory
  cpus             = var.master_cpu

  # Attach Data Disk
  disk {
    name        = vcd_independent_disk.master_disk[count.index].name
    bus_number  = 1
    unit_number = 0
  }

  # First Network Interface (with Static IP)
  network {
    type               = var.vcd_org_network_type
    name               = vcd_vapp_org_network.comman-net-card.org_network_name
    ip_allocation_mode = var.vcd_org_ip_allocation_mode
    ip                 = var.master_static_ip[count.index]
    is_primary         = true
  }

  # Second Network Interface (with DHCP)
  network {
    type               = var.vcd_org_network_type
    name               = var.vcd_vapp_nfs_network_name
    ip_allocation_mode = var.vcd_vapp_nfs_ip_allocation_mode
    adapter_type       = var.vcd_vapp_nfs_adapter_type
  }

  # Guest Customization Block
  customization {
    enabled = true
  }

  # Guestinfo Variables Passed via guest_properties
  guest_properties = {
    "guest.hostname"        = "master-${format("%02d", count.index + 1)}"
    "guestinfo.ip_address"  = var.master_static_ip[count.index]
    "guestinfo.gateway"     = var.vcd_vapp_gateway
    "guestinfo.dns_servers" = var.vcd_vapp_dns
  }

  # Automatically power on the VM after creation
  power_on = true
}
