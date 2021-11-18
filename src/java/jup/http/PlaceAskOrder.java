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

import static jup.http.JSONResponses.NOT_ENOUGH_ASSETS;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONStreamAware;

import jup.Account;
import jup.Asset;
import jup.Attachment;
import jup.JupException;

public final class PlaceAskOrder extends CreateTransaction {

    static final PlaceAskOrder instance = new PlaceAskOrder();

    private PlaceAskOrder() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "asset", "quantityQNT", "priceNQT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws JupException {

        Asset asset = ParameterParser.getAsset(req);
        long priceNQT = ParameterParser.getPriceNQT(req);
        long quantityQNT = ParameterParser.getQuantityQNT(req);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new Attachment.ColoredCoinsAskOrderPlacement(asset.getId(), quantityQNT, priceNQT);
        try {
            return createTransaction(req, account, attachment);
        } catch (JupException.InsufficientBalanceException e) {
            return NOT_ENOUGH_ASSETS;
        }
    }

}
