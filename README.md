This java application will only work on Windows! This Java application requires Powershell, Active Directory, & Dameware to work correctly.

This application was made specifically for City of Gulfport IT employees in order to streamline the process of helping our co-workers solve any problems with their PC by quickly remoting into the device and troubleshooting for them. Some parts of this program including the regex used to determine if the user is searching by PC name or description is specific to our naming scheme, and overall, this program will more than likely not work as intended for anyone outside of our domain. I may try to make a more agnostic version of this application in the future, if needed.

This java application also uses the JPowershell dependancy to make this possible. Thank you ProfessorFalken!

UPDATE: Currently this program has a bug. If there is no description for one of the PCs in Active Directory in your search results, the application will crash. This bug is currently being worked on & will be fixed in 1.05 


    ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ ▄▀▄     ▄▀▄ ░░░░░░░░░
    ░░░░█▀▀░█░█▄▄░█▄▄░█▄█░█▀░░░░░░░░░░░░░░░░░░░ ▄█░░▀▀▀▀▀░░█▄ ░░░░░░░░
    ░░░░█▄█░█░█▄█░█▄█░░█░░▄█░░░░░░░░░░░░░░░ ▄▄  █░░░░░░░░░░░█  ▄▄ ░░░░
    ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ █▄▄█ █░░▀░░┬░░▀░░█ █▄▄█ ░░░
    ██████╗░░█████╗░░░░██████╗███████╗░█████╗░██████╗░░█████╗░██╗░░██╗
    ██╔══██╗██╔══██╗░░██╔════╝██╔════╝██╔══██╗██╔══██╗██╔══██╗██║░░██║
    ██████╔╝██║░░╚═╝░░╚█████╗░█████╗░░███████║██████╔╝██║░░╚═╝███████║
    ██╔═══╝░██║░░██╗░░░╚═══██╗██╔══╝░░██╔══██║██╔══██╗██║░░██╗██╔══██║
    ██║░░░░░╚█████╔╝░░██████╔╝███████╗██║░░██║██║░░██║╚█████╔╝██║░░██║
    ╚═╝░░░░░░╚════╝░░░╚═════╝░╚══════╝╚═╝░░╚═╝╚═╝░░╚═╝░╚════╝░╚═╝░░╚═╝
    ░░░░░░░░█████╗░░█████╗░███╗░░██╗███╗░░██╗███████╗░█████╗░████████╗
    ░░██╗░░██╔══██╗██╔══██╗████╗░██║████╗░██║██╔════╝██╔══██╗╚══██╔══╝
    ██████╗██║░░╚═╝██║░░██║██╔██╗██║██╔██╗██║█████╗░░██║░░╚═╝░░░██║░░░
    ╚═██╔═╝██║░░██╗██║░░██║██║╚████║██║╚████║██╔══╝░░██║░░██╗░░░██║░░░
    ░░╚═╝░░╚█████╔╝╚█████╔╝██║░╚███║██║░╚███║███████╗╚█████╔╝░░░██║░░░
    ▐▓█▀▀▀▀▀╚════╝▌░╚════╝░╚═╝░░╚══╝╚═╝░░╚══╝╚══════╝░╚════╝░░░░╚═╝░░░
    ▐▓█░░▄░░▄░░░█▓▌█████░░█░█░█▀▀░█▀█░█▀░█░█▀█░█▄░█░░░▄█░░░█▀█░█░█░░░░
    ▐▓█▄▄▄▄▄▄▄▄▄█▓▌█▄▄▄█░░▀▄▀░██▄░█▀▄░▄█░█░█▄█░█░▀█░░░░█░▄░█▄█░▀▀█░░░░
    ░░░░▄▄███▄▄░░░░█████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
    ******************************************************************
    *                                                                *
    *           ACTIVE DIRECTORY PC SEARCH & CONNECT                 *
    *                 Author: Gabriel Brown                          *
    *                                                                *
    *    -  When prompted to enter a PC keyword, you may enter       *
    *      the user's first name, last name, job title, or any       *
    *      part of their PC name, beginning, middle, or end.         *
    *                                                                *
    *    -  This app also has a history feature. If you type         *
    *      in "history" or "show history", you will see a list       *
    *      of the PCs you previously connected to, as well as        *
    *      the date & time when connected. You can also clear        *
    *      the history with "clear history".                         *
    *                                                                *
    *    -  There is also a history log that is created in the       *
    *      same directory as the jar file. This history log          *
    *      will gather your history and save it, even when you       *
    *      close the app and restart it. This history feature        *
    *      is more permanent & reliable than the history             *
    *      function you can print in the console, as it writes       *
    *      & appends to a text file, rather than being stored        *
    *      in an arraylist of computers that will be erased          *
    *      once you exit the program.                                *
    *                                                                *
    *    -  The input following that should be intuitive. When       *
    *      prompted to connect to a PC, the number to the left       *
    *      of PC you would like to connect to would be your          *
    *      input.                                                    *
    *                                                                *
    *    -  Yes or no questions can be answered with "yes",          *
    *      "y", & "1", or "no", "n", & "0".                          *
    *                                                                *
    ******************************************************************
