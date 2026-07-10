
# High Level Design - MoveToData.io Static Information Site

| Date | Name | Comment |
|:--|:--|:--|
| 27/09/2021 | Jim Morrish | Initial Draft.  High level architecture for the static HTML Information site. |
|  |  |  |

The static website is hosted via a Google Storage Bucket and Cloudflare's free CDN account to productionize its presentation - including presenting via our naked URL, providing full end-to-end SSL encryption using Google and Cloudflare's managed certificates.

There are many options for static hosting, including hosting directly from github with pages.github.io -- but the solution I have chosen gives us a great deal of flexibility and allows us to host the content anywhere without altering the content in any way.
 
<img src="MoveToData Static Hosting v0.1.jpg">

*movetodata.io architecture diagram*

Administration of the Google bucket is permitted through the GCP console.  The production website's bucket is hosted in the movetodata-prod project.

User IDP is delivered through MoveToData.io Google Workspace (Enterprise GMail & Google Docs etc.).

The movetodata.io domain registrar is gandi.net.  

Gandi.net directs DNS requests for movetodata.io to Cloudflare's name servers via two NS records:
<BR>**External nameservers**
<BR>decker.ns.cloudflare.com
<BR>kayleigh.ns.cloudflare.com

Within Cloudflare's DNS management there are two proxied CNAME records to *c.storage.googleapis.com*, one for movetodata.io and one for the www subdomain.

There is also a page rule within cloudflare to perform a 301 redirect of www.movetodata.io to movetodata.io. 

## Tutorials for the above:

**Google HTML Storage Bucket**<BR>
[https://medium.com/@pablo.delvalle.cr/google-cloud-storage-for-affordable-simple-static-site-hosting-c6ceb473db40](https://medium.com/@pablo.delvalle.cr/google-cloud-storage-for-affordable-simple-static-site-hosting-c6ceb473db40)

**Cloudflare Free Tier CDN**<BR>
[https://medium.com/@pablo.delvalle.cr/cloudflare-and-google-cloud-for-hosting-a-static-site-fd2e1a97aa9b](https://medium.com/@pablo.delvalle.cr/google-cloud-storage-for-affordable-simple-static-site-hosting-c6ceb473db40)

# Publishing content (Simplified)

The content of the site is held in the movetodata.io Github project here: [https://github.com/MoveToData-io/movetodata.io](https://github.com/MoveToData-io/movetodata.io)

A Github Action has been configured against the repository so that a push will automatically update the website.


