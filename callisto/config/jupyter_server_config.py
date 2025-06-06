"""Configuration for the Jupyter development server."""

import os
import sys
sys.path.append('/etc/jupyter')
from customauthenticator import CustomIdentityProvider, CustomAuthorizer

#################
# Logging
#################

c.ServerApp.log_level = 'DEBUG'

#################
# Network
#################

c.ServerApp.ip = '0.0.0.0'
c.ServerApp.port = 8686
c.ServerApp.port_retries = 0

#################
# Browser
#################

c.ServerApp.open_browser = False

#################
# Terminal
#################

c.ServerApp.terminals_enabled = False

#################
# Authentication
#################

# TODO : this needs to be created automatically from api while creating instance of jupyter
# c.ServerApp.token = '${NOTEBOOK_TOKEN}'

#################
# Security
#################

c.ServerApp.disable_check_xsrf = False
# ORIGIN = 'http://localhost:3208'
ORIGIN = '*'
# c.ServerApp.allow_origin = ORIGIN
c.ServerApp.allow_origin_pat = '.*'
c.ServerApp.allow_credentials = True
c.ServerApp.tornado_settings = {
  'headers': {
#    'Access-Control-Allow-Origin': ORIGIN,
    'Access-Control-Allow-Methods': '*',
    'Access-Control-Allow-Headers': 'Accept, Accept-Encoding, Accept-Language, Authorization, Cache-Control, Connection, Content-Type, Host, Origin, Pragma, Referer, sec-ch-ua, sec-ch-ua-mobile, sec-ch-ua-platform, Sec-Fetch-Dest, Sec-Fetch-Mode, Sec-Fetch-Site, Upgrade, User-Agent, X-XSRFToken, X-Datalayer, Expires',
    'Access-Control-Allow-Credentials': 'true',
    'Content-Security-Policy': f"frame-ancestors 'self' {ORIGIN} ",
  },
  'cookie_options': {
    'SameSite': 'None',
    'Secure': True
  }
}
c.ServerApp.cookie_options = {
  "SameSite": "None",
  "Secure": True,
}

# Enable auto-completion feature
c.Completer.use_jedi = False

#################
# Server Extensions
#################

# c.ServerApp.jpserver_extensions = {
#     'jupyterlab': False,
# }

# jupyter_server_config.py
import shutil

# c is a magic, lazy variable
c.LanguageServerManager.language_servers = {
    "pylsp": {
        # if installed as a binary
        "argv": [shutil.which("pylsp")],
        "languages": ["python"],
        "version": 2,
        "mime_types": ["text/x-python"],
        "display_name": "Python Language Server"
    },
}

#################
# Content
#################


# c.FileContentsManager.delete_to_trash = False
# content_dir = os.path.dirname(os.path.realpath(__file__)) + '/notebooks'
# from pathlib import Path
# home = str(Path.home())
# Below needs to come from environment variable or configuration
# content_dir = "/root"
# c.ServerApp.root_dir = content_dir
# c.ServerApp.preferred_dir = content_dir

#################
# URLs
#################

# c.ServerApp.base_url = '/api/jupyter'
# c.ServerApp.default_url = '/api/jupyter/disabled'

#################
# Kernel
#################

# See
# https://github.com/jupyterlab/jupyterlab/pull/11841
# https://github.com/jupyter-server/jupyter_server/pull/657
c.ServerApp.kernel_ws_protocol = None # None or ''

#################
# JupyterLab
#################

c.LabApp.collaborative = False

c.ServerApp.identity_provider_class = CustomIdentityProvider
c.ServerApp.authorizer_class = CustomAuthorizer

#####################
# for load balancer
#####################
c.JupyterHub.extra_handlers = [
    ('/healthcheck', lambda x: x.set_status(200)),
]
