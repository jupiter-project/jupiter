/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

import nxt.crypto.Crypto;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Genesis {

    private static final JSONObject genesisParameters;
    static {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("genesis.json")) {
            genesisParameters = (JSONObject)JSONValue.parseWithException(new InputStreamReader(is));
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Failed to load genesis parameters", e);
        }
    }

    public static final byte[] CREATOR_PUBLIC_KEY = Convert.parseHexString((String)genesisParameters.get("genesisPublicKey"));
    public static final long CREATOR_ID = Account.getId(CREATOR_PUBLIC_KEY);
    public static final byte[] GENESIS_BLOCK_SIGNATURE = Convert.parseHexString((String)genesisParameters.get(Constants.isTestnet
            ? "genesisTestnetBlockSignature" : "genesisBlockSignature"));

    public static final long[] GENESIS_RECIPIENTS;
    public static final byte[][] GENESIS_PUBLIC_KEYS;
    public static final byte[][] GENESIS_SIGNATURES;
    static {
        JSONArray recipientPublicKeys = (JSONArray)genesisParameters.get("genesisRecipientPublicKeys");
        JSONArray genesisSignatures = (JSONArray)genesisParameters.get("genesisSignatures");
        GENESIS_RECIPIENTS = new long[recipientPublicKeys.size()];
        GENESIS_PUBLIC_KEYS = new byte[GENESIS_RECIPIENTS.length][];
        GENESIS_SIGNATURES = new byte[GENESIS_RECIPIENTS.length][];
        for (int i = 0; i < GENESIS_RECIPIENTS.length; i++) {
            GENESIS_PUBLIC_KEYS[i] = Convert.parseHexString((String)recipientPublicKeys.get(i));
            if (!Crypto.isCanonicalPublicKey(GENESIS_PUBLIC_KEYS[i])) {
                throw new RuntimeException("Invalid genesis recipient public key " + recipientPublicKeys.get(i));
            }
            GENESIS_RECIPIENTS[i] = Account.getId(GENESIS_PUBLIC_KEYS[i]);
            if (GENESIS_RECIPIENTS[i] == 0) {
                throw new RuntimeException("Invalid genesis recipient account " + GENESIS_RECIPIENTS[i]);
            }
            if (genesisSignatures != null) {
                GENESIS_SIGNATURES[i] = Convert.parseHexString((String)genesisSignatures.get(i));
            }
        }
    }

    public static final Map<Long, Long> GENESIS_AMOUNTS;
    static {
        JSONArray amounts = (JSONArray)genesisParameters.get("genesisAmounts");
        if (amounts.size() != GENESIS_RECIPIENTS.length) {
            throw new RuntimeException("Number of genesis amounts does not match number of genesis recipients");
        }
        Map<Long,Long> map = new HashMap<>();
        long total = 0;
        for (int i = 0; i < amounts.size(); i++) {
            long amount = ((Long)amounts.get(i)).longValue();
            if (amount <= 0) {
                throw new RuntimeException("Invalid genesis recipient amount " + amount);
            }
            map.put(GENESIS_RECIPIENTS[i], amount);
            total = Math.addExact(total, amount);
        }
        if (total != Constants.MAX_BALANCE_NXT) {
            throw new RuntimeException("Genesis amount total is " + total + ", must be " + Constants.MAX_BALANCE_NXT);
        }
        GENESIS_AMOUNTS = Collections.unmodifiableMap(map);
    }

    public static final long EPOCH_BEGINNING;
    static {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            EPOCH_BEGINNING = dateFormat.parse((String) genesisParameters.get("epochBeginning")).getTime();
        } catch (java.text.ParseException e) {
            throw new RuntimeException("Invalid epoch beginning " + genesisParameters.get("epochBeginning"));
        }
    }

    private Genesis() {} // never

}
