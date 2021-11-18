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

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import jup.Currency;
import jup.JupException;

public final class CanDeleteCurrency extends APIServlet.APIRequestHandler {

    static final CanDeleteCurrency instance = new CanDeleteCurrency();

    private CanDeleteCurrency() {
        super(new APITag[] {APITag.MS}, "account", "currency");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws JupException {
        Currency currency = ParameterParser.getCurrency(req);
        long accountId = ParameterParser.getAccountId(req, true);
        JSONObject response = new JSONObject();
        response.put("canDelete", currency.canBeDeletedBy(accountId));
        return response;
    }

}
