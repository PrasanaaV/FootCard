package com.example.footcard;
import com.google.gson.annotations.SerializedName;

public class Player {
    @SerializedName("name")
    private String name;

    @SerializedName("position")
    private String position;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("league_image_url")
    private String leagueImageUrl;

    @SerializedName("team_image_url")
    private String teamImageUrl;

    @SerializedName("nationality_image_url")
    private String nationalityImageUrl;

    // Constructors
    public Player() {
    }

    public Player(String name, String position, String imageUrl, String leagueImageUrl, String teamImageUrl, String nationalityImageUrl) {
        this.name = name;
        this.position = position;
        this.imageUrl = imageUrl;
        this.leagueImageUrl = leagueImageUrl;
        this.teamImageUrl = teamImageUrl;
        this.nationalityImageUrl = nationalityImageUrl;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLeagueImageUrl() {
        return leagueImageUrl;
    }

    public String getTeamImageUrl() {
        return teamImageUrl;
    }

    public String getNationalityImageUrl() {
        return nationalityImageUrl;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLeagueImageUrl(String leagueImageUrl) {
        this.leagueImageUrl = leagueImageUrl;
    }

    public void setTeamImageUrl(String teamImageUrl) {
        this.teamImageUrl = teamImageUrl;
    }

    public void setNationalityImageUrl(String nationalityImageUrl) {
        this.nationalityImageUrl = nationalityImageUrl;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", leagueImageUrl='" + leagueImageUrl + '\'' +
                ", teamImageUrl='" + teamImageUrl + '\'' +
                ", nationalityImageUrl='" + nationalityImageUrl + '\'' +
                '}';
    }
}
