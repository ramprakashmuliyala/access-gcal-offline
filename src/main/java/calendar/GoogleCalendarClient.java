package calendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GoogleCalendarClient {

    private static final String APPLICATION_NAME = "Google Calendar API OAuth PoC";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String clientId = "YOUR_CLIENT_ID";
    private static final String clientSecret = "YOUR_CLIENT_SECRET";


    public static void main(String... args) throws Throwable {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Your email address will be used to run the test. Enter your email address: ");
        String userEmailId = scanner.nextLine();
        System.out.println("You entered: " + userEmailId);

        System.out.println("Enter the refresh token: ");
        String refreshToken = scanner.nextLine();
        System.out.println("You entered: " + refreshToken);

        scanner.close();

        //Create and Delete an event on private/individual's calendar
        createAndDeleteEventOnPrivateCalendar(userEmailId, refreshToken);
    }


    public static Calendar getCalendar(String calendarId, String refreshToken) throws Throwable {

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(getAccessToken(refreshToken));

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Credentials getAccessToken(String refreshToken) throws Throwable {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(HTTP_TRANSPORT, JSON_FACTORY, refreshToken, clientId, clientSecret).execute();
        System.out.println("Got New Access Token: " + tokenResponse.getAccessToken());
        return new GoogleCredentials(new AccessToken(tokenResponse.getAccessToken(), null));
    }


    private static void createAndDeleteEventOnPrivateCalendar(String userEmailId, String refreshToken) throws Throwable {

        System.out.println("\nTEST STARTED FOR PERSONAL CALENDAR");

        Calendar privateCalendarService = getCalendar(userEmailId, refreshToken);

        //Create events in private and group calendars
        Event event = new Event()
                .setSummary("Google Calendar API Java PoC: Test Event Creation")
                .setLocation("Test Location")
                .setDescription("Google Calendar API Java PoC: Test Event Creation");

        EventAttendee[] attendees = new EventAttendee[]{
                new EventAttendee().setEmail(userEmailId)
        };
        event.setAttendees(Arrays.asList(attendees));

        LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);

        DateTime startDateTime = new DateTime(tomorrow + "T08:00:00-04:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/New_York");
        event.setStart(start);

        DateTime endDateTime = new DateTime(tomorrow + "T09:00:00-04:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/New_York");
        event.setEnd(end);

        event = privateCalendarService.events().insert(userEmailId, event).execute();
        String eventId = event.getId();
        System.out.printf("Event created: %s\n", event.getHtmlLink());

        System.out.println("Printing events from Calender after adding an event...");
        //Read events from private calendar
        readFromPrivateCalendar(userEmailId, refreshToken);

        // Delete the event
        privateCalendarService.events().delete(userEmailId, eventId).execute();

        System.out.println("Printing events from Calender after deleting the previously added event...");
        //Read events from private calendar
        readFromPrivateCalendar(userEmailId, refreshToken);

        System.out.println("TEST ENDED FOR PERSONAL CALENDAR\n");
    }

    private static void readFromPrivateCalendar(String calenderId, String refreshToken) throws Throwable {
        /*
         * Using google calendar API to get events from a user's calendar
         * 	1. Use the email address for the user which is also their calendar ID to create a connection
         * 	2. Get events for the user's email address
         */
        System.out.println("---------------------------------------------------------------------------------------------------");

        Calendar privateCalendarService = getCalendar(calenderId, refreshToken);

        // List the next 10 events from the personal calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = privateCalendarService.events().list(calenderId)
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.printf("No upcoming events found for private calendar: %s\n", calenderId);
        } else {
            System.out.printf("Upcoming events for private calendar: %s\n", calenderId);
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }

        System.out.println("---------------------------------------------------------------------------------------------------");
    }

}

