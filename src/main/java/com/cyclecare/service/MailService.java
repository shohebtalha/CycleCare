package com.cyclecare.service;

import com.cyclecare.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final String RESET_SUBJECT = "Reset your CycleCare password";

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(User user, String resetUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject(RESET_SUBJECT);
            helper.setText(buildResetEmail(user.getName(), resetUrl), true);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new IllegalStateException("Password reset email could not be sent.", ex);
        }
    }

    private String buildResetEmail(String name, String resetUrl) {
        String safeName = escapeHtml(name == null || name.isBlank() ? "there" : name.trim());
        String safeUrl = escapeHtml(resetUrl);
        return """
                <!DOCTYPE html>
                <html lang="en">
                <body style="margin:0;padding:0;background:#fff5f9;font-family:Arial,sans-serif;color:#4a2638;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#fff5f9;padding:32px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#ffffff;border:1px solid #ffd1e3;border-radius:12px;overflow:hidden;">
                          <tr>
                            <td style="background:#f86d9d;color:#ffffff;padding:24px 28px;font-size:24px;font-weight:700;">
                              CycleCare
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px;">
                              <p style="font-size:16px;line-height:1.5;margin:0 0 16px;">Hi %s,</p>
                              <p style="font-size:16px;line-height:1.5;margin:0 0 24px;">We received a request to reset your CycleCare password. Use the button below to choose a new password.</p>
                              <p style="text-align:center;margin:28px 0;">
                                <a href="%s" target="_self" style="background:#f86d9d;color:#ffffff;text-decoration:none;padding:14px 24px;border-radius:8px;font-weight:700;display:inline-block;">Reset password</a>
                              </p>
                              <p style="font-size:14px;line-height:1.5;margin:0 0 10px;">This link expires in 30 minutes and can only be used once.</p>
                              <p style="font-size:14px;line-height:1.5;margin:0 0 18px;">If the button does not work, copy and paste this URL into your browser:</p>
                              <p style="word-break:break-all;font-size:13px;line-height:1.5;margin:0 0 22px;"><a href="%s" target="_self" style="color:#d93672;">%s</a></p>
                              <p style="font-size:14px;line-height:1.5;margin:0;">If you did not request this, you can ignore this email. Your password will stay the same.</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#fff5f9;color:#8b5a70;padding:18px 28px;font-size:12px;line-height:1.5;">
                              CycleCare is an educational tracking tool, not a medical diagnostic tool.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(safeName, safeUrl, safeUrl, safeUrl);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
