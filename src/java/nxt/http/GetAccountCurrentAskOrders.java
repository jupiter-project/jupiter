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

package nxt.http;

import nxt.Nxt;
import nxt.NxtException;
import nxt.Order;
import nxt.Transaction;
import nxt.db.DbIterator;
import nxt.util.Convert;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountCurrentAskOrders extends APIServlet.APIRequestHandler {

    static final GetAccountCurrentAskOrders instance = new GetAccountCurrentAskOrders();

    private GetAccountCurrentAskOrders() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.AE}, "account", "asset", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

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
        JSONArray orders = new JSONArray();
        try {
            while (askOrders.hasNext()) {
            	Order.Ask askOrder = askOrders.next();
            	
            	Transaction transaction = Nxt.getBlockchain().getTransaction(askOrder.getAssetId());
            	JSONObject askOrderJSON = JSONData.askOrder(askOrder);
            	if (transaction != null) {
            		if (transaction.getMessage() != null) {
            			String messageString = Convert.toString(transaction.getMessage().getMessage(), transaction.getMessage().isText());
            			askOrderJSON.put(MESSAGE_FIELD, messageString);
            		}
            		if (transaction.getAttachment().getJSONObject().containsKey(NAME_FIELD)){
            			String nameString = (String) transaction.getAttachment().getJSONObject().get(NAME_FIELD);
            			askOrderJSON.put(NAME_FIELD, nameString);
            		}
            		if (transaction.getAttachment().getJSONObject().containsKey(DESCRIPTION_FIELD)){
            			String descriptionString = (String) transaction.getAttachment().getJSONObject().get(DESCRIPTION_FIELD);
            			askOrderJSON.put(DESCRIPTION_FIELD, descriptionString);
            		}
            	}
            	
            	
                orders.add(askOrderJSON);
            }
        } finally {
            askOrders.close();
        }
        JSONObject response = new JSONObject();
        response.put("askOrders", orders);
        return response;
    }

}
