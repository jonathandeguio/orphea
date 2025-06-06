# Output the vApp template ID
output "ubuntu_id" {
  description = "The ID of the template."
  value       = data.vcd_catalog_vapp_template.ubuntu.id
}
