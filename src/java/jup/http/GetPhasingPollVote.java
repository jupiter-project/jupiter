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

import org.json.simple.JSONStreamAware;

import jup.JupException;
import jup.PhasingVote;
import jup.util.JSON;

public class GetPhasingPollVote extends APIServlet.APIRequestHandler  {
    static final GetPhasingPollVote instance = new GetPhasingPollVote();

    private GetPhasingPollVote() {
        super(new APITag[] {APITag.PHASING}, "transaction", "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws JupException {
        long transactionId = ParameterParser.getUnsignedLong(req, "transaction", true);
        long accountId = ParameterParser.getAccountId(req, true);

        PhasingVote phasingVote = PhasingVote.getVote(transactionId, accountId);
        if (phasingVote != null) {
            return JSONData.phasingPollVote(phasingVote);
        }
        return JSON.emptyJSON;
    }
}
