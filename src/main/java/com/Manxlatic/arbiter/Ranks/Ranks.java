package com.Manxlatic.arbiter.Ranks;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ColourManager;

import java.util.ArrayList;

public enum Ranks {
    // Paid Ranks:
    CHAMPION(ColourManager.replaceFormat("&#E5A700C&#ECB317h&#F4BE2Ea&#FBCA45m&#FBCA45p&#F4BE2Ei&#ECB317o&#E5A700n"), "Champion", 9),
    MASTER(ColourManager.replaceFormat("&#00D464M&#00D464a&#00D464s&#00D464t&#00D464e&#00D464r"), "Master", 10),
    PROFESSOR(ColourManager.replaceFormat("&#B352EEP&#B352EEr&#B352EEo&#B352EEf&#B352EEe&#B352EEs&#B352EEs&#B352EEo&#B352EEr"), "Professor", 11),
    ELITE(ColourManager.replaceFormat("&#127A7AE&#127A7Al&#127A7Ai&#127A7At&#127A7Ae"), "Elite", 12),
    ACE(ColourManager.replaceFormat("&#C2E004A&#C2E004c&#C2E004e"), "Ace", 13),
    INTERMEDIATE(ColourManager.replaceFormat("&#6264A5I&#6264A5n&#6264A5t&#6264A5e&#6264A5r&#6264A5m&#6264A5e&#6264A5d&#6264A5i&#6264A5a&#6264A5t&#6264A5e"), "Intermediate", 14),
    APPRENTICE(ColourManager.replaceFormat("&#8B2D2DA&#8B2D2Dp&#8B2D2Dp&#8B2D2Dr&#8B2D2De&#8B2D2Dn&#8B2D2Dt&#8B2D2Di&#8B2D2Dc&#8B2D2De"), "Apprentice", 15),
    TRAINER(ColourManager.replaceFormat("&#8A8A8AT&#8A8A8Ar&#8A8A8Aa&#8A8A8Ai&#8A8A8An&#8A8A8Ae&#8A8A8Ar"), "TRAINER", 16);

    private final String display;
    private final String rankName;
    private final int weight;

    private static Arbiter arbiter;

    Ranks(String display, String rankName, int weight) {
        this.display = display;
        this.rankName = rankName;
        this.weight = weight;
    }

    public static void setMain(Arbiter arbiterInstance) {
        arbiter = arbiterInstance;
    }


    public String getDisplay() {
        return display;
    }

    public int getWeight() {
        return weight;
    }

    public ArrayList<String> getPermissions() {
        if (arbiter == null) {
            throw new IllegalStateException("PermsRanks main instance is not set");
        }
        return arbiter.getDbManager().GetPermissions(rankName);
    }



}