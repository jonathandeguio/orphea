resource "google_compute_instance" "bosler-test" {
  name         = "desktop-svr"
  machine_type = "e2-micro"

  tags = ["desktop-svr"]

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-10"
    }
  }


  network_interface {
    network = "default"
    #network = "bnp-test"
    #subnetwork = "bnp-test"
    access_config {
    }
  }

  metadata = {
    foo = "bar"
  }

  metadata_startup_script = file("./startup.sh")

}