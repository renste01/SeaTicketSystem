package dk.easv.seaticketsystem.Session;

// Projekt imports
import dk.easv.seaticketsystem.Model.User;

/**
 * Singleton handles user session and navigation
 * Important to identify who is logged in
 */
public class SessionManager {

    private static SessionManager instance;

    private User    currentUser;
    private String  selectedTicketId;
    private String  selectedEventId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(User user)   {this.currentUser = user;}
    public void logout()           {this.currentUser = null; selectedTicketId = null; selectedEventId = null;}

    public User   getCurrentUser()  {return currentUser;}
    public boolean isLoggedIn()     {return currentUser != null;}

    public String getSelectedTicketId()          {return selectedTicketId;}
    public void   setSelectedTicketId(String id) {this.selectedTicketId = id; }

    public String getSelectedEventId()           {return selectedEventId;}
    public void   setSelectedEventId(String id)  {this.selectedEventId = id;}
}
