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

import nxt.DigitalGoodsStore;
import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.util.Convert;

public final class GetDGSTagsLike extends APIServlet.APIRequestHandler {

    static final GetDGSTagsLike instance = new GetDGSTagsLike();

    private GetDGSTagsLike() {
        super(new APITag[] {APITag.DGS, APITag.SEARCH}, "tagPrefix", "inStockOnly", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        final boolean inStockOnly = "true".equalsIgnoreCase(req.getParameter("inStockOnly"));
        String prefix = Convert.emptyToNull(req.getParameter("tagPrefix"));
        if (prefix == null) {
            return JSONResponses.missing("tagPrefix");
        }
        if (prefix.length() < 2) {
            return JSONResponses.incorrect("tagPrefix", "tagPrefix must be at least 2 characters long");
        }

        JSONObject response = new JSONObject();
        JSONArray tagsJSON = new JSONArray();
        response.put("tags", tagsJSON);
        try (DbIterator<DigitalGoodsStore.Tag> tags = DigitalGoodsStore.Tag.getTagsLike(prefix, inStockOnly, firstIndex, lastIndex)) {
            while (tags.hasNext()) {
                tagsJSON.add(JSONData.tag(tags.next()));
            }
        }
        return response;
    }

}
