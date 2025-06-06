#!/bin/bash

#
# TO DO - finish script!
# Problem with identifying which non-root user to execute parts of this as
#


userid=$1
echo $userid > /TEST

apt-get update
apt install -y xfce4 xfce4-goodies tightvncserver 
mv ~/.vnc/xstartup ~/.vnc/xstartup.bak
cat > ~/.vnc/xstartup <<EOF
#!/bin/bash
xrdb $HOME/.Xresources
startxfce4 &
EOF

chmod +x ~/.vnc/xstartup
# /etc/systemd/system/vncserver@.service
systemctl daemon-reload
vncserver -kill :1
systemctl start vncserver@1
systemctl status vncserver@1

prog=/usr/bin/vncpasswd
mypass="Chang3m3ASAP!"
/usr/bin/expect <<EOF
spawn "$prog"
expect "Password:"
send "$mypass\r"
expect "Verify:"
send "$mypass\r"
expect eof
exit
EOF

wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
apt install -y ./google-chrome-stable_current_amd64.deb

# To connect from local machine:
# ssh -L 59000:localhost:5901 -C -N -l your_login your_server_ip
# ...or with GCP SDK:
# gcloud compute ssh --zone us-central1-c tunnel -- -N -p 59000 -D localhost:5901

