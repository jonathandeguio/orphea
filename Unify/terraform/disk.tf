# Independent disk for db nodes
resource "vcd_independent_disk" "db_disk" {
  count        = var.db_disk_count
  name         = "db-test-${format("%02d", count.index + 1)}"
  size_in_mb   = var.db_disk_size
  bus_type     = var.bus_type
  bus_sub_type = var.bus_sub_type
  depends_on   = [vcd_vapp.orphea]
}

# Independent disk for master nodes
resource "vcd_independent_disk" "master_disk" {
  count        = var.master_disk_count
  name         = "master-test-${format("%02d", count.index + 1)}"
  size_in_mb   = var.master_disk_size
  bus_type     = var.bus_type
  bus_sub_type = var.bus_sub_type
  depends_on   = [vcd_vapp.orphea]
}

# Independent disk for worker nodes
resource "vcd_independent_disk" "worker_disk" {
  count        = var.worker_disk_count
  name         = "worker-test-${format("%02d", count.index + 1)}"
  size_in_mb   = var.worker_disk_size
  bus_type     = var.bus_type
  bus_sub_type = var.bus_sub_type
  depends_on   = [vcd_vapp.orphea]
}
