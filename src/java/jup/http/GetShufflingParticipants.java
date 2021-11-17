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

package jup.http;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import jup.NxtException;
import jup.ShufflingParticipant;
import jup.db.DbIterator;

public final class GetShufflingParticipants extends APIServlet.APIRequestHandler {

    static final GetShufflingParticipants instance = new GetShufflingParticipants();

    private GetShufflingParticipants() {
        super(new APITag[] {APITag.SHUFFLING}, "shuffling");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long shufflingId = ParameterParser.getUnsignedLong(req, "shuffling", true);
        JSONObject response = new JSONObject();
        JSONArray participantsJSONArray = new JSONArray();
        response.put("participants", participantsJSONArray);
        try (DbIterator<ShufflingParticipant> participants = ShufflingParticipant.getParticipants(shufflingId)) {
            for (ShufflingParticipant participant : participants) {
                participantsJSONArray.add(JSONData.participant(participant));
            }
        }
        return response;
    }

}
