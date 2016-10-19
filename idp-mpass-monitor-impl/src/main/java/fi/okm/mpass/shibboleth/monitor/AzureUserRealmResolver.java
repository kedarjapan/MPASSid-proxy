/*
 * The MIT License
 * Copyright (c) 2015 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.okm.mpass.shibboleth.monitor;

import org.apache.http.protocol.HttpContext;

import net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A {@link SequenceStepResolver} for resolving the Azure authenticator via username.
 */
public class AzureUserRealmResolver extends BaseSequenceStepResolver {
    
    /** The default API version. */
    public static final String DEFAULT_API_VERSION = "2.1";
    
    /** The user realm base URL. */
    public static final String USER_REALM_URL = "https://login.microsoftonline.com/common/userrealm/";
    
    /** The parameter key for user. */
    public static final String PARAM_KEY_USER = "user";
    
    /** The parameter key for STS request. */
    public static final String PARAM_KEY_STSREQUEST = "stsrequest";
    
    /** The parameter key for API version. */
    public static final String PARAM_KEY_API_VERSION = "api_version";
    
    /** The username for which the authenticator is resolved. */
    private final String username;
    
    /** The API version. */
    private final String apiVersion;

    /**
     * Constructor.
     * @param clientBuilder The builder for HTTP client.
     * @param userid The username for which the authenticator is resolved.
     */
    public AzureUserRealmResolver(final HttpClientBuilder clientBuilder, final String userid) {
        this(clientBuilder, userid, DEFAULT_API_VERSION);
    }

    /**
     * Constructor.
     * @param clientBuilder The builder for HTTP client.
     * @param userid The username for which the authenticator is resolved.
     * @param api The API version.
     */
    public AzureUserRealmResolver(final HttpClientBuilder clientBuilder, final String userid, final String api) {
        super(clientBuilder);
        username = Constraint.isNotEmpty(userid, "userid cannot be empty!");
        apiVersion = Constraint.isNotEmpty(api, "api version cannot be empty!");
    }
    
    /** {@inheritDoc} */
    public SequenceStep resolve(final HttpContext context, final SequenceStep startingStep) 
            throws ResponseValidatorException {
        final String restResponseStr = resolveStep(context, startingStep, false);
        final SequenceStep result = new SequenceStep();
        final String stsRequest = getValue(restResponseStr, "name=\"ctx\" value");
        final String query = "?" + PARAM_KEY_USER + "=" + username + "&" + PARAM_KEY_API_VERSION + "=" + apiVersion + 
                PARAM_KEY_STSREQUEST + "=" + stsRequest;
        result.setUrl(USER_REALM_URL + query);
        return result;
    }
}