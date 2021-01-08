package tech.bugger.global.transfer;

public final class TopUser {

    private String username;

    private int earnedRelevance;

    public TopUser(String username, int earnedRelevance) {
        this.username = username;
        this.earnedRelevance = earnedRelevance;
    }

    public String getUsername() {
        return username;
    }

    public int getEarnedRelevance() {
        return earnedRelevance;
    }

    @Override
    public String toString() {
        return "TopUser{" +
                "username='" + username + '\'' +
                ", earnedRelevance=" + earnedRelevance +
                '}';
    }

}
