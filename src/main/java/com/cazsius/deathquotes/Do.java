package com.cazsius.deathquotes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andre L Noel
 * @version 2016-11-20
 *
 */

public class Do {

    public Do() {

    }

    // General Minecraft Modding Programming Tools

    /**Returns a string of all the data in the specified file. It is intended to conveniently load any text file into a string.
     * @param filePathAndName
     * @return String
     */
    public static String FileToString( String filePathAndName ) {
        Reader istream;
        try {
            istream = new FileReader(filePathAndName);
        } catch (FileNotFoundException e) {
            Err("FILE NOT FOUND for file " + filePathAndName);
            e.printStackTrace();
            return "";
        }
        StringBuffer sbuffer = new StringBuffer( );
        char[] b = new char[128];
        int n;
        try {
            while ((n = istream.read(b)) > 0) { sbuffer.append(b, 0, n); }
        } catch (IOException e) {
            Err("Could not read from file " + filePathAndName);
            e.printStackTrace();
        }
        try {
            istream.close();
        } catch (IOException e) {
            Err("Could not close the read stream for file " + filePathAndName);
            e.printStackTrace();
        }
        return sbuffer.toString();
    }

    /**Returns true if it successfully writes the file to storage with the string data.
     * @param filePathAndName
     * @param filedata
     * @return boolean true if successful
     */
    public static boolean StringToFile( String filePathAndName, String filedata ) {
        Pattern pattern = Pattern.compile("^(.*)/");
        Matcher matcher = pattern.matcher(filePathAndName);
        if ( matcher.find() ) { folderMake( matcher.group(1) ); } // create folder path if it doesn't exist
        File fh = new File(filePathAndName);
        if ( ! fh.exists() ) {
            try {
                fh.createNewFile();
            } catch (IOException e) {
                Err("Could not create file " + filePathAndName );
                e.printStackTrace();
                return false;
            }
        }
        OutputStream ostream;
        try {
            ostream = new FileOutputStream(filePathAndName);
        } catch (FileNotFoundException e1) {
            Err("Could not open file output stream for " + filePathAndName);
            e1.printStackTrace();
            return false;
        }
        // put filedata to the ostream here
        byte[] data = filedata.getBytes( );
        try {
            ostream.write(data, 0, data.length);
        } catch (IOException e1) {
            Err("Could not write data to file " + filePathAndName);
            e1.printStackTrace();
            try {
                ostream.close();
            } catch (IOException e) {
                Err("Could not close output stream for file " + filePathAndName + " after the above write error.");
                e.printStackTrace();
            }
            return false;
        }
        try {
            ostream.close();
        } catch (IOException e) {
            Err("Could not close output stream for file " + filePathAndName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**Returns true on success moving a folder and its contents to another folder location.
     * If it fails part way through it will stop with the folder partially copied.
     * It merges into the destination folder if it already exists.
     * It assumes reasonably small to medium file sizes at this time,
     * but with large amounts of ram it may be fine with large files.
     * If you need perfect robustness, write another one. lol.
     * Excellent for what it was made for.
     * @param fromPath
     * @param toPath
     * @return boolean true on success
     */
    public static boolean moveFolder(String fromPath, String toPath) {
        File fromPathFile = new File(fromPath);
        String[] fileNameList = fromPathFile.list();
        for (int i=0; i < fileNameList.length; i++) {
            String fromPathPlus = fromPath +"/"+ fileNameList[i];
            String toPathPlus = toPath +"/"+ fileNameList[i];
            File fromFile = new File(fromPathPlus);
            File toFile = new File(toPathPlus);
            if ( fromFile.isDirectory() ) { // is folder
                toFile.mkdirs();
                if ( moveFolder(fromPathPlus,toPathPlus) ) {
                    fromFile.delete(); // the boolean value the .delete() returns is actually wrong. So no real error checking is possible using the return value.
                } else { Do.Err("moveFolder("+fromPathPlus+", "+toPathPlus+") Failed."); return false; }
            } else { // is file
                String fileContent = Do.FileToString(fromPathPlus);
                if ( fileContent.length() == fromFile.length() ) {
                    if ( Do.StringToFile(toPathPlus,fileContent) ) { // ASSUMES fairly small file sizes since the whole file content is read into RAM instead of in parts.
                        fromFile.delete();
                    } else { Do.Err("moveFolder("+fromPathPlus+", "+toPathPlus+") Failed to write destination file."); return false; }
                } else { Do.Err("moveFolder("+fromPathPlus+", "+toPathPlus+") Failed to read source file."); return false;}
            } // end is folder or is file
        } // end for i fileList
        fromPathFile.delete();
        return true;
    }

    /**
     * Outputs a string with mod name and "TRACE" in the console. Also it outputs the same text to the player chat, if the player is not null.
     * @param player
     * @param s
     */
    public static void Trace(PlayerEntity player, String s) {
        Say(player,"TRACE [" + DeathQuotes.NAME + "] " + s);
        Trace(s);
    }

    /**
     * Outputs a string with mod name and "TRACE" in the console.
     * @param s
     */
    public static void Trace(String s) {
        System.out.println("TRACE [" + DeathQuotes.ID + "] " + s);
    }

    /**
     * Displays text message in chat to the player, if parameter player is not null.
     * @param player
     * @param theMessage
     */
    public static void Say(PlayerEntity player, String theMessage) {
        if (player != null) {
            player.sendMessage(new StringTextComponent(theMessage)); // mc1.11
            //player.addChatComponentMessage(new TextComponentString(theMessage)); // mc1.9 to mc1.10.2
            //player.addChatComponentMessage(new ChatComponentText(theMessage)); // mc1.7.10 and mc1.7.2 and mc1.8
            //player.addChatMessage(theMessage); // mc1.6.4
        } else { Do.Trace("Do.Say: player is null, message is:"+theMessage); return; }
    }

    public static void Say(String playerName, String theMessage) {
        Do.Say(Do.getPlayerByUsername(DeathQuotes.server, playerName), theMessage);
    }

    /**
     * Displays text message in chat to all players currently in game.
     * @param fromPlayer
     * @param theMessage
     */
    public static void SayToAll(PlayerEntity fromPlayer, String theMessage) {
        String fromPlayerName = fromPlayer.getName().getString();
        String[] playerNames = ((ServerPlayerEntity)fromPlayer).server.getOnlinePlayerNames();
        for(String aPlayerName:playerNames) { Do.Say(aPlayerName,theMessage); }
        // something to look into: ((EntityPlayerMP)player).mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(fromPlayer,theMessage);
    }


    /**
     * Sends the mod name and a string message to the standard error output channel and to player chat if the player is not null.
     * @param player
     * @param s
     */
    public static void Err(PlayerEntity player, String s) {
        Say(player,"ERROR [" + DeathQuotes.ID + "] " + s);
        Err(s);
        return;
    }

    /**
     * Sends the mod name and a string message to the standard error output channel.
     * @param s
     */
    public static void Err(String s) {
        System.err.println("ERROR [" + DeathQuotes.ID + "] " + s);
        return;
    }

    /**
     * Returns true if the player is an op (operator). With a custom exception that if the SpawnCommands misc config setting "allow With Cheats Disabled" is true, then in single player it also returns true, as if the player is an op despite the "no cheats allowed" option at game creation.
     * @param player
     */
   public static boolean IsOp(PlayerEntity player) {
        // test if "cheats enabled: on" and is in single player then consider the player an op
        if ( ((ServerPlayerEntity)player).server.isSinglePlayer() && player.hasPermissionLevel(2)) { return true; }
        // mc 1.89    && MinecraftServer.getServer().getConfigurationManager().canSendCommands(player.getGameProfile()) ) { return true; }

        // actual test for op:
        return hasItemInArrayIgnoreCase(player.getName().getString(), ((ServerPlayerEntity)player).server.getPlayerList().getOppedPlayers().getKeys()); // mc1.9
        //return hasItemInArray(player.getName(),MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames()); // mc1.8 and mc1.8.9
        //return MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()); // mc1.7.10
        //return MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(player.getGameProfile().getName()); // mc 1.7.2 ???
        //return MinecraftServer.getServerConfigurationManager(MinecraftServer.getServer()).isPlayerOpped(player.username); // mc 1.6.4

        /** NOTES: probably useful stuff found on the way here.  lol
         ((ServerPlayerEntity)player).server.getOpPermissionLevel()
         ((ServerPlayerEntity)player).server.getPlayerList().getOppedPlayerNames();
         ((ServerPlayerEntity)player).server.getPlayerList().getOppedPlayers();
         ((ServerPlayerEntity)player).server.getPlayerList().getPlayerByUsername(username);
         ((ServerPlayerEntity)player).server.getPlayerList().getPlayerByUUID(playerUUID);
         ((ServerPlayerEntity)player).server.getPlayerList().getPlayerNBT(player);
         ((ServerPlayerEntity)player).server.getPlayerList().saveAllPlayerData();
         ((ServerPlayerEntity)player).server.getPlayerList().sendMessageToTeamOrAllPlayers(player, message);
         ((ServerPlayerEntity)player).server.isSinglePlayer();
         ((ServerPlayerEntity)player).server.getServer().getAllUsernames();
         ((ServerPlayerEntity)player).server.getServer().getCommandManager();
         ((ServerPlayerEntity)player).server.getServer().getCommandSenderEntity();
         ((ServerPlayerEntity)player).server.getServer().getOpPermissionLevel();
         UUID uuid;
         ((ServerPlayerEntity)player).server.getServer().getPlayerProfileCache().getProfileByUUID(uuid).getProperties();
         /**/

    }

    /**Returns true if it exists and it is a folder.
     * @param filePathAndName
     * @return boolean
     */
    public static boolean folderExists(String filePathAndName) {
        File fh = new File(filePathAndName);
        if (fh.exists() && fh.isDirectory()) { return true; }
        return false;
    }

    /**Returns true if it exists and it is a data file, not a folder.
     * @param filePathAndName
     * @return boolean
     */
    public static boolean fileExists(String filePathAndName) {
        File fh = new File(filePathAndName);
        if (fh.exists() && !fh.isDirectory()) { return true; }
        return false;
    }

    /**
     * Attempts to delete the file.  Returns true if successful.
     * @param filePathAndName
     */
    public static boolean fileDelete(String filePathAndName) {
        File fh = new File(filePathAndName);
        return fh.delete();
    }

    /**Returns the numeric long of the date the file was last modified.
     * @param filePathAndName
     * @return
     */
    public static long fileDateModified(String filePathAndName) {
        File fh = new File(filePathAndName);
        return fh.lastModified();
    }

    /**
     * Attempts to create all folders specified in path, if they do not exist.
     * @param filePath
     */
    public static boolean folderMake (String filePath) {
        File fh = new File(filePath);
        return fh.mkdirs();
    }

    // Configuration Array system: String[n][2] contains variable names and values.  All are string.  Names are case insensitive and may include spaces between other displayable characters.
    // In the future this should not use arrays.  This should use something like a dynamic list.

    /** Returns true if the value of the variable in the configuration array equalsIgnoreCase any of these
     * "true","t","1","1.0","yes","y","on","enable","enabled","allow","allowed","correct".
     * List is arbitrary. Source code may be edited if needed.
     * Returns false under any other circumstance.
     * @param configArray
     * @param varName
     * @return boolean
     */
    public static boolean getConfigArrayValueBoolean( String[][] configArray, String varName) {
        return getBoolean(getConfigArrayValue( configArray, varName ));
    }

    /** Returns an integer value for a variable in a configuration array.
     * @param configArray
     * @param varName
     * @return
     */
    public static int getConfigArrayValueInteger( String[][] configArray, String varName) {
        try { return Integer.parseInt( getConfigArrayValue(configArray,varName) ); }
        catch (NumberFormatException e) { return 0; }
    }

    /** Returns an double value for a variable in a configuration array.
     * @param configArray
     * @param varName
     * @return
     */
    public static double getConfigArrayValueDouble( String[][] configArray, String varName) {
        try { return Double.parseDouble( getConfigArrayValue(configArray,varName) ); }
        catch (NumberFormatException e) { return 0; }
    }

    /** Returns an float value for a variable in a configuration array.
     * @param configArray
     * @param varName
     * @return
     */
    public static float getConfigArrayValueFloat( String[][] configArray, String varName) {
        try { return Float.parseFloat( getConfigArrayValue(configArray,varName) ); }
        catch (NumberFormatException e) { return 0; }
    }

    /**Returns a string of the value for variable in a configuration array.
     * @param configArray
     * @param varName
     * @return string
     */
    public static String getConfigArrayValue( String[][] configArray, String varName) {
        for(int i = (configArray.length - 1); i >= 0; i--) { // if there are duplicate entries this takes the value of the last one
            if ( configArray[i][0].equalsIgnoreCase(varName) ) { return configArray[i][1]; }
        }
        configArray = null;
        return "";
    }

    public static String[][] setConfigArrayValue( String[][] configArray, String varName, double varValue ) {
        return setConfigArrayValue(configArray,varName,Double.toString(varValue));
    }

    public static String[][] setConfigArrayValue( String[][] configArray, String varName, int varValue ) {
        return setConfigArrayValue(configArray,varName,Integer.toString(varValue));
    }

    public static String[][] setConfigArrayValue( String[][] configArray, String varName, boolean varValue ) {
        return setConfigArrayValue(configArray,varName,Boolean.toString(varValue));
    }

    /** Returns a configuration array with a changed value for a config variable name. If the variable name is not in the configuration passed to this, it will add it and the new value. This will return a configuration array with the change. It does not change the original array.
     * Suggestion: use this by assigning your config array to this function. Example: myConfigArr = setConfigArrayValue( myConfigArr, "enableMyFeature", "true" );
     * @param configArray
     * @param varName
     * @param varValue
     * @return a new updated configArray
     */
    public static String[][] setConfigArrayValue( String[][] configArray, String varName, String varValue ) {
        String[][] outputArray = null;
        boolean foundVarName = false;
        for(int i = 0; i < configArray.length; i++) {
            if ( configArray[i][0].equalsIgnoreCase(varName) ) {
                configArray[i][1] = varValue;
                foundVarName = true;
            } // allow to loop through all in case there are duplicate variable names
        }
        if (foundVarName) {
            return configArray;
        } else {
            outputArray = new String[configArray.length + 1][2]; // the output array is now a different size than the original
            outputArray[configArray.length][0] = varName; // add the new variable on the end of the array
            outputArray[configArray.length][1] = varValue;
            for(int i = 0; i < configArray.length; i++) {
                outputArray[i][0] = configArray[i][0]; // include all the original variables
                outputArray[i][1] = configArray[i][1];
            }
            configArray = null;
            return outputArray;
        }

    }

    /**Returns a changed config array without the config variable name specified.
     * Suggestion: use this by assigning your config array to this function. Example: myConfigArr = setConfigArrayValue( myConfigArr, "variableNameToRemove" );
     * @param configArray
     * @param varName
     * @return a new updated configArray
     */
    public static String[][] removeConfigArrayVariable( String[][] configArray, String varName ) {
        String[][] outputArray = null;
        int foundVarName = 0;
        for(int i = 0; i < configArray.length; i++) {
            if ( configArray[i][0].equalsIgnoreCase(varName) ) { // case insensitive variable names
                foundVarName++;
            } // allow to loop through all in case there are duplicate variable names
        }
        if (foundVarName == 0) { // variable name not found, no change needed
            return configArray;
        } else {
            outputArray = new String[configArray.length - foundVarName][2]; // the output array is now a different size than the original
            int outputArrayIndex = 0;
            for(int i = 0; i < configArray.length; i++) {
                if (! configArray[i][0].equalsIgnoreCase(varName) ) { // include all the other original variables
                    outputArray[outputArrayIndex  ][0] = configArray[i][0];
                    outputArray[outputArrayIndex++][1] = configArray[i][1];
                }
            }
            configArray = null;
            return outputArray;
        }
    }

    /** Reads a configuration file containing pairs of variable names and values into a 2 dimensional string array. Comment lines start with // and are ignored. Example line in file: NumberOfElephants=5
     * @param filename
     * @return String[n][2] a string array of configuration variable names and values
     */
    public static String[][] getConfigArrayFile(String filename) {
        String[][] tempDataArray = null;
        String[][] finalDataArray = null;
        String varName = null;
        String varValue = null;
        int usableArrayLines = 0;
        String[] rawDataLines = FileToString(filename).replaceAll("\r","\n").split("\n"); // load data. fix windows return vs newline.
        tempDataArray = new String[rawDataLines.length][2];
        // clean and count usable lines
        for(int rawDataLine = 0; rawDataLine < rawDataLines.length; rawDataLine++) {
            // clean the config file line
            rawDataLines[rawDataLine] = rawDataLines[rawDataLine].replaceAll("\t"," "); // change tabs to spaces
            rawDataLines[rawDataLine] = rawDataLines[rawDataLine].replaceAll("^\\s+",""); // remove white space at beginning of line
            rawDataLines[rawDataLine] = rawDataLines[rawDataLine].replaceFirst("^//.*","");// remove comments.  they start with //
            rawDataLines[rawDataLine] = rawDataLines[rawDataLine].replaceAll("\\s+$",""); // remove white space at end of line
            rawDataLines[rawDataLine] = rawDataLines[rawDataLine].replaceFirst("\\s*=\\s*","=");// remove white space around first equal sign (=)
            if ( rawDataLines[rawDataLine].equals("") ) { continue; }
            // parse config file data line
            boolean hasNoVarNameAndEquals = true;
            if ( ! rawDataLines[rawDataLine].startsWith("=") ) {
                for(int i = 0; i < rawDataLines[rawDataLine].length(); i++){
                    if ( rawDataLines[rawDataLine].substring(i, i+1).equals("=")) {
                        varName  = rawDataLines[rawDataLine].substring(0,i).toLowerCase();// the toLowerCase helps makes variable names case insensitive
                        varValue = rawDataLines[rawDataLine].substring(i + 1);
                        hasNoVarNameAndEquals = false;
                        i = rawDataLines[rawDataLine].length(); // end the i loop
                    }
                }
            } // end if not starts with "="
            // store and count the data
            if ( hasNoVarNameAndEquals ) { continue; }
            tempDataArray[usableArrayLines][0]=varName;
            tempDataArray[usableArrayLines++][1]=varValue;
        } // end for raw lines
        finalDataArray =  new String[usableArrayLines][2]; // create an array of the correct size
        for(int i = 0; i < usableArrayLines; i++) {
            for(int j = 0; j < 2; j++) {
                finalDataArray[i][j] = tempDataArray[i][j]; // put into the array of the correct size
            }
        }
        tempDataArray = null;
        return finalDataArray;
    }

    /**
     * Writes dataArray to a configuration file.  The array should be composed of variable name and value pairs. Example: dataArray[0][0]="NumberOfElephants"; dataArray[0][1]="5"; dataArray[1][0]="canElephantsFly"; dataArray[1][1]="no";
     * @param fileName
     * @param dataArray
     * @return true if successful
     */
    public static boolean putConfigArrayFile(String fileName, String[][] dataArray) {
        String fileData = "";
        for(int i = 0; i < dataArray.length; i++)
        { fileData += dataArray[i][0] +"="+ dataArray[i][1] + "\n"; }
        return Do.StringToFile(fileName, fileData);
    }


    /**Returns true if the string is any one of a set of strings meaning true.  Such as "true","t","1","1.0","1.","yes","y","on","enable","enabled","allow","allowed","correct".  Otherwise it returns false.
     * @param stringValue
     * @return boolean
     */
    public static boolean getBoolean( String stringValue ) {
        stringValue = stringValue.replaceAll("^\\s+",""); // remove leading white space
        stringValue = stringValue.replaceAll("\\s+$",""); // remove trailing white space
        final String[] trues = { "true","t","1","1.0","1.","yes","y","on","ok","enable","enabled","allow","allowed","correct" }; // arbitrary and may be edited
        for(int i = 0; i <  trues.length; i++) {
            if (stringValue.equalsIgnoreCase(trues[i])) { stringValue = null; return true; }
        }
        stringValue = null; // clear from memory in case the content is large.
        return false;
    }

    /**Returns false if number is zero else returns true
     * @param number
     * @return boolean
     */
    public static boolean getBoolean( int number ) {
        return ( number == 0 ? false : true );
    }

    /** Returns the string decoded to its original from %hh and "+" encoding. See also escape(string).
     * @param string
     * @return
     */
    public static String unEscape( String string ) {
        try { return java.net.URLDecoder.decode(string, "ISO-8859-1"); }
        catch (UnsupportedEncodingException e) { return string; }
    }

    /** Returns the string with all non-standard bytes encoded in hex with prefixed escape character "%" with the exception that spaces are encoded as "+". See also unEscape(string).
     * @param string
     * @return
     */
    public static String escape( String string ) {
        try { return java.net.URLEncoder.encode(string, "ISO-8859-1"); }
        catch (UnsupportedEncodingException e) { return string; }
    }

    /**
     * Returns true if the item is in the array.
     * @param theItem
     * @param theArray
     * @return
     */
    public static Boolean hasItemInArray(String theItem, String[] theArray) {
        for (int i=0;i < theArray.length; i++) {
            if ( theArray[i].equals(theItem) ) { return true; }
        }
        return false;
    }

    /**
     * Returns true if the item is in the array comparing ignoring the capitalization.
     * @param theItem
     * @param theArray
     * @return
     */
    public static Boolean hasItemInArrayIgnoreCase(String theItem, String[] theArray) { // case insensitive
        for (int i=0;i < theArray.length; i++) {
            if ( theArray[i].equalsIgnoreCase(theItem) ) { return true; }
        }
        return false;
    }

    /**
     * Returns string corrected to the capitalization in the array if the item is in the array. Returns null if no match is found.
     * @param theItem
     * @param theArray
     * @return
     */
    public static String correctFromItemInArrayForCase(String theItem, String[] theArray) { // case insensitive
        for (int i=0;i < theArray.length; i++) {
            if ( theArray[i].equalsIgnoreCase(theItem) ) { return theArray[i]; }
        }
        return null;
    }

    /**Returns an PlayerEntity with specified userName or null if not found.
     * @param server
     * @param userName
     * @return
     */
    public static PlayerEntity getPlayerByUsername(MinecraftServer server, String userName) {
        return server.getPlayerList().getPlayerByUsername(userName); // mc1.9 TEST THIS

        /**  mc1.8.9
         for (int i = 0;i < server.getConfigurationManager().playerEntityList.size();i++) {
         thePlayer = (PlayerEntity)(server.getConfigurationManager().playerEntityList.get(i));
         //if ( thePlayer.getGameProfile().getName().equalsIgnoreCase(userName)) { return thePlayer; } // mc1.7.10
         if ( thePlayer.getName().equalsIgnoreCase(userName)) { return thePlayer; } // mc1.8
         }
         return null;
         /**/
    }

}
