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

package fi.okm.mpass.idp.authn.impl;

import java.text.ParseException;
import java.util.List;

import javax.annotation.Nonnull;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.openid.connect.sdk.claims.ACR;

import fi.okm.mpass.idp.authn.SocialUserAuthenticationException;
import fi.okm.mpass.idp.authn.SocialUserErrorIds;
import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;

/**
 * An action that verifies ACR of ID Token.
 * 
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 */
@SuppressWarnings("rawtypes")
public class ValidateOIDCIDTokenACR extends AbstractAuthenticationAction {

    /*
     * A DRAFT PROTO CLASS!! NOT TO BE USED YET.
     * 
     * FINAL GOAL IS TO MOVE FROM CURRENT OIDC TO MORE WEBFLOW LIKE
     * IMPLEMENTATION.
     */

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(ValidateOIDCIDTokenACR.class);

    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {
        log.trace("Entering");

        final SocialUserOpenIdConnectContext suCtx = authenticationContext
                .getSubcontext(SocialUserOpenIdConnectContext.class, true);
        if (suCtx == null) {
            log.error("{} Not able to find su oidc context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext,
                    AuthnEventIds.NO_CREDENTIALS);
            log.trace("Leaving");
            return;
        }

        // Check acr
        // If the acr Claim was requested, the Client SHOULD check that the
        // asserted Claim Value is appropriate. The meaning and processing
        // of acr Claim Values is out of scope for this specification.
        List<ACR> acrs = suCtx.getOpenIdConnectInformation().getAcr();
        if (acrs != null && acrs.size() > 0) {
            String acr;
            try {
                acr = suCtx.getOidcTokenResponse().getOIDCTokens().getIDToken()
                        .getJWTClaimsSet().getStringClaim("acr");
            } catch (ParseException e) {
                log.error("{} Error parsing id token", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext,
                        AuthnEventIds.NO_CREDENTIALS);
                log.trace("Leaving");
                return;
            }
            if (acr == null) {
                log.error("{} acr requested but not received", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext,
                        AuthnEventIds.NO_CREDENTIALS);
                log.trace("Leaving");
                return;
            }
            if (!acrs.contains(acr)) {
                log.error("{} acr received does not match requested:" + acr,
                        getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext,
                        AuthnEventIds.NO_CREDENTIALS);
                log.trace("Leaving");
                return;
            }
        }
        log.trace("Leaving");
        return;
    }

}
