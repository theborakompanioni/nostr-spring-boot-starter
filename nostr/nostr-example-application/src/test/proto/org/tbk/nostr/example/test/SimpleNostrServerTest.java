package org.tbk.nostr.example.test;

import org.junit.jupiter.api.Test;

class SimpleNostrServerTest {

    @Test
    void test() throws Exception {
        SimpleNostrServer simpleNostrServer = new SimpleNostrServer();
        simpleNostrServer.afterPropertiesSet();

        Thread.sleep(1000L);

        simpleNostrServer.shutDown();
    }
}