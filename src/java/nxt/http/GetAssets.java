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

import static nxt.http.JSONResponses.UNKNOWN_ASSET;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Asset;

public final class GetAssets extends APIServlet.APIRequestHandler {

    static final GetAssets instance = new GetAssets();

    private GetAssets() {
        super(new APITag[] {APITag.AE}, "assets", "assets", "assets", "includeCounts"); // limit to 3 for testing
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long[] assetIds = ParameterParser.getUnsignedLongs(req, "assets");
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        JSONObject response = new JSONObject();
        JSONArray assetsJSONArray = new JSONArray();
        response.put("assets", assetsJSONArray);
        for (long assetId : assetIds) {
            Asset asset = Asset.getAsset(assetId);
            if (asset == null) {
                return UNKNOWN_ASSET;
            }
            assetsJSONArray.add(JSONData.asset(asset, includeCounts));
        }
        return response;
    }

}
