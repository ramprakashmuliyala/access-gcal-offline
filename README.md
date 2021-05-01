# access-gcal-offline
Demo application that shows how to access Google Calendar even when the user is offline using OAuth2 through user consent.

# Description
Application has 2 components:
1. Spring boot application that implements OAuth flow. This provides functionality to signin users through google and prompt a consent page for the user to authorize the application to write to his/her google calendar. This prints out a refresh token on the console.
2. GoogleCalendarClient.java which demonstrates how to use the refresh token to get a new access token from Google and then read/write to the user's google calendar.

# Create OAuth Credentials on Google Cloud Platform
Provide the following inputs:

1. Authorized JavaScript origins: http://localhost:8090
2. Authorized redirect URIs: http://localhost:8090/login/oauth2/code/google

# Update credentials in the demo code
1. Enter the client ID and secret values in application.yaml in the fields "clientId" and "clientSecret" respectively
2. Enter the client ID and secret values in GoogleCalendarClient.java in the static variables "clientId" and 'clientSecret" respectively

# Running the Code
1. Run Application.java to start spring boot
2. Open a browser and navigate to http://localhost:8090
3. Click to sigin with google and authorize access to the calendar
4. Note down the refresh token printed out to the console.
5. Run GoogleCalendarClient and enter your gmail ID and the refresh token 
6. Program will print out the event created on your calendar to the console and then cleanup afterwards. 


