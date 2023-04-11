package com.honey.telegram_bot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
@Slf4j
public class ProxyConfig {
    // Proxy
    @Value("${app.proxy.host}")
    private String host;
    @Value("${app.proxy.port}")
    private int port;
    @Value("${app.proxy.open}")
    private boolean open;

    @Bean
    public DefaultBotOptions initDefaultBotOptions() {
        log.info("load proxy config: host={}, port={}, open={}", host, port, open);
        DefaultBotOptions options = new DefaultBotOptions();
        if (open) {
            options.setProxyHost(host);
            options.setProxyPort(port);
            options.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        }
        return options;
    }
}
