/*
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

import static jup.http.JSONResponses.incorrect;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONStreamAware;

import jup.NxtException;
import jup.util.JSON;

public class AddMetis extends APIServlet.APIRequestHandler {

    /** AddMetis instance */
    static final AddMetis instance = new AddMetis();

    /**
     * Create the AddMetis instance
     */
    private AddMetis() {
        super(new APITag[] {APITag.METIS}, "host", "protocol", "port");
    }

    /**
     * Process the AddMetis API request
     *
     * @param   req                 API request
     * @return                      API response
     * @throws NxtException 
     */
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
    	String host = ParameterParser.getString(request, "host", true);
    	String protocol = ParameterParser.getString(request, "protocol", true);
    	if (!protocol.equals("ws") && !protocol.equals("wss")) {
    		throw new ParameterException(incorrect("protocol"));
    	}
    	String port = ParameterParser.getString(request, "port", false);
    	

    	MetisServers.metisService.submit(() -> {
            MetisServer newMetisServer = MetisServers.createMetisServer(protocol, port, host);
            if (newMetisServer != null) {
            	MetisServers.add(newMetisServer);
            }
        });
        return JSON.emptyJSON;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }
}