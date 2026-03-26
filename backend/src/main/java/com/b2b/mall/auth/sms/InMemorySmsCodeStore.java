package com.b2b.mall.auth.sms;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemorySmsCodeStore {

    private static final class Entry {
        String code;
        long expiresAtEpochMs;
        long lastSentAtEpochMs;
    }

    private final ConcurrentHashMap<String, Entry> byPhone = new ConcurrentHashMap<>();

    /**
     * @return true 已写入新验证码；false 发送间隔过短未更新
     */
    public boolean tryPutCode(String phone, String code, long ttlMs, long minIntervalSinceLastSendMs) {
        long now = System.currentTimeMillis();
        final boolean[] accepted = {false};
        byPhone.compute(
                phone,
                (k, existing) -> {
                    if (existing != null && now - existing.lastSentAtEpochMs < minIntervalSinceLastSendMs) {
                        return existing;
                    }
                    Entry e = existing == null ? new Entry() : existing;
                    e.code = code;
                    e.expiresAtEpochMs = now + ttlMs;
                    e.lastSentAtEpochMs = now;
                    accepted[0] = true;
                    return e;
                });
        return accepted[0];
    }

    /** 校验成功则删除验证码（一次性） */
    public boolean verifyAndConsume(String phone, String inputCode) {
        long now = System.currentTimeMillis();
        Entry e = byPhone.get(phone);
        if (e == null) {
            return false;
        }
        synchronized (e) {
            if (now > e.expiresAtEpochMs) {
                byPhone.remove(phone, e);
                return false;
            }
            if (!e.code.equals(inputCode)) {
                return false;
            }
            byPhone.remove(phone);
            return true;
        }
    }
}
