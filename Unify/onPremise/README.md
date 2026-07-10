To create movetodata bundle, run below

```
./build_movetodata_offline.sh bundle
```

Copy the bundle to install PC or Server

Prepare :

```
sudo mkdir /movetodata && sudo tar -C /movetodata -xf movetodata_bundle.tar.gz && sudo /movetodata/bundle/build_movetodata_offline.sh prepare

```

After above command reboot

```
reboot
```

The below command is to install movetodata

```
sudo /movetodata/bundle/build_movetodata_offline.sh install
```

Additional info:

- Make sure not to overwrite ingress yaml. it is different in onPremise than GCP. It uses Prefix and /\* des not work.

- Kubernetes will try to pull images if they not set to IfNotPresent

- MetalLB IP should be changed as per the network.

References :

https://docs.tigera.io/calico/latest/getting-started/kubernetes/quickstart

https://docs.tigera.io/calico/latest/getting-started/kubernetes/flannel/migration-from-flannel

https://github.com/sandervanvugt/cka
