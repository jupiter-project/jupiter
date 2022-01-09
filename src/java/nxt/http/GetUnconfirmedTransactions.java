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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Nxt;
import nxt.Transaction;
import nxt.db.DbIterator;
import nxt.db.FilteringIterator;
import nxt.util.Convert;

public final class GetUnconfirmedTransactions extends APIServlet.APIRequestHandler {

    static final GetUnconfirmedTransactions instance = new GetUnconfirmedTransactions();

    private GetUnconfirmedTransactions() {
        super(new APITag[] {APITag.TRANSACTIONS, APITag.ACCOUNTS}, "account", "account", "account", "firstIndex", "lastIndex",
        		"withMessage", "message");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        Set<Long> accountIds = Convert.toSet(ParameterParser.getAccountIds(req, false));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        
        boolean withMessage = "true".equalsIgnoreCase(req.getParameter("withMessage"));
        String messageToFilter = req.getParameter("message");
        int firstIndexToInclude = ParameterParser.getFirstIndex(req);
        int lastIndexToInclude = ParameterParser.getLastIndex(req);
        if (withMessage && messageToFilter != null && !messageToFilter.isEmpty()) {
        	firstIndex = 0;
        	lastIndex = Integer.MAX_VALUE;
        }

        JSONArray transactions = new JSONArray();
        int elementsFiltered = 0;
        if (accountIds.isEmpty()) {
            try (DbIterator<? extends Transaction> transactionsIterator = Nxt.getTransactionProcessor().getAllUnconfirmedTransactions(firstIndex, lastIndex)) {
                while (transactionsIterator.hasNext()) {
                    Transaction transaction = transactionsIterator.next();
                    
                    if (withMessage) {
    	            	if (transaction.getMessage() == null) continue;
    	
    	            	if (messageToFilter != null && !messageToFilter.isEmpty()) {
    	                	String messageString = Convert.toString(transaction.getMessage().getMessage(), transaction.getMessage().isText());
    	                	if (!messageString.contains(messageToFilter)) {
    	                		continue;
    	                	}
    	                	
    	                	if (elementsFiltered >= firstIndexToInclude && elementsFiltered <= lastIndexToInclude) {
    		            		transactions.add(JSONData.unconfirmedTransaction(transaction));
    	            		}
    	            		
    	            		elementsFiltered++;
    	            		
    	            		if (elementsFiltered > lastIndexToInclude) {
    	            			break;
    	            		}

    	            	} else {
    	                	transactions.add(JSONData.unconfirmedTransaction(transaction));
    	                }
    	            	
                    } else {
                    	transactions.add(JSONData.unconfirmedTransaction(transaction));
                    }
                }
            }
        } else {
            try (FilteringIterator<? extends Transaction> transactionsIterator = new FilteringIterator<> (
                    Nxt.getTransactionProcessor().getAllUnconfirmedTransactions(0, -1),
                    transaction -> accountIds.contains(transaction.getSenderId()) || accountIds.contains(transaction.getRecipientId()),
                    firstIndex, lastIndex)) {
                while (transactionsIterator.hasNext()) {
                    Transaction transaction = transactionsIterator.next();
                    transactions.add(JSONData.unconfirmedTransaction(transaction));
                }
            }
        }

        JSONObject response = new JSONObject();
        response.put("unconfirmedTransactions", transactions);
        return response;
    }

}
