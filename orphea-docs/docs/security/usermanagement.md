import ReactPlayer from "react-player"

# User Management

Orphea allows administrators to manage users in Orphea on one page.

![Users Splash](../_media/docs_ss/Security/token/userPageContent.png)

In this page located under Orphea settings, it is simple to quickly see all users in the Orphea environment and key details such as:

- Username
- Email
- Last Login
- Plus, many more

## Creating Users

Creating a user in Orphea is a simple process.

- Navigate to the Settings page and go to the User tab
- On the top right of the page, select New User
- Enter details of the user
- Select Create

<div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/security/create-User.mp4" width="100%" />
</div>

## Finding more detail about User

Hovering over a user name in the User tab will pop up a box showing details. Here you can see which level of which group this user is in. For example, the Project administrator down below is in the Test Repository Members of the Test Group.

![u3](../_media/docs_ss/Security/userMoreDetails.png)

## Editing Users

Users can edit their own profile at any time by selecting their personal user profile in the User tab. This will update their profile with their new details.

![Edit User](../_media/docs_ss/Security/updateUser.png)
