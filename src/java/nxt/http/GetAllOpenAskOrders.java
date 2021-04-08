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
import nxt.Order;
import nxt.Transaction;
import nxt.db.DbIterator;
import nxt.util.Convert;
import nxt.util.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAllOpenAskOrders extends APIServlet.APIRequestHandler {

    static final GetAllOpenAskOrders instance = new GetAllOpenAskOrders();

    private GetAllOpenAskOrders() {
        super(new APITag[] {APITag.AE}, "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        JSONArray ordersData = new JSONArray();

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        try (DbIterator<Order.Ask> askOrders = Order.Ask.getAll(firstIndex, lastIndex)) {
            while (askOrders.hasNext()) {
            	Order.Ask askOrder = askOrders.next();
            	
            	Transaction transaction = Nxt.getBlockchain().getTransaction(askOrder.getAssetId());
            	JSONObject askOrderJSON = JSONData.askOrder(askOrder);
            	if (transaction != null) {
            		if (transaction.getMessage() != null) {
            			String messageString = Convert.toString(transaction.getMessage().getMessage(), transaction.getMessage().isText());
                    	askOrderJSON.put("message", messageString);
            		}
            		if (transaction.getAttachment().getJSONObject().containsKey("name")){
            			askOrderJSON.put("name", transaction.getAttachment().getJSONObject().get("name"));
            		}
            		if (transaction.getAttachment().getJSONObject().containsKey("description")){
            			askOrderJSON.put("description", transaction.getAttachment().getJSONObject().get("description"));
            		}
                    Logger.logMessage(transaction.getAttachment().getJSONObject().toJSONString());
            	}
            	
                ordersData.add(askOrderJSON);
            }
        }

        response.put("openOrders", ordersData);
        return response;
    }

}
