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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import jup.Jup;
import jup.JupException;
import jup.Transaction;
import jup.TransactionType;
import jup.util.Filter;

public final class GetExpectedOrderCancellations extends APIServlet.APIRequestHandler {

    static final GetExpectedOrderCancellations instance = new GetExpectedOrderCancellations();

    private GetExpectedOrderCancellations() {
        super(new APITag[] {APITag.AE});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws JupException {
        Filter<Transaction> filter = transaction -> transaction.getType() == TransactionType.ColoredCoins.ASK_ORDER_CANCELLATION
                || transaction.getType() == TransactionType.ColoredCoins.BID_ORDER_CANCELLATION;

        List<? extends Transaction> transactions = Jup.getBlockchain().getExpectedTransactions(filter);
        JSONArray cancellations = new JSONArray();
        transactions.forEach(transaction -> cancellations.add(JSONData.expectedOrderCancellation(transaction)));
        JSONObject response = new JSONObject();
        response.put("orderCancellations", cancellations);
        return response;
    }
}
