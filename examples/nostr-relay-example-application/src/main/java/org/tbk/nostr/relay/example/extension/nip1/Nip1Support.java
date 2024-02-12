package org.tbk.nostr.relay.example.extension.nip1;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.proto.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface Nip1Support {
    enum IndexedTagName {
        a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,
        A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z;
    }

    Flux<Event> findAllAfterCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt);


    Flux<Event> findAllAfterCreatedAtInclusiveWithTag(XonlyPublicKey author, int kind, Instant createdAt, IndexedTagName tagName, String firstTagValue);


    Mono<Void> markDeletedBeforeCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt);

    Mono<Void> markDeletedBeforeCreatedAtInclusiveWithTag(XonlyPublicKey author, int kind, Instant createdAt, IndexedTagName tagName, String firstTagValue);

}
