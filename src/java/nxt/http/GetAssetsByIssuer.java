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

import nxt.Asset;
import nxt.Nxt;
import nxt.Transaction;
import nxt.db.DbIterator;
import nxt.util.Convert;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssetsByIssuer extends APIServlet.APIRequestHandler {

    static final GetAssetsByIssuer instance = new GetAssetsByIssuer();

    private GetAssetsByIssuer() {
        super(new APITag[] {APITag.AE, APITag.ACCOUNTS}, "query", "account", "account", "account", "firstIndex", "lastIndex", "includeCounts");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long[] accountIds = ParameterParser.getAccountIds(req, true);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        String query = Convert.nullToEmpty(req.getParameter("query")).toLowerCase();

        JSONObject response = new JSONObject();
        JSONArray accountsJSONArray = new JSONArray();
        response.put("assets", accountsJSONArray);
        for (long accountId : accountIds) {
            JSONArray assetsJSONArray = new JSONArray();
            try (DbIterator<Asset> assets = Asset.getAssetsIssuedBy(accountId, firstIndex, lastIndex)) {
                while (assets.hasNext()) {
                	Asset asset = assets.next();
                	JSONObject assetJSON = JSONData.asset(asset, includeCounts);
                	
                	Transaction transaction = Nxt.getBlockchain().getTransaction(asset.getId());
                	if (transaction != null) {
                		if (transaction.getMessage() != null) {
                			String messageString = Convert.toString(transaction.getMessage().getMessage(), transaction.getMessage().isText());
                			assetJSON.put(MESSAGE_FIELD, messageString);
                		}
                	}
                	
                	// filter by name or description fields by the query parameter
                	if (matchByNameOrDescription(assetJSON, query)) {
                		assetsJSONArray.add(assetJSON);
                	}
                    
                }
            }
            accountsJSONArray.add(assetsJSONArray);
        }
        return response;
    }
    
    private boolean matchByNameOrDescription(JSONObject assetJSON, String query) {
    	if (query.isEmpty()) {
    		return true;
    	}
    	
		if ((assetJSON.containsKey(NAME_FIELD) && ((String)assetJSON.get(NAME_FIELD)).toLowerCase().indexOf(query) != -1) ||
		(assetJSON.containsKey(DESCRIPTION_FIELD) && ((String)assetJSON.get(DESCRIPTION_FIELD)).toLowerCase().indexOf(query) != -1)){
			return true;
		} 

		return false;
    }

}
