package org.tbk.nostr.relay.plugin.allowlist.db.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.tbk.nostr.relay.plugin.allowlist.db.domain.AllowlistEntries;

public class StarterEntityRegistrar implements ImportBeanDefinitionRegistrar {

   @Override
   public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
      AutoConfigurationPackages.register(registry, AllowlistEntries.class.getPackageName());
   }
}