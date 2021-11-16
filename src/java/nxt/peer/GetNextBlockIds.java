/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2017-2020 Sigwo Technologies
 * Copyright © 2020-2021 Jupiter Project Developers
 *
 * See t Copyright © 2017-2020 Sigwo Technologies
 * Copyright © 2020-2021 Jupiter Project Developers
 *he LICENSE.txt file at the top-level directory of this distribution
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

package nxt.peer;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Jup;
import nxt.util.Convert;

final class GetNextBlockIds extends PeerServlet.PeerRequestHandler {

    static final GetNextBlockIds instance = new GetNextBlockIds();

    private GetNextBlockIds() {}


    @Override
    JSONStreamAware processRequest(JSONObject request, Peer peer) {

        JSONObject response = new JSONObject();

        JSONArray nextBlockIds = new JSONArray();
        long blockId = Convert.parseUnsignedLong((String) request.get("blockId"));
        int limit = (int)Convert.parseLong(request.get("limit"));
        if (limit > 1440) {
            return GetNextBlocks.TOO_MANY_BLOCKS_REQUESTED;
        }
        List<Long> ids = Jup.getBlockchain().getBlockIdsAfter(blockId, limit > 0 ? limit : 1440);
        ids.forEach(id -> nextBlockIds.add(Long.toUnsignedString(id)));
        response.put("nextBlockIds", nextBlockIds);

        return response;
    }

    @Override
    boolean rejectWhileDownloading() {
        return true;
    }

}
