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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import javax.servlet.http.HttpServletRequest;

import java.util.Collection;

public final class GetMetisServers extends APIServlet.APIRequestHandler {

    static final GetMetisServers instance = new GetMetisServers();

    private GetMetisServers() {
        super(new APITag[] {APITag.METIS});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

    	Collection<? extends MetisServer> metisServers = MetisServers.getMetisServers();
    	
    	JSONArray metisJSON = new JSONArray();
    	metisServers.forEach(metisServer -> {
    		metisJSON.add(JSONData.peer(metisServer));
        });

    	JSONObject response = new JSONObject();
        response.put("metisServers", metisJSON);
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }
}
