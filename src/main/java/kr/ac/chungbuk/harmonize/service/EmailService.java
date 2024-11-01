package kr.ac.chungbuk.harmonize.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import kr.ac.chungbuk.harmonize.entity.User;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    // 아이디 전송 메서드
    public void sendId(String email, String loginId) {
        String subject = "하모나이즈 아이디 찾기 결과";
        String text = "안녕하세요,\n\n귀하의 아이디는 " + loginId + " 입니다.\n\n감사합니다.";
        sendEmail(email, subject, text);
    }

    // 비밀번호 재설정 링크 전송 메서드
    public void sendPasswordResetLink(String email, String token) {
        String subject = "하모나이즈 비밀번호 재설정";
        String resetLink = "http://localhost:3000/reset-password/" + token;
        String text = "안녕하세요,\n\n아래 링크를 클릭하여 비밀번호를 재설정하세요:\n" + resetLink + "\n\n감사합니다.";
        sendEmail(email, subject, text);
    }

    // 이메일 발송 메서드
    private void sendEmail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("harmonize2024@haemonize.com");
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}

