package com.example.onlinetalaba.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${spring.mail.password}")
    private String emailToken;

    public boolean sendVerificationCode(String to, String code) {
        String htmlContent = """
                <h2>Online Talaba platformasi uchun tasdiqlash kodi</h2>
                <p>Quyidagi kod orqali akkauntingizni faollashtiring:</p>
                <h1 style="color:blue;">%s</h1>
                <p>Agar bu so‘rov siz tomonidan yuborilmagan bo‘lsa, ushbu xabarni e’tiborsiz qoldiring.</p>
                """.formatted(code);

        return sendHtmlMail(to, "Email verification code", htmlContent);
    }

    public boolean sendResetPasswordCode(String to, String code) {
        String htmlContent = """
                <h2>Online Talaba platformasi uchun parolni tiklash kodi</h2>
                <p>Quyidagi kod orqali parolingizni yangilang:</p>
                <h1 style="color:red;">%s</h1>
                <p>Agar bu so‘rov siz tomonidan yuborilmagan bo‘lsa, ushbu xabarni e’tiborsiz qoldiring.</p>
                """.formatted(code);

        return sendHtmlMail(to, "Reset password code", htmlContent);
    }

    private boolean sendHtmlMail(String to, String subject, String htmlContent) {
        String host = "smtp.gmail.com";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailFrom, emailToken);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }
}