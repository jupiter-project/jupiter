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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import jup.JupException;
import jup.Order;
import jup.db.DbIterator;

public final class GetAccountCurrentAskOrderIds extends APIServlet.APIRequestHandler {

    static final GetAccountCurrentAskOrderIds instance = new GetAccountCurrentAskOrderIds();

    private GetAccountCurrentAskOrderIds() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.AE}, "account", "asset", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws JupException {

        long accountId = ParameterParser.getAccountId(req, true);
        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        DbIterator<Order.Ask> askOrders;
        if (assetId == 0) {
            askOrders = Order.Ask.getAskOrdersByAccount(accountId, firstIndex, lastIndex);
        } else {
            askOrders = Order.Ask.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex);
        }
        JSONArray orderIds = new JSONArray();
        try {
            while (askOrders.hasNext()) {
                orderIds.add(Long.toUnsignedString(askOrders.next().getId()));
            }
        } finally {
            askOrders.close();
        }
        JSONObject response = new JSONObject();
        response.put("askOrderIds", orderIds);
        return response;
    }

}
