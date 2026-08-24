
package org.main;

import java.util.*;

public class Console {
    private static final Scanner scanner = new Scanner(System.in);

    public static void Warning(String msg) {
        System.out.printf("\u001B[33mWARNING: %s\u001B[0m%n", msg);
    }

    /**
     * User terminal confirmation
     *
     @deprecated
     */

    private static boolean getUserConfirmation() {
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.length() != 1) {
                System.out.println("(y/n)?");
                continue;
            }

            if (input.charAt(0) == 'y') {
                return true;

            } else if (input.charAt(0) == 'n') {
                return false;
            }
        }
    }

}
