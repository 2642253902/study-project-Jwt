package org.example.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mailQueue")
public class MailQueueListener {

    @Resource
    JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String username;

    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = data.get("email").toString();
        String code = data.get("code").toString();
        String type = data.get("type").toString();
        SimpleMailMessage mailMessage = switch (type) {
            case "register" -> createMailMessage("注册验证码", "您的注册验证码是："
                    + code + "，请在3分钟内使用", email);
            case "reset" -> createMailMessage("重置密码验证码", "您的重置密码验证码是："
                    + code + "，请在3分钟内使用", email);
            default -> null;
        };
        if (mailMessage == null) {
            return;
        } else {
            javaMailSender.send(mailMessage);
        }
    }

    private SimpleMailMessage createMailMessage(String title, String content, String email) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject(title);
        mailMessage.setText(content);
        mailMessage.setTo(email);
        mailMessage.setFrom(username);
        return mailMessage;
    }
}
