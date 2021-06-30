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

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Asset;
import nxt.Nxt;
import nxt.Transaction;
import nxt.db.DbIterator;
import nxt.util.Convert;

public final class GetAssetsByIssuer extends APIServlet.APIRequestHandler {

    static final GetAssetsByIssuer instance = new GetAssetsByIssuer();

    private GetAssetsByIssuer() {
        super(new APITag[] {APITag.AE, APITag.ACCOUNTS}, "query", "account", "account", "account", "firstIndex", "lastIndex", "includeCounts", "includeNTFInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long[] accountIds = ParameterParser.getAccountIds(req, true);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        String query = Convert.nullToEmpty(req.getParameter("query")).toLowerCase();
        boolean includeNTFInfo = "true".equalsIgnoreCase(req.getParameter("includeNTFInfo"));

        JSONObject response = new JSONObject();
        JSONArray accountsJSONArray = new JSONArray();
        response.put("assets", accountsJSONArray);
        for (long accountId : accountIds) {
            JSONArray assetsJSONArray = new JSONArray();
            try (DbIterator<Asset> assets = Asset.getAssetsIssuedBy(query, accountId, firstIndex, lastIndex)) {
                while (assets.hasNext()) {
                	Asset asset = assets.next();
                	JSONObject assetJSON = JSONData.asset(asset, includeCounts);
                	
                	if (includeNTFInfo) {
                		Transaction transaction = Nxt.getBlockchain().getTransaction(asset.getId());
                	
	                	if (transaction != null) {
	                		if (transaction.getMessage() != null) {
	                			String messageString = Convert.toString(transaction.getMessage().getMessage(), transaction.getMessage().isText());
	                			assetJSON.put(MESSAGE_FIELD, messageString);
	                		}
	                	}
                	}
                	
                	assetsJSONArray.add(assetJSON);
                }
            }
            accountsJSONArray.add(assetsJSONArray);
        }
        return response;
    }
}
