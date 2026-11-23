package in.jansetu.it;

import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;
import in.jansetu.auth.otp.OtpSender;
import org.springframework.core.Ordered;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Takes precedence over the real senders in *IT tests and remembers the last code per target. */
public class CapturingOtpSender implements OtpSender, Ordered {
    private final Map<String, String> last = new ConcurrentHashMap<>();

    @Override
    public boolean supports(OtpChannel channel) {
        return true;
    }

    @Override
    public void send(String target, String code, OtpPurpose purpose) {
        last.put(target, code);
    }

    /** Removes and returns the last code sent to target, or null if nothing was sent. */
    public String take(String target) {
        return last.remove(target);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
