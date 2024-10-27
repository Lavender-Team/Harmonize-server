package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.entity.UserAnalysis;
import kr.ac.chungbuk.harmonize.repository.UserAnalysisRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAnalysisService {

    private final UserRepository userRepository;
    private final UserAnalysisRepository userAnalysisRepository;

    @Autowired
    public UserAnalysisService(UserRepository userRepository, UserAnalysisRepository userAnalysisRepository) {
        this.userRepository = userRepository;
        this.userAnalysisRepository = userAnalysisRepository;
    }

    @Transactional
    public void save(Long userId, Double highestPitch, Double lowestPitch) {
        User user = userRepository.findById(userId).orElseThrow();

        UserAnalysis userAnalysis = new UserAnalysis(highestPitch, lowestPitch);
        user.updateAnalysis(userAnalysis);

        userAnalysisRepository.save(userAnalysis);
        userRepository.save(user);
    }


}
