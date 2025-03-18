package com.Manxlatic.arbiter.Ranks;

import com.Manxlatic.arbiter.Arbiter;
import com.Manxlatic.arbiter.Managers.ColourManager;

import java.util.ArrayList;

public enum StaffRanks {

    OWNER(ColourManager.replaceFormat("&#980000O&#B50000w&#D20000n&#E90808e&#FF1010r"), "Owner",0),

    SRMANAGER(ColourManager.replaceFormat("&#A83D16S&#B24416r&#BB4B16. &#C55316M&#CF5A16a&#D86116n&#E26816a&#EC7016g&#F57716e&#FF7E16r"), "SrManager", 1),

    MANAGER(ColourManager.replaceFormat("&#2D5331M&#296830a&#257D2En&#22922Da&#1EA62Cg&#1ABB2Ae&#16D029r"), "Manager", 2),

    DEVELOPER(ColourManager.replaceFormat("&#459296D&#469DA2e&#46A8ADv&#47B3B9e&#48BFC5l&#48CAD0o&#49D5DCp&#49E0E7e&#4AEBF3r"), "Develeoper",3),

    SRADMIN(ColourManager.replaceFormat("&#749741S&#79A638r&#7EB52E. &#83C425A&#88D21Cd&#8DE113m&#92F009i&#97FF00n"), "SrAdmin",4),

    ADMIN(ColourManager.replaceFormat("&#FF1F00A&#FF2314d&#FF2727m&#FF3535i&#FF4343n"), "Admin",5),

    SRMODERATOR(ColourManager.replaceFormat("&#11FFDDS&#0FF6E0r&#0EECE3. &#0CE3E6M&#0BDAE9o&#09D0ECd&#08C7F0e&#06BDF3r&#05B4F6a&#03ABF9t&#02A1FCo&#0098FFr"), "SrModerator", 6),

    MODERATOR(ColourManager.replaceFormat("&#9400FFM&#8A00FFo&#7F00FFd&#7500FFe&#6B00FFr&#6000FFa&#5600FFt&#4B00FFo&#4100FFr"), "Moderator",7),

    TRIALMOD(ColourManager.replaceFormat("&#4F00E5T&#4403D6r&#3906C6i&#2E09B7a&#230BA8l &#180E99M&#0D1189o&#02147Ad"), "TrialMod",8),

    PLAYER(".", "Player", 16);


    private final String display;
    private final String rankName;
    private final int weight;

    private static Arbiter arbiter;

    StaffRanks(String display, String rankName, int weight) {
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

    public ArrayList<String> getStaffPermissions() {
        if (arbiter == null) {
            throw new IllegalStateException("PermsRanks main instance is not set");
        }
        return arbiter.getDbManager().GetPermissions(rankName);
    }
    /*static {
        SRMODERATOR.perms = concatArrays(MODERATOR.perms, new String[]{});
        MODERATOR.perms = TRIALMOD.perms;
        TRIALMOD.perms = PLAYER.perms;
    }*/

    /*private String display;
    private String[] perms;

    private int weight;




    StaffRanks(String display, String[] perms, int weight) {
        this.display = display;
        this.perms = perms;
    }


    static {
        Yaml yaml = new Yaml();
        try {
            // Load the contents of perms.yml into a Map
            FileInputStream inputStream = new FileInputStream("perms.yml");
            Map<String, String[]> data = yaml.load(inputStream);

            // Assign permissions to enum constants
            for (StaffRanks rank : values()) {
                if (data.containsKey(rank.name())) {
                    rank.perms = concatArrays(rank.perms, data.get(rank.name()));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Concatenates two arrays
    private static String[] concatArrays(String[] array1, String[] array2) {
        if (array1 == null) {
            return array2;
        }
        if (array2 == null) {
            return array1;
        }
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }


    public String getDisplay() { return display; }

    public String[] getPerms() { return perms; }

    public int getWeight() { return weight; }
*/

}
