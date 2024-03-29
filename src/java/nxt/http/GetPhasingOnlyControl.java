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

import org.json.simple.JSONStreamAware;

import nxt.AccountRestrictions.PhasingOnly;
import nxt.util.JSON;

/**
 * Returns the phasing control certain account. The result contains the following entries similar to the control* parameters of {@link SetPhasingOnlyControl}
 * 
 * <ul>
 * <li>votingModel - See {@link SetPhasingOnlyControl} for possible values. NONE(-1) means not control is set</li>
 * <li>quorum</li>
 * <li>minBalance</li>
 * <li>minBalanceModel - See {@link SetPhasingOnlyControl} for possible values</li>
 * <li>holding</li>
 * <li>whitelisted - array of whitelisted voter account IDs</li>
 * </ul>
 * 
 * <p>
 * Parameters
 * <ul>
 * <li>account - the account for which the phasing control is queried</li>
 * </ul>
 * 
 * 
 * @see SetPhasingOnlyControl
 * 
 */
public final class GetPhasingOnlyControl extends APIServlet.APIRequestHandler {

    static final GetPhasingOnlyControl instance = new GetPhasingOnlyControl();
    
    private GetPhasingOnlyControl() {
        super(new APITag[] {APITag.ACCOUNT_CONTROL}, "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long accountId = ParameterParser.getAccountId(req, true);
        PhasingOnly phasingOnly = PhasingOnly.get(accountId);
        return phasingOnly == null ? JSON.emptyJSON : JSONData.phasingOnly(phasingOnly);
    }

}
