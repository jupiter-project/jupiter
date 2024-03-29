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

import nxt.PhasingPoll;

public final class GetPhasingPolls extends APIServlet.APIRequestHandler {

    static final GetPhasingPolls instance = new GetPhasingPolls();

    private GetPhasingPolls() {
        super(new APITag[] {APITag.PHASING}, "transaction", "transaction", "transaction", "countVotes"); // limit to 3 for testing
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long[] transactionIds = ParameterParser.getUnsignedLongs(req, "transaction");
        boolean countVotes = "true".equalsIgnoreCase(req.getParameter("countVotes"));
        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        response.put("polls", jsonArray);
        for (long transactionId : transactionIds) {
            PhasingPoll poll = PhasingPoll.getPoll(transactionId);
            if (poll != null) {
                jsonArray.add(JSONData.phasingPoll(poll, countVotes));
            } else {
                PhasingPoll.PhasingPollResult pollResult = PhasingPoll.getResult(transactionId);
                if (pollResult != null) {
                    jsonArray.add(JSONData.phasingPollResult(pollResult));
                }
            }
        }
        return response;
    }

}
