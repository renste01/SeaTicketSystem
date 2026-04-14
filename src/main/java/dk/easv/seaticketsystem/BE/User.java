package dk.easv.seaticketsystem.BE;

public abstract class User {

    private final String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private final UserRole role;
    private String profileImagePath;   // null = Using initials as avatar

    protected User(String id, String firstName, String lastName, String email, String password, UserRole role)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
    // Getters
    public String getId()                       {return id;}
    public String getFirstName()                {return firstName;}
    public String getLastName()                 {return lastName;}
    public String getName() {
        if (lastName == null || lastName.isBlank()) return firstName;
        return firstName + " " + lastName;
    }
    public String getEmail()                    {return email;}
    public String getPassword()                 {return password;}
    public UserRole getRole()                   {return role;}
    public String getProfileImagePath()         {return profileImagePath;}

    // Setters
    public void setFirstName(String fn)         {this.firstName = fn;}
    public void setLastName(String ln)          {this.lastName = ln;}
    public void setEmail(String em)             {this.email = em;}
    public void setPassword(String pw)          {this.password = pw;}
    public void setProfileImagePath(String path){this.profileImagePath = path;}


    public boolean checkPassword(String pw)     {return this.password != null && this.password.equals(pw);}

    @Override public String toString()          {return getName() + " (" + email + ")";}
}

