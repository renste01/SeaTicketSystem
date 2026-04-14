package dk.easv.seaticketsystem.Model;

public class UserSearchModel {

    private String query = "";

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query == null ? "" : query.trim(); }
}

