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

package nxt.http;

import static nxt.http.JSONResponses.INCORRECT_ACCOUNT;
import static nxt.http.JSONResponses.MISSING_ACCOUNT;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.util.Convert;

public final class RSConvert extends APIServlet.APIRequestHandler {

    static final RSConvert instance = new RSConvert();

    private RSConvert() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.UTILS}, "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        String accountValue = Convert.emptyToNull(req.getParameter("account"));
        if (accountValue == null) {
            return MISSING_ACCOUNT;
        }
        try {
            long accountId = Convert.parseAccountId(accountValue);
            if (accountId == 0) {
                return INCORRECT_ACCOUNT;
            }
            JSONObject response = new JSONObject();
            JSONData.putAccount(response, "account", accountId);
            return response;
        } catch (RuntimeException e) {
            return INCORRECT_ACCOUNT;
        }
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

}
