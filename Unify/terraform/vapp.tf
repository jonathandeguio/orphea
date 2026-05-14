# Get the vApp template ID
resource "vcd_vapp" "orphea" {
  name     = var.vapp_name
  power_on = true

  # Enable customization at the vApp level
  metadata_entry {
    key         = "guestinfo.customization"
    value       = "enabled"
    is_system   = false
    user_access = "READWRITE"
    type        = "MetadataStringValue"
  }
}
