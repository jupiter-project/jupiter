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

package nxt.env;

import java.nio.file.Paths;

import nxt.Nxt;

public class WindowsUserDirProvider extends DesktopUserDirProvider {

    private static final String NXT_USER_HOME = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", Nxt.APPLICATION.toUpperCase()).toString();

    @Override
    public String getUserHomeDir() {
        return NXT_USER_HOME;
    }
}
