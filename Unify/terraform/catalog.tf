data "vcd_catalog" "bosler" {
  org  = var.vcd_org
  name = var.vcd_catalog
}

data "vcd_catalog_vapp_template" "ubuntu" {
  org        = var.vcd_org
  catalog_id = data.vcd_catalog.bosler.id
  name       = var.vcd_template
}
