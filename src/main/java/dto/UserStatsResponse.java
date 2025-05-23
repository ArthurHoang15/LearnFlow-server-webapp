package dto;

public class UserStatsResponse {
    private final int totalUsers;
    private final int activeUsers;

    public UserStatsResponse(int totalUsers, int activeUsers) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public int getActiveUsers() {
        return activeUsers;
    }
}
