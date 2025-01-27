package org.tbk.nostr.relay.plugin.allowlist.config;

import com.google.common.base.Throwables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.nostr.relay.plugin.allowlist.Allowlist;
import org.tbk.nostr.relay.plugin.allowlist.validation.AllowlistValidator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class AllowlistPluginAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void beansAreCreated() {
        this.contextRunner.withUserConfiguration(AllowlistPluginAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.nostr.plugin.allowlist.allowed="
                        + "npub17vvjdx582l5yaxmd4kfjtjm5jvlkf62f03xr4rmh2umpu78d74jqxhkuj6,"
                        + "66b981cf8a15c68671d195dfabcd259b20dddc79f62f06fe578fe08258e7e0f8,"
                        + "npub1ttc0gv8qlq0ae75jkjuq44855k8a6sugwcuppmsea2hnxypdux6qlpta39"
                )
                .run(context -> {
                    assertThat(context.containsBean("allowlist"), is(true));
                    assertThat(context.getBean(Allowlist.class), is(notNullValue()));

                    assertThat(context.containsBean("allowlistValidator"), is(true));
                    assertThat(context.getBean(AllowlistValidator.class), is(notNullValue()));
                });
    }

    @Test
    void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(AllowlistPluginAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.nostr.plugin.allowlist.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("allowlist"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(Allowlist.class));

                    assertThat(context.containsBean("allowlistValidator"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(AllowlistValidator.class));
                });
    }

    @Test
    void throwOnInvalidPropertiesValues0() {
        this.contextRunner.withUserConfiguration(AllowlistPluginAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.nostr.plugin.allowlist.allowed=npub1_invalidnpub"
                )
                .run(context -> {
                    try {
                        // triggers creation of the bean
                        Allowlist ignoredOnPurpose = context.getBean(Allowlist.class);
                        fail("Should have failed to start application context");
                    } catch (Exception e) {
                        Throwable rootCause = Throwables.getRootCause(e);
                        assertThat(rootCause, is(instanceOf(BindValidationException.class)));

                        BindValidationException validationException = (BindValidationException) rootCause;
                        assertThat(validationException.getValidationErrors().hasErrors(), is(true));
                        assertThat(validationException.getValidationErrors().getAllErrors(), hasSize(1));

                        assertThat(validationException.getValidationErrors().getAllErrors().get(0).getDefaultMessage(), is("Error while parsing pubkey"));
                    }
                });
    }

    @Test
    void throwOnInvalidPropertiesValues1() {
        this.contextRunner.withUserConfiguration(AllowlistPluginAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.nostr.plugin.allowlist.allowed=0000000000000000000005dfabcd259b20dddc79f62f06fe578fe08258e7e0f8"
                )
                .run(context -> {
                    try {
                        // triggers creation of the bean
                        Allowlist ignoredOnPurpose = context.getBean(Allowlist.class);
                        fail("Should have failed to start application context");
                    } catch (Exception e) {
                        Throwable rootCause = Throwables.getRootCause(e);
                        assertThat(rootCause, is(instanceOf(BindValidationException.class)));

                        BindValidationException validationException = (BindValidationException) rootCause;
                        assertThat(validationException.getValidationErrors().hasErrors(), is(true));
                        assertThat(validationException.getValidationErrors().getAllErrors(), hasSize(1));

                        assertThat(validationException.getValidationErrors().getAllErrors().get(0).getDefaultMessage(), is("Value must be a valid public key"));
                    }
                });
    }
}
