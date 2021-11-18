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

import static jup.http.JSONResponses.PRUNED_TRANSACTION;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONStreamAware;

import jup.Jup;
import jup.JupException;
import jup.PrunableMessage;
import jup.util.JSON;

public final class GetPrunableMessage extends APIServlet.APIRequestHandler {

    static final GetPrunableMessage instance = new GetPrunableMessage();

    private GetPrunableMessage() {
        super(new APITag[] {APITag.MESSAGES}, "transaction", "secretPhrase", "sharedKey", "retrieve");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws JupException {
        long transactionId = ParameterParser.getUnsignedLong(req, "transaction", true);
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        byte[] sharedKey = ParameterParser.getBytes(req, "sharedKey", false);
        if (sharedKey.length != 0 && secretPhrase != null) {
            return JSONResponses.either("secretPhrase", "sharedKey");
        }
        boolean retrieve = "true".equalsIgnoreCase(req.getParameter("retrieve"));
        PrunableMessage prunableMessage = PrunableMessage.getPrunableMessage(transactionId);
        if (prunableMessage == null && retrieve) {
            if (Jup.getBlockchainProcessor().restorePrunedTransaction(transactionId) == null) {
                return PRUNED_TRANSACTION;
            }
            prunableMessage = PrunableMessage.getPrunableMessage(transactionId);
        }
        if (prunableMessage != null) {
            return JSONData.prunableMessage(prunableMessage, secretPhrase, sharedKey);
        }
        return JSON.emptyJSON;
    }

}
