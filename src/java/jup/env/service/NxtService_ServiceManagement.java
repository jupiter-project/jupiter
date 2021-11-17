/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2017-2020 Sigwo Technologies
 * Copyright © 2020-2021 Jupiter Project Developers
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package jup.env.service;

import javax.swing.JOptionPane;

import jup.Jup;
import jup.env.LookAndFeel;

@SuppressWarnings("UnusedDeclaration")
public class NxtService_ServiceManagement {

    public static boolean serviceInit() {
        LookAndFeel.init();
        new Thread(() -> {
            String[] args = {};
            Jup.main(args);
        }).start();
        return true;
    }

    // Invoked when registering the service
    public static String[] serviceGetInfo() {
        return new String[]{
                Jup.APPLICATION + " Server", // Long name
                "Manages the " + Jup.APPLICATION + " cryptographic currency protocol", // Description
                "true", // IsAutomatic
                "true", // IsAcceptStop
                "", // failure exe
                "", // args failure
                "", // dependencies
                "NONE/NONE/NONE", // ACTION = NONE | REBOOT | RESTART | RUN
                "0/0/0", // ActionDelay in seconds
                "-1", // Reset time in seconds
                "", // Boot Message
                "false" // IsAutomatic Delayed
        };
    }

    public static boolean serviceIsCreate() {
        return JOptionPane.showConfirmDialog(null, "Do you want to install the " + Jup.APPLICATION + " service ?", "Create Service", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean serviceIsLaunch() {
        return true;
    }

    public static boolean serviceIsDelete() {
        return JOptionPane.showConfirmDialog(null, "This " + Jup.APPLICATION + " service is already installed. Do you want to delete it ?", "Delete Service", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static boolean serviceControl_Pause() {
        return false;
    }

    public static boolean serviceControl_Continue() {
        return false;
    }

    public static boolean serviceControl_Stop() {
        return true;
    }

    public static boolean serviceControl_Shutdown() {
        return true;
    }

    public static void serviceFinish() {
        System.exit(0);
    }

}
