package in.jansetu.auth.otp;

import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Sends OTPs by email (Spring Mail, configured from MAIL_* env vars). */
@Slf4j
@Component
@Profile("!dev")
public class EmailOtpSender implements OtpSender {
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;
    private final String host;

    public EmailOtpSender(ObjectProvider<JavaMailSender> mailSender, @Value("${jansetu.mail.from:}") String from,
                          @Value("${spring.mail.host:}") String host) {
        this.mailSender = mailSender;
        this.from = from;
        this.host = host;
    }

    @Override
    public boolean supports(OtpChannel channel) {
        return channel == OtpChannel.EMAIL;
    }

    @Override
    public void send(String target, String code, OtpPurpose purpose) {
        JavaMailSender mail = mailSender.getIfAvailable();
        if (mail == null || host.isBlank()) {
            log.error("Email OTP not sent: MAIL_HOST is not configured");
            return;
        }
        var m = new SimpleMailMessage();
        m.setTo(target);
        if (!from.isBlank()) m.setFrom(from);
        m.setSubject("JanSetu verification code / सत्यापन कोड");
        m.setText("Your JanSetu code is " + code + ". It expires in 5 minutes. Do not share it with anyone.\n\n"
                + "आपका JanSetu कोड " + code + " है। यह 5 मिनट में समाप्त हो जाएगा। इसे किसी से साझा न करें।");
        try {
            mail.send(m);
        } catch (MailException e) {
            // Never fail the request: an error only for existing accounts would reveal that they exist.
            log.error("Email OTP delivery failed: {}", e.getMessage());
        }
    }
}
