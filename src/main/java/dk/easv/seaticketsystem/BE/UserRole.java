package dk.easv.seaticketsystem.BE;
// Decides which user
public enum UserRole
{
    USER("Bruger"),
    COORDINATOR("Eventkoordinator"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {this.displayName = displayName;}

    public String getDisplayName() {return displayName;}
}

