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

import nxt.Account;
import nxt.db.DbIterator;
import nxt.util.Convert;

public final class SearchAccountAssets extends APIServlet.APIRequestHandler {

    static final SearchAccountAssets instance = new SearchAccountAssets();

    private SearchAccountAssets() {
        super(new APITag[] {APITag.AE}, "query", "account", "height", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        String query = Convert.nullToEmpty(req.getParameter("query"));
        if (query.isEmpty()) {
            return JSONResponses.missing("query");
        }
        
        int firstIndexToInclude = ParameterParser.getFirstIndex(req);
        int lastIndexToInclude = ParameterParser.getLastIndex(req);
        long accountId = ParameterParser.getAccountId(req, true);
        int height = ParameterParser.getHeight(req);

        JSONObject response = new JSONObject();
        JSONArray assetJSON = new JSONArray();

        int elementsFiltered = 0;
        try (DbIterator<Account.AccountAsset> accountAssets = Account.getAccountAssets(accountId, height, 0, -1)) {
            while (accountAssets.hasNext()) {
            	JSONObject accountAssetJSON = JSONData.accountAsset(accountAssets.next(), false, true);
                
            	if (matchByName(accountAssetJSON, query)) {
            		if (elementsFiltered >= firstIndexToInclude && elementsFiltered <= lastIndexToInclude) {
            			assetJSON.add(accountAssetJSON);
            		}
            		
            		elementsFiltered++;
            		
            		if (elementsFiltered > lastIndexToInclude) {
            			break;
            		}
            	}
            }
            
            response.put("accountAssets", assetJSON);
            return response;
        }
    }
    
    private boolean matchByName(JSONObject askOrderJSON, String query) {
		if ((askOrderJSON.containsKey(NAME_FIELD) && ((String)askOrderJSON.get(NAME_FIELD)).toLowerCase().indexOf(query) != -1)){
			return true;
		} 

		return false;
    }
}
