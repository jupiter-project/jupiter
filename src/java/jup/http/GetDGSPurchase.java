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

import static jup.http.JSONResponses.DECRYPTION_FAILED;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import jup.Account;
import jup.DigitalGoodsStore;
import jup.JupException;
import jup.crypto.Crypto;
import jup.util.Convert;
import jup.util.Logger;

public final class GetDGSPurchase extends APIServlet.APIRequestHandler {

    static final GetDGSPurchase instance = new GetDGSPurchase();

    private GetDGSPurchase() {
        super(new APITag[] {APITag.DGS}, "purchase", "secretPhrase", "sharedKey");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws JupException {

        DigitalGoodsStore.Purchase purchase = ParameterParser.getPurchase(req);
        JSONObject response = JSONData.purchase(purchase);

        byte[] sharedKey = ParameterParser.getBytes(req, "sharedKey", false);
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        if (sharedKey.length != 0 && secretPhrase != null) {
            return JSONResponses.either("secretPhrase", "sharedKey");
        }
        if (sharedKey.length == 0 && secretPhrase == null) {
            return response;
        }
        if (purchase.getEncryptedGoods() != null) {
            byte[] data = purchase.getEncryptedGoods().getData();
            try {
                byte[] decrypted = Convert.EMPTY_BYTE;
                if (data.length != 0) {
                    if (secretPhrase != null) {
                        byte[] readerPublicKey = Crypto.getPublicKey(secretPhrase);
                        byte[] sellerPublicKey = Account.getPublicKey(purchase.getSellerId());
                        byte[] buyerPublicKey = Account.getPublicKey(purchase.getBuyerId());
                        byte[] publicKey = Arrays.equals(sellerPublicKey, readerPublicKey) ? buyerPublicKey : sellerPublicKey;
                        if (publicKey != null) {
                            decrypted = Account.decryptFrom(publicKey, purchase.getEncryptedGoods(), secretPhrase, true);
                        }
                    } else {
                        decrypted = Crypto.aesDecrypt(purchase.getEncryptedGoods().getData(), sharedKey);
                        decrypted = Convert.uncompress(decrypted);
                    }
                }
                response.put("decryptedGoods", Convert.toString(decrypted, purchase.goodsIsText()));
            } catch (RuntimeException e) {
                Logger.logDebugMessage("Error trying to decrypt in GetDGSPurchase " + e.toString(), e);
                return DECRYPTION_FAILED;
            }
        }
        return response;
    }

}
