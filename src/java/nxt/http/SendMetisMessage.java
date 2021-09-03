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

import nxt.Account;
import nxt.Attachment;
import nxt.NxtException;
import nxt.TransactionType;

public final class SendMetisMessage extends CreateTransaction {

    static final SendMetisMessage instance = new SendMetisMessage();
    
    private static final byte SUBTYPE_MESSAGING_METIS_DATA = 16;
    private static final byte SUBTYPE_MESSAGING_METIS_METADATA = 17;

    private SendMetisMessage() {
        super(new APITag[] {APITag.MESSAGES, APITag.CREATE_TRANSACTION}, "recipient", "subtype");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long recipientId = ParameterParser.getAccountId(req, "recipient", false);
        int subtype = ParameterParser.getInt(req, "subtype", 0, 17, false);
        Account account = ParameterParser.getSenderAccount(req);
        Attachment attachement;
        
        if (subtype == 0) {
        	attachement = Attachment.ARBITRARY_MESSAGE;
        } else if (subtype == TransactionType.SUBTYPE_MESSAGING_METIS_ACCOUNT_INFO) {
        	attachement = Attachment.METIS_ACCOUNT_INFO;
        } else if (subtype == TransactionType.SUBTYPE_MESSAGING_METIS_CHANNEL_INVITATION) {
        	attachement = Attachment.METIS_CHANNEL_INVITATION;
        } else if (subtype == TransactionType.SUBTYPE_MESSAGING_METIS_CHANNEL_MEMBER) {
        	attachement = Attachment.METIS_CHANNEL_MEMBER;
        } else if (subtype == SUBTYPE_MESSAGING_METIS_DATA) {
        	attachement = Attachment.METIS_DATA;
        } else if (subtype == SUBTYPE_MESSAGING_METIS_METADATA) {
        	attachement = Attachment.METIS_METADATA;
        } else {
        	attachement = Attachment.METIS_ARBITRARY_MESSAGE;
        }
        
        return createTransaction(req, account, recipientId, 0, attachement);
    }

}
