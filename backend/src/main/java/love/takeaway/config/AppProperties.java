package love.takeaway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Identity identity = new Identity();
    private final Notification notification = new Notification();
    private final Storage storage = new Storage();
    private final Wechat wechat = new Wechat();
    private final LocalSeed localSeed = new LocalSeed();
    private final Bootstrap bootstrap = new Bootstrap();

    public Identity getIdentity() {
        return identity;
    }

    public Notification getNotification() {
        return notification;
    }

    public Storage getStorage() {
        return storage;
    }

    public Wechat getWechat() {
        return wechat;
    }

    public LocalSeed getLocalSeed() {
        return localSeed;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public static class Identity {
        private String mode = "debug";
        private String debugHeader = "X-Debug-Openid";
        private String wechatHeader = "X-WX-OPENID";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getDebugHeader() {
            return debugHeader;
        }

        public void setDebugHeader(String debugHeader) {
            this.debugHeader = debugHeader;
        }

        public String getWechatHeader() {
            return wechatHeader;
        }

        public void setWechatHeader(String wechatHeader) {
            this.wechatHeader = wechatHeader;
        }
    }

    public static class Notification {
        private String mode = "log";
        private long dispatchDelayMs = 10_000;
        private int batchSize = 20;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public long getDispatchDelayMs() {
            return dispatchDelayMs;
        }

        public void setDispatchDelayMs(long dispatchDelayMs) {
            this.dispatchDelayMs = dispatchDelayMs;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }

    public static class Storage {
        private String directory = ".data/uploads";

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }

    public static class Wechat {
        private String appId = "";
        private String appSecret = "";
        private String newOrderTemplateId = "";
        private String orderStatusTemplateId = "";

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public String getNewOrderTemplateId() {
            return newOrderTemplateId;
        }

        public void setNewOrderTemplateId(String newOrderTemplateId) {
            this.newOrderTemplateId = newOrderTemplateId;
        }

        public String getOrderStatusTemplateId() {
            return orderStatusTemplateId;
        }

        public void setOrderStatusTemplateId(String orderStatusTemplateId) {
            this.orderStatusTemplateId = orderStatusTemplateId;
        }
    }

    public static class LocalSeed {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Bootstrap {
        private String merchantCode = "";
        private int validDays = 7;

        public String getMerchantCode() {
            return merchantCode;
        }

        public void setMerchantCode(String merchantCode) {
            this.merchantCode = merchantCode;
        }

        public int getValidDays() {
            return validDays;
        }

        public void setValidDays(int validDays) {
            this.validDays = validDays;
        }
    }
}
