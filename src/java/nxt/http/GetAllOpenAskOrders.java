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
    
    private static String NAME_FIELD = "name";
    private static String DESCRIPTION_FIELD = "description";

    private GetAllOpenAskOrders() {
        super(new APITag[] {APITag.AE}, "query", "firstIndex", "lastIndex");
    }
    

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        JSONArray ordersData = new JSONArray();

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        String query = Convert.nullToEmpty(req.getParameter("query")).toLowerCase();

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
            		if (transaction.getAttachment().getJSONObject().containsKey(NAME_FIELD)){
            			String nameString = (String) transaction.getAttachment().getJSONObject().get(NAME_FIELD);
            			askOrderJSON.put(NAME_FIELD, nameString);
            		}
            		if (transaction.getAttachment().getJSONObject().containsKey(DESCRIPTION_FIELD)){
            			String descriptionString = (String) transaction.getAttachment().getJSONObject().get(DESCRIPTION_FIELD);
            			askOrderJSON.put(DESCRIPTION_FIELD, descriptionString);
            		}
            	}
            	
            	// filter by name or description fields by the query parameter
            	if (matchByNameOrDescription(askOrderJSON, query)) {
            		ordersData.add(askOrderJSON);
            	}
            }
        }

        response.put("openOrders", ordersData);
        return response;
    }
    
    private boolean matchByNameOrDescription(JSONObject askOrderJSON, String query ) {
    	if (query.isEmpty()) {
    		return true;
    	}
    	
		if ((askOrderJSON.containsKey(NAME_FIELD) && ((String)askOrderJSON.get(NAME_FIELD)).toLowerCase().indexOf(query) != -1) ||
		(askOrderJSON.containsKey(DESCRIPTION_FIELD) && ((String)askOrderJSON.get(DESCRIPTION_FIELD)).toLowerCase().indexOf(query) != -1)){
			System.out.println(true);
			return true;
		} 

		return false;
    }
}
