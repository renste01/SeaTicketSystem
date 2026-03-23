package dk.easv.seaticketsystem.Model;
// Bruges til at bestemme hvilken rolle en bruger har
public enum UserRole
{
    USER("Bruger"),
    COORDINATOR("Eventkoordinator"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {this.displayName = displayName;}

    public String getDisplayName() {return displayName;}
}
