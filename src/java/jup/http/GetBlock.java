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

import static jup.http.JSONResponses.INCORRECT_BLOCK;
import static jup.http.JSONResponses.INCORRECT_HEIGHT;
import static jup.http.JSONResponses.INCORRECT_TIMESTAMP;
import static jup.http.JSONResponses.UNKNOWN_BLOCK;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONStreamAware;

import jup.Block;
import jup.Jup;
import jup.util.Convert;

public final class GetBlock extends APIServlet.APIRequestHandler {

    static final GetBlock instance = new GetBlock();

    private GetBlock() {
        super(new APITag[] {APITag.BLOCKS}, "block", "height", "timestamp", "includeTransactions", "includeExecutedPhased");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        Block blockData;
        String blockValue = Convert.emptyToNull(req.getParameter("block"));
        String heightValue = Convert.emptyToNull(req.getParameter("height"));
        String timestampValue = Convert.emptyToNull(req.getParameter("timestamp"));
        if (blockValue != null) {
            try {
                blockData = Jup.getBlockchain().getBlock(Convert.parseUnsignedLong(blockValue));
            } catch (RuntimeException e) {
                return INCORRECT_BLOCK;
            }
        } else if (heightValue != null) {
            try {
                int height = Integer.parseInt(heightValue);
                if (height < 0 || height > Jup.getBlockchain().getHeight()) {
                    return INCORRECT_HEIGHT;
                }
                blockData = Jup.getBlockchain().getBlockAtHeight(height);
            } catch (RuntimeException e) {
                return INCORRECT_HEIGHT;
            }
        } else if (timestampValue != null) {
            try {
                int timestamp = Integer.parseInt(timestampValue);
                if (timestamp < 0) {
                    return INCORRECT_TIMESTAMP;
                }
                blockData = Jup.getBlockchain().getLastBlock(timestamp);
            } catch (RuntimeException e) {
                return INCORRECT_TIMESTAMP;
            }
        } else {
            blockData = Jup.getBlockchain().getLastBlock();
        }

        if (blockData == null) {
            return UNKNOWN_BLOCK;
        }

        boolean includeTransactions = "true".equalsIgnoreCase(req.getParameter("includeTransactions"));
        boolean includeExecutedPhased = "true".equalsIgnoreCase(req.getParameter("includeExecutedPhased"));

        return JSONData.block(blockData, includeTransactions, false, includeExecutedPhased);

    }

}