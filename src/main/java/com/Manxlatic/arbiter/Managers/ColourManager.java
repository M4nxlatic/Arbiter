package com.Manxlatic.arbiter.Managers;

import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColourManager {

    public static final Pattern IPPATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Set<ChatColor> COLORS = EnumSet.of(ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY, ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE);
    private static final Set<ChatColor> FORMATS = EnumSet.of(ChatColor.BOLD, ChatColor.STRIKETHROUGH, ChatColor.UNDERLINE, ChatColor.ITALIC, ChatColor.RESET);
    private static final Set<ChatColor> MAGIC = EnumSet.of(ChatColor.MAGIC);
    //Vanilla patterns used to strip existing formats
    private static final Pattern STRIP_ALL_PATTERN = Pattern.compile(ChatColor.COLOR_CHAR + "+([0-9a-fk-orA-FK-OR])");
    //Pattern used to strip md_5 legacy hex hack
    private static final Pattern STRIP_RGB_PATTERN = Pattern.compile(ChatColor.COLOR_CHAR + "x((?:" + ChatColor.COLOR_CHAR + "[0-9a-fA-F]){6})");
    //Essentials '&' convention colour codes
    private static final Pattern REPLACE_ALL_PATTERN = Pattern.compile("(&)?&([0-9a-fk-orA-FK-OR])");
    private static final Pattern REPLACE_ALL_RGB_PATTERN = Pattern.compile("(&)?&#([0-9a-fA-F]{6})");
    //Used to prepare xmpp output
    private static final Pattern LOGCOLOR_PATTERN = Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]");
    private static final Pattern URL_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w-_\\.]{2,})\\.([a-zA-Z]{2,3}(?:/\\S+)?)");
    //Used to strip ANSI control codes from console
    private static final Pattern ANSI_CONTROL_PATTERN = Pattern.compile("[\\x1B\\x9B][\\[\\]()#;?]*(?:(?:(?:;[-a-zA-Z\\d/#&.:=?%@~_]+)*|[a-zA-Z\\d]+(?:;[-a-zA-Z\\d/#&.:=?%@~_]*)*)?\\x07|(?:\\d{1,4}(?:;\\d{0,4})*)?[\\dA-PR-TZcf-nq-uy=><~])");
    private static final Pattern PAPER_CONTROL_PATTERN = Pattern.compile("(?i)" + (char) 0x7f + "[0-9A-FK-ORX]");

    public ColourManager() {
    }

    public static String replaceFormat(final String input) {
        if (input == null) {
            return null;
        }
        return replaceColor(input, EnumSet.allOf(ChatColor.class), true);
    }

    static String replaceColor(final String input, final Set<ChatColor> supported, final boolean rgb) {
        final StringBuffer legacyBuilder = new StringBuffer();
        final Matcher legacyMatcher = REPLACE_ALL_PATTERN.matcher(input);
        legacyLoop:
        while (legacyMatcher.find()) {
            final boolean isEscaped = legacyMatcher.group(1) != null;
            if (!isEscaped) {
                final char code = legacyMatcher.group(2).toLowerCase(Locale.ROOT).charAt(0);
                for (final ChatColor color : supported) {
                    if (color.getChar() == code) {
                        legacyMatcher.appendReplacement(legacyBuilder, ChatColor.COLOR_CHAR + "$2");
                        continue legacyLoop;
                    }
                }
            }
            legacyMatcher.appendReplacement(legacyBuilder, "&$2");
        }
        legacyMatcher.appendTail(legacyBuilder);

        if (rgb) {
            final StringBuffer rgbBuilder = new StringBuffer();
            final Matcher rgbMatcher = REPLACE_ALL_RGB_PATTERN.matcher(legacyBuilder.toString());
            while (rgbMatcher.find()) {
                final boolean isEscaped = rgbMatcher.group(1) != null;
                if (!isEscaped) {
                    try {
                        final String hexCode = rgbMatcher.group(2);
                        rgbMatcher.appendReplacement(rgbBuilder, parseHexColor(hexCode));
                        continue;
                    } catch (final NumberFormatException ignored) {
                    }
                }
                rgbMatcher.appendReplacement(rgbBuilder, "&#$2");
            }
            rgbMatcher.appendTail(rgbBuilder);
            return rgbBuilder.toString();
        }
        return legacyBuilder.toString();
    }

    /**
     * @throws NumberFormatException If the provided hex color code is invalid or if version is lower than 1.16.
     */
    public static String parseHexColor(String hexColor) throws NumberFormatException {

        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1); //fuck you im reassigning this.
        }
        if (hexColor.length() != 6) {
            throw new NumberFormatException("Invalid hex length");
        }

        //noinspection ResultOfMethodCallIgnored
        Color.fromRGB(Integer.decode("#" + hexColor));
        final StringBuilder assembledColorCode = new StringBuilder();
        assembledColorCode.append(ChatColor.COLOR_CHAR + "x");
        for (final char curChar : hexColor.toCharArray()) {
            assembledColorCode.append(ChatColor.COLOR_CHAR).append(curChar);
        }
        return assembledColorCode.toString();
    }
    public static String BuildColorString(String HexColour1, String HexColour2, String Value)
    {
        Color HC1 = hexToColor(HexColour1);
        Color HC2 = hexToColor(HexColour2);
        String Output = "";
        for (int i = 1; i <= Value.length(); i++)
        {
            float ratio = (float) i / Value.length();
            Color interpolatedColour = interpolateColour(HC1,HC2,ratio);

            Output = Output + "&" + colourToHex(interpolatedColour) + Value.substring(i-1, i);


        }
        return replaceFormat(Output);

    }

    private static Color interpolateColour(Color c1, Color c2, float ratio)
    {
        int r = (int) (c1.getRed() + ratio * (c2.getRed()-c1.getRed()));
        int g = (int) (c1.getGreen() + ratio * (c2.getGreen()-c1.getGreen()));
        int b = (int) (c1.getBlue() + ratio * (c2.getBlue()-c1.getBlue()));

        return Color.fromRGB(r,g,b);

    }

    private static Color hexToColor(String ColourHex)
    {
        return Color.fromRGB(
                Integer.valueOf(ColourHex.substring(1,3),16),
                Integer.valueOf(ColourHex.substring(3,5),16),
                Integer.valueOf(ColourHex.substring(5,7),16));
    }
    private static String colourToHex(Color c)
    {
        return String.format("#%02X%02X%02X",c.getRed(),c.getGreen(),c.getBlue());

    }
}

