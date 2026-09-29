package com.dteam0928.sangsae.quest;

import java.util.ArrayList;
import java.util.List;

public class UserQuestRepository {
    private final List<UserQuest> userQuests = new ArrayList<>();
    private long sequence = 1L;

    public UserQuest save(UserQuest userQuest) {
        userQuest.setId(sequence++);
        userQuests.add(userQuest);
        return userQuest;
    }

    public List<UserQuest> findAll() {
        return userQuests;
    }
}
