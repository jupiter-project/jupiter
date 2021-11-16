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

package nxt;

import nxt.db.BasicDb;
import nxt.db.TransactionalDb;

public final class Db {

    public static final String PREFIX = Constants.isTestnet ? "nxt.testDb" : "nxt.db";
    public static final TransactionalDb db = new TransactionalDb(new BasicDb.DbProperties()
            .maxCacheSize(Jup.getIntProperty("nxt.dbCacheKB"))
            .dbUrl(Jup.getStringProperty(PREFIX + "Url"))
            .dbType(Jup.getStringProperty(PREFIX + "Type"))
            .dbDir(Jup.getStringProperty(PREFIX + "Dir"))
            .dbParams(Jup.getStringProperty(PREFIX + "Params"))
            .dbUsername(Jup.getStringProperty(PREFIX + "Username"))
            .dbPassword(Jup.getStringProperty(PREFIX + "Password", null, true))
            .maxConnections(Jup.getIntProperty("nxt.maxDbConnections"))
            .loginTimeout(Jup.getIntProperty("nxt.dbLoginTimeout"))
            .defaultLockTimeout(Jup.getIntProperty("nxt.dbDefaultLockTimeout") * 1000)
            .maxMemoryRows(Jup.getIntProperty("nxt.dbMaxMemoryRows"))
    );

    static void init() {
        db.init(new NxtDbVersion());
    }

    static void shutdown() {
        db.shutdown();
    }

    private Db() {} // never

}
