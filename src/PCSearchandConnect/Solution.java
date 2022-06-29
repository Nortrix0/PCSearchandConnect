/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                         *
 *   Console Application that will search Active Directory for the specific PC the user    *
 *   is searching for from keywords from the PC name, or the users name, or department.    *
 *   Once PC is found, user can automatically connect to the PC. Application also logs     *
 *   the history of all PCs connected to it. Once disconnected from selected PC, if        *
 *   PC has no description, or lacks information, you can change the description in        *
 *   Active Directory directly from this program as well.                                  *
 *                                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package PCSearchandConnect;
/* * * * * * * * * * * * * * * * * * * * * * * * *
 *                                               *
 *      Active Directory PC Search & Connect     *
 *              By Gabriel Brown                 *
 *              City of Gulfport                 *
 *                                               *
 * * * * * * * * * * * * * * * * * * * * * * * * */
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Solution
{
    public static void main(String[] args) throws IOException
    {
        /* We will print the readme at the top of the program, and separate it well from our ASCII art. */
        readme();
        System.out.println();

        /* Prints ASCII Art of Program Name */
        printTitle();

        /* Here we will begin to initialize all objects & variables that we  *
         * will need to pass to multiple other functions.                    */

        /* Open Powershell session at beginning of program, as it takes a while. */
        try (PowerShell powerShell = PowerShell.openSession())
        {
            /* Initialize Variables in main to be passed to other functions */
            ArrayList<Computer> allPCs = new ArrayList<>();
            ArrayList<Computer> pcHistory = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            /* Initialize our history text file and writer. */
            File historyTxt = new File("PCHistory.txt");
            BufferedWriter histWriter = new BufferedWriter(new FileWriter(historyTxt, true));

            /* This is the while loop that our program will spend 99.99% of its time in. This while loop is always
             * true until the user inputs a value to change that to false, ending the program.  */
            boolean menu = true;
            while (menu)
            {
                String userInput = getInput(reader);
                switch (userInput)
                {
                    /* If getInput returns a blank string, the program ends. */
                    case "":
                        menu = false;
                        break;
                    /* If getInput returns history, we call our printHistory function which will print all PCs
                     * in our pcHistory ArrayList of Computer Objects we have previously connected to. */
                    case "history":
                        printHistory(pcHistory);
                        continue;
                        /* Clear history simply clears our pcHistory array list, then prints it to show that
                         * the history has been erased.  */
                    case "clear history":
                        pcHistory.clear();
                        System.out.println("\tHistory cleared!");
                        printHistory(pcHistory);
                        continue;
                        /* If the user is confused, or would like to read the documentation written on the program,
                         * they can type things like what or help, and the documentation will be printed in console. */
                    case "what":
                        readme();
                        continue;
                        /* Default is for when the user is searching for a PC, and contains the vast majority of the
                         * functionality in this program. First, we search Active Directory and add them to our
                         * arraylist of computer objects, then we print out that arraylist of PCs. After that, we
                         * prompt the user to choose one of the PCs to connect to. If they choose not to connect,
                         * then we go back to the beginning, prompting the user to enter a PC keyword to search.
                         * If the user does select a PC, we add that PC to our history, which includes the PC name,
                         * PC description, and the time that we connected to the PC. That history is both temporarily
                         * stored in our pcHistory arraylist and permanently stored in a text file. Once the user
                         * has finished helping the client, if there is no description on the PC, or if it isn't
                         * descriptive enough, the user can edit the description of the PC previously connected to
                         * before going back to the PC search. */
                    default:
                        searchAD(userInput, allPCs, powerShell);
                        printPCInfo(allPCs);
                        String PCname = selectPC(allPCs, pcHistory, reader);
                        if (!(PCname.isBlank()))
                        {
                            writeHistory(pcHistory, histWriter);
                            connectToPC(PCname, powerShell);
                            changeACDescription(PCname, powerShell, reader);
                        }
                }
            }

        }
        catch (PowerShellNotAvailableException ex)
        {
            System.out.println("Powershell couldn't open!");
            ex.printStackTrace();
        }
    }

    /* getInput is a weird little function that contains all the nasty if statements we use to allow multiple inputs for
     * different functions in our program. By turning all these different ways of phrasing different functions into one
     * simple phrase, we can use a much more clean looking switch statement in our main function. */
    private static String getInput(BufferedReader reader) throws IOException
    {
        System.out.print("\n\tEnter PC keyword: ");
        String userInput = reader.readLine();
        if (userInput.equalsIgnoreCase("quit") || userInput.equalsIgnoreCase("no") || userInput.length() < 2)
            userInput = "";
        if (userInput.equalsIgnoreCase("show history") || userInput.equalsIgnoreCase("print history")
                || userInput.equalsIgnoreCase("history"))
            userInput = "history";
        if (userInput.equalsIgnoreCase("clear history"))
            userInput = "clear history";
        if (userInput.equalsIgnoreCase("show info") || userInput.equalsIgnoreCase("info") ||
                userInput.equalsIgnoreCase("readme") || userInput.equalsIgnoreCase("read me") ||
                userInput.equalsIgnoreCase("print info") || userInput.equalsIgnoreCase("help"))
            userInput = "what";
        return userInput;
    }

    /* searchAD takes in the user input and adds it to a longer string that is a powershell
     * command which will search Active Directory for our PC. This function creates Computer
     * objects and adds them to our arraylist of computer objects. These objects are our
     * search results. */
    private static void searchAD(String userInput, ArrayList<Computer> allPCs, PowerShell powerShell)
    {
        String keyword = "description";
        if (userInput.matches("\\d+") || userInput.matches("\\w\\w\\d+") || userInput.matches("\\w\\w"))
            keyword = "name";

        String nameSearch = "(Get-ADComputer -Filter '" + keyword + " -like \"*" + userInput +
                "*\"' | Select Name | Select-String -Pattern \"\\w+\\d\" -List).Matches.Value";

        String descriptionSearch = "(Get-ADComputer -Filter '" + keyword + " -like \"*" + userInput +
                "*\"' -Properties * | Select Description | Out-String)" +
                ".Replace(\"Description\", \"\").Replace(\"-\",\"\").Replace(\" \", \"\")";

        System.out.print("\tSearching Active Directory.");

        PowerShellResponse nameResponse = powerShell.executeCommand(nameSearch);
        System.out.print(".");
        PowerShellResponse descriptionResponse = powerShell.executeCommand(descriptionSearch);
        System.out.print(".\n");

        Scanner nameOutput = new Scanner(nameResponse.getCommandOutput());
        Scanner descriptionOutput = new Scanner(descriptionResponse.getCommandOutput());

        while (nameOutput.hasNextLine())
        {
            try
            {
                Computer PC = new Computer(nameOutput.nextLine(), descriptionOutput.next());
                allPCs.add(PC);
            }
            catch (NoSuchElementException e)
            {
                System.out.println("\tThis search result is missing due to lack of description.");
            }

        }
    }

    /* printPCInfo simply and very cleanly prints out our search results, our Computer objects stored after
     * our search. */
    private static void printPCInfo(ArrayList<Computer> allPCs)
    {
        System.out.println("\n\n\t   PC Name \t  PC Description");
        System.out.println("\t-----------------------------------------------------------------------");
        int i = 1;
        for (Computer allPC : allPCs)
        {
            if (i < 10)
                System.out.println("\t " + i + ". " + allPC.getPCname() + "\t  " + allPC.getDescription());
            else
                System.out.println("\t" + i + ". " + allPC.getPCname() + "\t  " + allPC.getDescription());

            i++;
        }
        System.out.println("\t-----------------------------------------------------------------------\n");
    }

    /* The selectPC function prompts the user to select a PC from the list to connect to. If there is only one PC,
     * the user can either confirm that they want to connect to it, or decide not to, which will return a blank string
     * This function is also where our selected PC is added to our pcHistory arraylist of Computers, as if we select a
     * PC, we will be connecting to it, and therefore need to store it in our history. */
    private static String selectPC(ArrayList<Computer> allPCs, ArrayList<Computer> pcHistory, BufferedReader reader) throws IOException
    {
        String chosenPC = "";

        if (allPCs.size() > 1)
        {
            System.out.print("\tSelect a PC to connect to: ");
            String input = reader.readLine();

            if (input.matches("\\d"))
            {
                int pcChoice = Integer.parseInt(input) - 1;
                if (pcChoice >= 0 && pcChoice < allPCs.size())
                {
                    Computer PC = new Computer(allPCs.get(pcChoice).getPCname(), allPCs.get(pcChoice).getDescription());
                    pcHistory.add(PC);
                    System.out.print("\t" + allPCs.get(pcChoice).getPCname() + " added to history. ");
                    chosenPC = allPCs.get(pcChoice).getPCname();
                }
            }
        }
        else if (allPCs.size() == 1)
        {
            System.out.print("\tConnect to this PC? ");
            String input = reader.readLine();
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes") || input.equals("1"))
            {
                Computer PC = new Computer(allPCs.get(0).getPCname(), allPCs.get(0).getDescription());
                pcHistory.add(PC);
                System.out.print("\t" + allPCs.get(0).getPCname() + " added to history. ");
                chosenPC = allPCs.get(0).getPCname();
            }
        }
        allPCs.clear();
        return chosenPC;
    }

    /* printHistory is a lot like printPCinfo, except this function can be run any time the user wants to see the
     * history. It is also formatted differently, including the time connected as well. */
    private static void printHistory(ArrayList<Computer> pcHistory)
    {
        System.out.println("\n\tDate & Time\t PC Name \t PC Description");
        System.out.println("\t-----------------------------------------------------------------------");
        for (int i = pcHistory.size() - 1; i >= 0; i--)
        {
            System.out.println("\t" + pcHistory.get(i).getDateNtime() + "\t " + pcHistory.get(i).getPCname() + "\t " + pcHistory.get(i).getDescription());
        }
        System.out.println("\t-----------------------------------------------------------------------");
    }

    /* writeHistory is similar to printHistory. It just writes to a text file, and appends to it,
     * rather than just printing onto the console. This function is important for us to store history
     * more permanently, as our pcHistory arraylist of computers is erased every time we exit the
     * program. */
    private static void writeHistory(ArrayList<Computer> pcHistory, BufferedWriter histWriter) throws IOException
    {
        histWriter.write("\n\n\tDate & Time\t PC Name \t PC Description");
        histWriter.write("\n\t-----------------------------------------------------------------------");
        histWriter.write("\n\t" + pcHistory.get(pcHistory.size()-1).getDateNtime() + "\t " +
                pcHistory.get(pcHistory.size()-1).getPCname() + "\t " + pcHistory.get(pcHistory.size()-1).getDescription());
        histWriter.write("\n\t-----------------------------------------------------------------------");
        histWriter.close();
    }

    /* Very simple and short function that executes a powershell command using the PCname passed by our
     * select PC function. The powershell command connects us to our intended PC through Dameware. */
    private static void connectToPC(String PCname, PowerShell powerShell)
    {
        if (!(PCname.isBlank()))
        {
            System.out.print("Connecting now..");
            String connectCmd = "& \"C:\\Program Files (x86)\\SolarWinds\\DameWare Remote Support\\dwrcc.exe\" -c: -h: -m:{" + PCname + "} -a:1 -x";
            powerShell.executeCommand(connectCmd);
            System.out.print(".\n");
        }
    }

    /* This function allows the user to edit the description of a PC that has not been updated, or was never added.
     * In theory, this should allow the user to slowly update and correct any PCs that may need to be connected to
     * frequently, but do not have information like the name of that PCs user, or what the jobTitle of the user is. */
    private static void changeACDescription(String PCname, PowerShell powerShell, BufferedReader reader) throws IOException
    {
        if (!(PCname.isBlank()))
        {
            System.out.print("\n\tChange PC Description? ");
            String input = reader.readLine();
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes") ||
                    input.equalsIgnoreCase("1"))
            {
                System.out.print("\tEnter user's full name: ");
                String fullName = reader.readLine();
                System.out.print("\tEnter user's job title: ");
                String jobTitle = reader.readLine();
                if (fullName.isBlank() && jobTitle.isBlank())
                {
                    System.out.println("\tDescription unchanged.");
                    return;
                }
                else if (fullName.isBlank())
                    fullName = "NA";
                else if (jobTitle.isBlank())
                    jobTitle = "Unknown";

                String description = fullName + " | " + jobTitle;
                String setDescriptionCmd = "Set-ADComputer -Identity \"" + PCname +
                        "\" -Description \"" + description + "\"";

                powerShell.executeCommand(setDescriptionCmd);

                System.out.println("\t" + PCname + " description changed to \"" + description + "\"");
            }
        }
    }

    /* Very much self-explanatory */
    private static void printTitle()
    {
        System.out.println("\n");
        System.out.println("\t░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ ▄▀▄     ▄▀▄ ░░░░░░░░░");
        System.out.println("\t░░░░█▀▀░█░█▄▄░█▄▄░█▄█░█▀░░░░░░░░░░░░░░░░░░░ ▄█░░▀▀▀▀▀░░█▄ ░░░░░░░░");
        System.out.println("\t░░░░█▄█░█░█▄█░█▄█░░█░░▄█░░░░░░░░░░░░░░░ ▄▄  █░░░░░░░░░░░█  ▄▄ ░░░░");
        System.out.println("\t░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ █▄▄█ █░░▀░░┬░░▀░░█ █▄▄█ ░░░");
        System.out.println("\t██████╗░░█████╗░░░░██████╗███████╗░█████╗░██████╗░░█████╗░██╗░░██╗");
        System.out.println("\t██╔══██╗██╔══██╗░░██╔════╝██╔════╝██╔══██╗██╔══██╗██╔══██╗██║░░██║");
        System.out.println("\t██████╔╝██║░░╚═╝░░╚█████╗░█████╗░░███████║██████╔╝██║░░╚═╝███████║");
        System.out.println("\t██╔═══╝░██║░░██╗░░░╚═══██╗██╔══╝░░██╔══██║██╔══██╗██║░░██╗██╔══██║");
        System.out.println("\t██║░░░░░╚█████╔╝░░██████╔╝███████╗██║░░██║██║░░██║╚█████╔╝██║░░██║");
        System.out.println("\t╚═╝░░░░░░╚════╝░░░╚═════╝░╚══════╝╚═╝░░╚═╝╚═╝░░╚═╝░╚════╝░╚═╝░░╚═╝");
        System.out.println("\t░░░░░░░░█████╗░░█████╗░███╗░░██╗███╗░░██╗███████╗░█████╗░████████╗");
        System.out.println("\t░░██╗░░██╔══██╗██╔══██╗████╗░██║████╗░██║██╔════╝██╔══██╗╚══██╔══╝");
        System.out.println("\t██████╗██║░░╚═╝██║░░██║██╔██╗██║██╔██╗██║█████╗░░██║░░╚═╝░░░██║░░░");
        System.out.println("\t╚═██╔═╝██║░░██╗██║░░██║██║╚████║██║╚████║██╔══╝░░██║░░██╗░░░██║░░░");
        System.out.println("\t░░╚═╝░░╚█████╔╝╚█████╔╝██║░╚███║██║░╚███║███████╗╚█████╔╝░░░██║░░░");
        System.out.println("\t░▓█▀▀▀▀▀╚════╝░░╚════╝░╚═╝░░╚══╝╚═╝░░╚══╝╚══════╝░╚════╝░░░░╚═╝░░░");
        System.out.println("\t░▓█░░▄░░▄░░░█▓░█████░░█░█░█▀▀░█▀█░█▀░█░█▀█░█▄░█░░░▄█░░░█▀█░█░█░░░░");
        System.out.println("\t░▓█▄▄▄▄▄▄▄▄▄█▓░█▄▄▄█░░▀▄▀░██▄░█▀▄░▄█░█░█▄█░█░▀█░░░░█░▄░█▄█░▀▀█░░░░");
        System.out.println("\t░░░░▄▄███▄▄░░░░█████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░");
        System.out.print("\t");

    }

    /* Simply prints out documentation for the program. Intended to explain
     * what the program does, and how to navigate through it. */
    private static void readme()
    {
        System.out.println();
        System.out.println("\t******************************************************************");
        System.out.println("\t*    -  When prompted to enter a PC keyword, you may enter       *");
        System.out.println("\t*      the user's name, job title, department, or PC name.       *");
        System.out.println("\t*                                                                *");
        System.out.println("\t*    -  This app also stores your history. If you type in        *");
        System.out.println("\t*     \"history\", you will see a list of the PCs you              *");
        System.out.println("\t*      previously connected to, as well as the time you          *");
        System.out.println("\t*      connected. There is also a history log that is in         *");
        System.out.println("\t*      the same directory as the jar file. This text file        *");
        System.out.println("\t*      is more permanent & reliable than the history             *");
        System.out.println("\t*      function you can print in the console, as it writes       *");
        System.out.println("\t*      & appends to a text file, rather than being stored        *");
        System.out.println("\t*      in a list of computers that will be erased once you       *");
        System.out.println("\t*      exit the program. You can also clear the history          *");
        System.out.println("\t*      list (but not the log) with \"clear history\".              *");
        System.out.println("\t*                                                                *");
        System.out.println("\t*    -  The input following that should be intuitive. When       *");
        System.out.println("\t*      prompted to connect to a PC, the number to the left       *");
        System.out.println("\t*      of the PC you would like to connect to would be           *");
        System.out.println("\t*      your input.                                               *");
        System.out.println("\t*                                                                *");
        System.out.println("\t*    -  Yes or no questions can be answered with \"yes\",          *");
        System.out.println("\t*      \"y\", & \"1\", or \"no\", \"n\", & \"0\".                          *");
        System.out.println("\t******************************************************************");
    }
}
