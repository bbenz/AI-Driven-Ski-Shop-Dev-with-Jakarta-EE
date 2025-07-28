package com.skishop.ai;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Support Service Main Application
 * 
 * <p>LangChain4j 1.1.0 + Azure OpenAI を使用したAIサポートサービス</p>
 * 
 * @since 1.0.0
 */
@QuarkusMain
public class AiSupportServiceApplication implements QuarkusApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(AiSupportServiceApplication.class);
    
    public static void main(String... args) {
        logger.info("Starting AI Support Service Application...");
        Quarkus.run(AiSupportServiceApplication.class, args);
    }
    
    @Override
    public int run(String... args) throws Exception {
        logger.info("AI Support Service Application started successfully!");
        logger.info("LangChain4j version: 1.1.0");
        logger.info("Azure OpenAI integration enabled");
        
        Quarkus.waitForExit();
        return 0;
    }
}
