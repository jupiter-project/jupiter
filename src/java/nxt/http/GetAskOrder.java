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

import nxt.Nxt;
import nxt.NxtException;
import nxt.Order;
import nxt.Transaction;
import nxt.util.Convert;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.UNKNOWN_ORDER;

public final class GetAskOrder extends APIServlet.APIRequestHandler {

    static final GetAskOrder instance = new GetAskOrder();

    private GetAskOrder() {
        super(new APITag[] {APITag.AE}, "order");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long orderId = ParameterParser.getUnsignedLong(req, "order", true);
        Order.Ask askOrder = Order.Ask.getAskOrder(orderId);
        if (askOrder == null) {
            return UNKNOWN_ORDER;
        }
        
        JSONObject askOrderJSON = JSONData.askOrder(askOrder);
        Transaction transaction = Nxt.getBlockchain().getTransaction(askOrder.getAssetId());
    	if (transaction != null) {
    		if (transaction.getMessage() != null) {
    			String messageString = Convert.toString(transaction.getMessage().getMessage(), transaction.getMessage().isText());
    			askOrderJSON.put(MESSAGE_FIELD, messageString);
    		}
    		if (transaction.getAttachment().getJSONObject().containsKey(NAME_FIELD)){
    			String nameString = (String) transaction.getAttachment().getJSONObject().get(NAME_FIELD);
    			askOrderJSON.put(NAME_FIELD, nameString);
    		}
    		if (transaction.getAttachment().getJSONObject().containsKey(DESCRIPTION_FIELD)){
    			String descriptionString = (String) transaction.getAttachment().getJSONObject().get(DESCRIPTION_FIELD);
    			askOrderJSON.put(DESCRIPTION_FIELD, descriptionString);
    		}
    	}
        
        
        return askOrderJSON;
    }
}
