import ReactPlayer from "react-player"

# Tokens

Tokens are MoveToData’s security authentication method.
MoveToData uses tokens to send and receive data to and from external systems.

## Short Term Token

Every time a user logs into MoveToData, they generate a short term token assigned to their profile which lasts for one day. This keeps the user logged in for the day reducing the need to log in multiple times while ensuring security. Tokens can also be used for single sign on capabilities in MoveToData

## Long Term Tokens

MoveToData uses long term tokens for external systems to send and receive data. Only administrators can generate tokens and assign them. Tokens can be viewed in the settings page under the Tokens tab.



## Creating a Token

Creating a Token in MoveToData is a simple process.

- Navigate to the Settings page and go to the Token tab
- On the top right of the page, select New Token
- Enter details of the Token
- Select Create
- Copy your Token to clipboard

<div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/security/token/create-Token.mp4" width="100%" />
</div>

[//]: # ([comment]: <img src="../_media/Security/token/Token_AdobeExpress.gif" width="2000" height="1250">)
