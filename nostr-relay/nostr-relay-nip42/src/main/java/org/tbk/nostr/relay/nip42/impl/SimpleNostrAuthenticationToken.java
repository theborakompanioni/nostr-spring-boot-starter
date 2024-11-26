package org.tbk.nostr.relay.nip42.impl;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;
import org.tbk.nostr.relay.nip42.Nip42Support;

import java.io.Serial;
import java.util.Collection;

public class SimpleNostrAuthenticationToken extends AbstractAuthenticationToken implements Nip42Support.NostrAuthentication {

    @Value
    @RequiredArgsConstructor
    public static class SimplePublicKeyPrincipal implements Nip42Support.PublicKeyPrincipal {
        @NonNull
        XonlyPublicKey publicKey;
    }

    @Serial
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final Nip42Support.PublicKeyPrincipal principal;

    private Object credentials;

    public SimpleNostrAuthenticationToken(XonlyPublicKey principal, Object credentials) {
        super(null);
        this.principal = new SimplePublicKeyPrincipal(principal);
        this.credentials = credentials;
        setAuthenticated(false);
    }

    public SimpleNostrAuthenticationToken(XonlyPublicKey principal, Object credentials,
                                          Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = new SimplePublicKeyPrincipal(principal);
        this.credentials = credentials;
        super.setAuthenticated(true); // must use super, as we override
    }

    public static SimpleNostrAuthenticationToken unauthenticated(XonlyPublicKey principal, Object credentials) {
        return new SimpleNostrAuthenticationToken(principal, credentials);
    }

    public static SimpleNostrAuthenticationToken authenticated(XonlyPublicKey principal, Object credentials,
                                                               Collection<? extends GrantedAuthority> authorities) {
        return new SimpleNostrAuthenticationToken(principal, credentials, authorities);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Nip42Support.PublicKeyPrincipal getPrincipal() {
        return this.principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated,
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }

}
