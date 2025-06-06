# nstall using the apt repository
# Set up the repository
# Step 1 : Update the apt package index and install packages to allow apt to use a repository over HTTPS:
 apt-get -y update
 apt-get -y install ca-certificates curl gnupg

# Step 2 : Add Docker’s official GPG key:
 install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
 chmod a+r /etc/apt/keyrings/docker.gpg

# Step 3 : Use the following command to set up the repository:
echo  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu  focal stable" >   /etc/apt/sources.list.d/docker.list 

  # Install Docker Engine
  # Step 1 : Update the apt package index:
   apt-get -y update

  # Step 2 : Install Docker Engine, containerd, and Docker Compose
   apt-get -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

  # If this above command is not working then use this command 
  #sudo apt install docker.io

  # Step 3: Verify that the Docker Engine installation is successful by running the hello-world image.
   docker run hello-world
