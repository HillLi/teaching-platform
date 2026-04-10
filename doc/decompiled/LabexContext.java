// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.LabexContext
// Spring bean: "labexContext" (configured in spring-servlet.xml)
// ============================================================
package labex.common;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Application-wide context holder bean.
 *
 * Configured in spring-servlet.xml as a singleton bean with:
 *   - messageSource (injected reference to ReloadableResourceBundleMessageSource)
 *   - autoSaveTime = ${page.autosavetime} (600000 ms = 10 minutes)
 *
 * Purpose: Centralized access to i18n messages and the auto-save interval
 * used for periodically saving student experiment answers.
 */
public class LabexContext {

    /** Injected MessageSource for i18n (classpath:message*.properties) */
    private MessageSource messageSource;

    /** Auto-save interval in milliseconds (default 600000 = 10 min) */
    private long autoSaveTime;

    public LabexContext() {
    }

    // ---- Getters / Setters (set via Spring DI) ----

    public MessageSource getMessageSource() {
        return this.messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public long getAutoSaveTime() {
        return this.autoSaveTime;
    }

    public void setAutoSaveTime(long autoSaveTime) {
        this.autoSaveTime = autoSaveTime;
    }

    // ---- Utility Methods ----

    /**
     * Retrieve an internationalized message by code, using the current locale.
     * Message keys are defined in message_zh_CN.properties:
     *   login.succeed, logout.succeed, login.invalid, login.failed,
     *   login.forbidden, login.iperror, login.accounterror
     */
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * Retrieve an internationalized message with arguments.
     */
    public String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
