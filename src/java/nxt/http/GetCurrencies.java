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

import static nxt.http.JSONResponses.UNKNOWN_CURRENCY;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Currency;

public final class GetCurrencies extends APIServlet.APIRequestHandler {

    static final GetCurrencies instance = new GetCurrencies();

    private GetCurrencies() {
        super(new APITag[] {APITag.MS}, "currencies", "currencies", "currencies", "includeCounts"); // limit to 3 for testing
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long[] currencyIds = ParameterParser.getUnsignedLongs(req, "currencies");
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        JSONObject response = new JSONObject();
        JSONArray currenciesJSONArray = new JSONArray();
        response.put("currencies", currenciesJSONArray);
        for (long currencyId : currencyIds) {
            Currency currency = Currency.getCurrency(currencyId);
            if (currency == null) {
                return UNKNOWN_CURRENCY;
            }
            currenciesJSONArray.add(JSONData.currency(currency, includeCounts));
        }
        return response;
    }

}
