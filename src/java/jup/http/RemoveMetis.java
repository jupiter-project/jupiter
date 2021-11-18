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
import jup.util.JSON;

public class RemoveMetis extends APIServlet.APIRequestHandler {

    /** AddMetis instance */
    static final RemoveMetis instance = new RemoveMetis();

    /**
     * Create the AddMetis instance
     */
    private RemoveMetis() {
        super(new APITag[] {APITag.METIS}, "metisHostServer");
    }

    /**
     * Process the AddMetis API request
     *
     * @param   req                 API request
     * @return                      API response
     * @throws JupException 
     */
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws JupException {
    	String announcedAddress = request.getParameter("metisHostServer");

    	MetisServers.metisService.submit(() -> {
            MetisServer newMetisServer = MetisServers.findOrCreateMetisServer(announcedAddress, false);
            if (newMetisServer != null) {
            	MetisServers.removeMetisServer(newMetisServer);
            }
        });
        return JSON.emptyJSON;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }
}