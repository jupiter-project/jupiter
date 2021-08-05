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

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Order;
import nxt.db.DbIterator;
import nxt.util.Convert;

public final class SearchAllOpenAskOrders extends APIServlet.APIRequestHandler {

    static final SearchAllOpenAskOrders instance = new SearchAllOpenAskOrders();

    private SearchAllOpenAskOrders() {
        super(new APITag[] {APITag.AE}, "query", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        String query = Convert.nullToEmpty(req.getParameter("query"));
        if (query.isEmpty()) {
            return JSONResponses.missing("query");
        }
        
        int firstIndexToInclude = ParameterParser.getFirstIndex(req);
        int lastIndexToInclude = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        JSONArray ordersData = new JSONArray();

        int elementsFiltered = 0;
        try (DbIterator<Order.Ask> askOrders = Order.Ask.getAll()) {
            while (askOrders.hasNext()) {
            	Order.Ask askOrder = askOrders.next();
            	
            	JSONObject askOrderJSON = JSONData.askOrder(askOrder, true);
            	
            	// filter by name or description fields by the query parameter
            	if (filterByNameDescription(askOrderJSON, query)) {
            		if (elementsFiltered >= firstIndexToInclude && elementsFiltered <= lastIndexToInclude) {
            			ordersData.add(askOrderJSON);
            		}
            		
            		elementsFiltered++;
            		
            		if (elementsFiltered > lastIndexToInclude) {
            			break;
            		}
            	}
            }
        }

        response.put("openOrders", ordersData);
        return response;
    }
    
    private boolean filterByNameDescription(JSONObject json, String query) {
    	if (query == null || query.isEmpty()) {
    		return true;
    	}
    	String[] queryWords = query.split(" ");
    	boolean matches = true;
    	for (String word : queryWords) {
    		matches = matches && (match(json, NAME_FIELD, word) || match(json, DESCRIPTION_FIELD, word));
    	}

		return matches;
    }
    
    private boolean match(JSONObject json, String field, String query) {
    	if ((json.containsKey(field) && ((String)json.get(field)).toLowerCase().indexOf(query.toLowerCase()) != -1)){
			return true;
		} 
    	return false;
    }
}