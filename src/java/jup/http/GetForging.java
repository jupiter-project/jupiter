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

package jup.http;

import static jup.http.JSONResponses.NOT_FORGING;
import static jup.http.JSONResponses.UNKNOWN_ACCOUNT;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import jup.Account;
import jup.Generator;
import jup.Jup;
import jup.crypto.Crypto;


public final class GetForging extends APIServlet.APIRequestHandler {

    static final GetForging instance = new GetForging();

    private GetForging() {
        super(new APITag[] {APITag.FORGING}, "secretPhrase", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        int elapsedTime = Jup.getEpochTime() - Jup.getBlockchain().getLastBlock().getTimestamp();
        if (secretPhrase != null) {
            Account account = Account.getAccount(Crypto.getPublicKey(secretPhrase));
            if (account == null) {
                return UNKNOWN_ACCOUNT;
            }
            Generator generator = Generator.getGenerator(secretPhrase);
            if (generator == null) {
                return NOT_FORGING;
            }
            return JSONData.generator(generator, elapsedTime);
        } else {
            API.verifyPassword(req);
            JSONObject response = new JSONObject();
            JSONArray generators = new JSONArray();
            Generator.getSortedForgers().forEach(generator -> generators.add(JSONData.generator(generator, elapsedTime)));
            response.put("generators", generators);
            return response;
        }
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireFullClient() {
        return true;
    }

}
