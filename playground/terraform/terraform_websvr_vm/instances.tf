resource "google_compute_instance" "web" {
  name         = "webserver"
  machine_type = "f1-micro"

  tags = ["http-server"]

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-9"
    }
  }


  scheduling {
    preemptible       = true
    automatic_restart = false
  }

  network_interface {
    #network = "default"
    network = "bnp-test"
    subnetwork = "bnp-test"
    access_config {
    }
  }

  metadata = {
    foo = "bar"
  }

  metadata_startup_script = file("./startup.sh")

}